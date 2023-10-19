package com.vmware.inference.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helpers {
    static Logger LOGGER = LoggerFactory.getLogger(Helpers.class);

    public static String getResourcePath(String resourcePath, boolean useFullPath) {
        if (useFullPath) {

            try (InputStream sourceStream = Helpers.class.getResourceAsStream(resourcePath)){

                File target = new File("/tmp/" + resourcePath );

                Files.copy(
                        sourceStream,
                        target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                return target.toPath().toString();

            } catch ( Exception ex ) {
                LOGGER.error("", ex);
                return null;
            }
        } else {
            return new File( Helpers.class.getResource(resourcePath).getFile() )
                    .toPath()
                    .toString();
        }
    }

    // TODO: Support multiple dimensional arrays
    public static double[][] convertStringTo2DArray(String input) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(
                input.replace("{","[").replace("}","]"),
                double[][].class);
    }
}
