import numpy as np
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
import onnxruntime as rt
import onnxruntime.tools
from skl2onnx import to_onnx
import psycopg2
import pandas as pd
from sqlalchemy import create_engine
import subprocess
import sys

db_connect_url = 'postgresql+psycopg2://gpadmin:Uu4jcDSjqlDVQ@44.201.91.88:5432/dev'
conn = create_engine(db_connect_url, pool_recycle=3600).connect()
df = pd.read_sql("select * from \"rf_credit_card_transactions_training\"", conn)
X, y = df[["time_elapsed", "amt", "lat", "long"]].to_numpy(), df[["is_fraud"]].to_numpy()

X_train, X_test, y_train, y_test = train_test_split(X, y)
clr = RandomForestClassifier(random_state=1, n_estimators=5, class_weight='balanced')
clr.fit(X_train, y_train)


onx = to_onnx(clr, X[:1])
with open("java/onnxinference/src/main/resources/rf_fraud_2.onnx", "wb") as f:
    f.write(onx.SerializeToString())

# Generate optimized ORT model
subprocess.call([sys.executable,
                 '-m',
                 'onnxruntime.tools.convert_onnx_models_to_ort',
                 'java/onnxinference/src/main/resources/rf_fraud_2.onnx'])

sess = rt.InferenceSession("java/onnxinference/src/main/resources/rf_fraud_2.onnx", providers=["CPUExecutionProvider"])
input_name = sess.get_inputs()[0].name
label_name = sess.get_outputs()[0]
inference_data = np.array([(20, 245, -54.3, 192.5)])
pred_onx = sess.run(None, {input_name: inference_data.astype(np.double)})[1]
print(f"{pred_onx} {input_name} {label_name} {inference_data.shape}")


