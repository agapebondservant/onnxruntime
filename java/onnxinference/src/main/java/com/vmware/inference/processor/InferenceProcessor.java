package com.vmware.inference.processor;

import com.vmware.inference.main.InvokeInference;
import com.vmware.inference.service.InferenceService;
import com.vmware.inference.util.Helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ai.onnxruntime.*;
import ai.onnxruntime.OrtSession.*;
import ai.onnxruntime.OrtSession.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InferenceProcessor {

    static Logger LOGGER = LoggerFactory.getLogger(InferenceProcessor.class);

    public Map process( String modelName, String input ){
        Map result = new HashMap<>();

        try {

            OrtEnvironment env = OrtEnvironment.getEnvironment();

            Map<String, OnnxTensor> container = new HashMap<>();

            String modelPath = Helpers.getResourcePath(
                    String.format("/%s", modelName),
                    true);

            String inferenceInput = input;

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

                result = inferenceService.getInferenceResult(resultOutputProbability, resultOutputLabel);

                LOGGER.info(result.toString());

            } finally {
                inferenceTensor.close();
            }
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
        return result;
    }
}
