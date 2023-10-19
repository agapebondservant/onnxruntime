package com.vmware.inference.main;
import com.vmware.inference.processor.InferenceProcessor;
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
                new InferenceProcessor().process( args[0], args[1] );
            } catch (Exception ex) {
                LOGGER.error("", ex);
            }
        }
    }
}
