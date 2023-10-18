package com.vmware.inference;

import ai.onnxruntime.*;
import ai.onnxruntime.OrtSession.*;
import ai.onnxruntime.OrtSession.Result;
import com.vmware.inference.service.InferenceService;
import com.vmware.inference.util.Helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static ai.onnxruntime.OrtSession.Result.*;

public class InvokeInference {

    static Logger LOGGER = LoggerFactory.getLogger(InvokeInference.class);

    public static void main(String[] args) {

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // USAGE
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (args.length != 2) {
            LOGGER.info("Usage: TestInference <path/to/onnx/model> <array of model inputs>");
        }

        else {

            try {

                OrtEnvironment env = OrtEnvironment.getEnvironment();

                Map<String, OnnxTensor> container = new HashMap<>();

                String modelPath = Helpers.getResourcePath(String.format("/%s", args[0]), false);

                String inferenceInput = args[1];

                double[][] inferenceData = Helpers.convertStringTo2DArray(inferenceInput);

                OnnxTensor inferenceTensor = OnnxTensor.createTensor(env, inferenceData);

                OrtSession.SessionOptions options = new SessionOptions();

                InferenceService inferenceService = new InferenceService();

                options.setIntraOpNumThreads(8);

                try (OrtSession session = env.createSession(modelPath, options)) {

                    NodeInfo inputMetaData = session.getInputInfo().values().iterator().next();

                    container.put(inputMetaData.getName(), inferenceTensor);

                    OrtSession.Result results = session.run(container);

                    Optional<OnnxValue> resultOutputProbability = results.get("output_probability");

                    Optional<OnnxValue> resultOutputLabel = results.get("output_label");

                    LOGGER.info(inferenceService.getInferenceResult(resultOutputProbability, resultOutputLabel).toString());

                } finally {
                    inferenceTensor.close();
                }
            } catch (Exception ex) {
                LOGGER.error("", ex);
            }
        }
    }
}
