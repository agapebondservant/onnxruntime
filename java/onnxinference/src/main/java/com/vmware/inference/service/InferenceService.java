package com.vmware.inference.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import ai.onnxruntime.*;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OrtSession.*;
import static ai.onnxruntime.OrtSession.Result.*;

public class InferenceService {
    public Map getInferenceResult(Optional<OnnxValue> outputProba, Optional<OnnxValue> outputLabel) throws Exception {

        Object returnProba = null, returnLabel = null;

        if (outputLabel.isPresent()) {
            returnLabel = ( (long[]) outputLabel.get().getValue() )[ 0 ];
        }

        if (outputProba.isPresent()) {

            OnnxValue onnxValue = (OnnxValue) outputProba.get();

            if ( onnxValue instanceof OnnxSequence) {

                OnnxSequence resultSequence = (OnnxSequence) onnxValue;

                List<? extends OnnxValue> elements = resultSequence.getValue();

                returnProba = ((OnnxMap) elements.get(0)).getValue();
            } else if ( onnxValue instanceof OnnxTensor) {

                OnnxTensor resultTensor = (OnnxTensor) onnxValue;

                returnProba = resultTensor.getValue();
            }
        }

        return Map.of(
                "predict", returnLabel,
                "predict_proba", (Long) returnLabel == null ? null : ((Map)returnProba).get(returnLabel),
                "predict_proba_all", returnProba
        );
    }
}
