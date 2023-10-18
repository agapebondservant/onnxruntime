package com.vmware.inference.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class Helpers {
    public static String getResourcePath(String path, boolean useFullPath) {
        return useFullPath ?
                Helpers.class.getClassLoader().getResource(path).toExternalForm() :
                new File(Helpers.class.getResource(path).getFile()).toPath().toString();
    }

    // TODO: Support multiple dimensional arrays
    public static double[][] convertStringTo2DArray(String input) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(
                input.replace("{","[").replace("}","]"),
                double[][].class);
    }
}
