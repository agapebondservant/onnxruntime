To run python script:
```
pip install -r requirements.txt
python python/randomforestonnx.py
```

To view the ONNX model:
```
pip install netron
netron </path/to/model>
```

Deploy the ONNXInference setuup:
```
source .env
mvn wrapper:wrapper
scripts/deploy-gf-onnx-inference.sh
```

Test the standalone ONNXInference service:
```
cd java/onnxinference
mvn clean package -f pom-onnx.xml
mvn exec:java -f pom-onnx.xml -Dexec.args="rf_fraud_2.onnx {{20,245,-54.3,192.5}}" # should return without errors
cd -
```

Test the Gemfire function:
```
cd java/onnxinference
mvn clean package -f pom-test.xml -Ddeploy.scope=compile
kubectl exec -it gfclient -n  ${ML_GEMFIRE_TEST_NS} -- java -jar /usr/local/tomcat/temp/${ML_GEMFIRE_LIB_CLIENT_JARNAME} rf_fraud_2.onnx "{{20,245,-54.3,192.5}}" 
cd ../..
```

For troubleshooting:
```
kubectl logs ${ML_GEMFIRE_POD_1} -n ${ML_GEMFIRE_TEST_NS} # for troubleshooting (if the functions were not successfully deployed)
```