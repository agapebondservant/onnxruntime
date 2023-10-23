#!/bin/bash

source .env

echo "......................................................................."
echo "Starting script........................................................"
echo "......................................................................."

echo "......................................................................."
echo "Undeploy GF functions..."
echo "......................................................................."
kubectl exec -it ${ML_GEMFIRE_POD_1} -n ${ML_GEMFIRE_TEST_NS} -- gfsh -e "connect --locator=${ML_GEMFIRE_LOCATOR_ADDR}[10334]" -e "undeploy"
kubectl exec -it ${ML_GEMFIRE_POD_1} -n ${ML_GEMFIRE_TEST_NS} -- rm  -rf /data/deployments/${ML_GEMFIRE_LIB_INFERENCEFUNCTION_MODULENAME}; rm -rf /data/deployments/${ML_GEMFIRE_LIB_ONNXSERVICE_MODULENAME}
kubectl exec -it ${ML_GEMFIRE_POD_2} -n ${ML_GEMFIRE_TEST_NS} -- rm  -rf /data/deployments/${ML_GEMFIRE_LIB_INFERENCEFUNCTION_MODULENAME}; rm -rf /data/deployments/${ML_GEMFIRE_LIB_ONNXSERVICE_MODULENAME}
echo "Undeployment complete."

echo "......................................................................."
echo "Build and deploy the ONNXInference service..."
echo "......................................................................."
cd java/onnxinference
envsubst < settings.xml.template > settings.xml
./mvnw clean package -f pom-onnx.xml -s settings.xml
kubectl cp ${ML_GEMFIRE_LIB_ONNXSERVICE_JARPATH} ${ML_GEMFIRE_TEST_NS}/${ML_GEMFIRE_POD_1}:/tmp
kubectl exec -it ${ML_GEMFIRE_POD_1} -n ${ML_GEMFIRE_TEST_NS} -- gfsh -e "connect --locator=${ML_GEMFIRE_LOCATOR_ADDR}[10334]" -e "deploy --jars=/tmp/onnxinference-1.0-SNAPSHOT.jar"
sleep 5
cd -
echo "Deployment complete."

echo "......................................................................."
echo "Build and deploy the Gemfire function..."
echo "......................................................................."
cd java/onnxinference
./mvnw clean package -f pom-gemfire.xml -s settings.xml
kubectl cp ${ML_GEMFIRE_LIB_INFERENCEFUNCTION_JARPATH} ${ML_GEMFIRE_TEST_NS}/${ML_GEMFIRE_POD_1}:/tmp
kubectl exec -it ${ML_GEMFIRE_POD_1} -n ${ML_GEMFIRE_TEST_NS} -- gfsh -e "connect --locator=${ML_GEMFIRE_LOCATOR_ADDR}[10334]" -e "deploy --jars=/tmp/gemfire-onnx-1.0-SNAPSHOT.jar" -e "list functions"
sleep 5
cd -
echo "Deployment complete."

echo "......................................................................."
echo "Build and deploy the Gemfire client app..."
echo "......................................................................."
cd java/onnxinference
kubectl delete pod gfclient -n${ML_GEMFIRE_TEST_NS} || true
kubectl run gfclient -n${ML_GEMFIRE_TEST_NS} --labels=app=gfclient --image=tomcat:9.0.82-jdk11-corretto
kubectl wait --for=condition=ready pod -l app=gfclient -n${ML_GEMFIRE_TEST_NS} --timeout=30s
kubectl exec -it gfclient -n${ML_GEMFIRE_TEST_NS} -- yum install -y tar
./mvnw clean package -f pom-test.xml -s settings.xml -Ddeploy.scope=compile

for i in {1..3} # Workaround - run "kubectl cp" multiple times, to workaround failures for larger files
do
  kubectl cp ${ML_GEMFIRE_LIB_CLIENT_JARPATH} ${ML_GEMFIRE_TEST_NS}/gfclient:/usr/local/tomcat/temp
done
cd -
echo "Deployment complete."

echo "......................................................................."
echo "Finished script."