package com.vmware.gemfire;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.vmware.inference.processor.InferenceProcessor;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.apache.geode.security.ResourcePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnnxInferenceFunction implements Function {

    static Logger LOGGER = LoggerFactory.getLogger(OnnxInferenceFunction.class);

    InferenceProcessor inferenceProcessor = new InferenceProcessor();

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext functionContext) {
        if (functionContext instanceof RegionFunctionContext) {
            RegionFunctionContext rfc = (RegionFunctionContext) functionContext;
            Object rawArguments = rfc.getArguments();
            if (rawArguments == null ||
                    !(rawArguments instanceof Object[]) ||
                        ((Object[])rawArguments).length != 2 ) {
                throw new RuntimeException("Invalid arguments: " + (arguments.length > 0 ? arguments[0] : "NOARGS"));
            }

            String[] arguments = Arrays.stream(obj)
                    .map(Object::toString)
                    .toArray(String[]::new);
            sendInferenceResults(arguments, rfc);
        } else {
            throw new RuntimeException("The function must be executed for partitioned regions");
        }
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean optimizeForWrite() {
        return false;
    }

    @Override
    public boolean isHA() {
        return false;
    }

    @Override
    public Collection<ResourcePermission> getRequiredPermissions(String regionName) {
        return Function.super.getRequiredPermissions(regionName);
    }

    @Override
    public Collection<ResourcePermission> getRequiredPermissions(String regionName, Object args) {
        return Function.super.getRequiredPermissions(regionName, args);
    }

    private void sendInferenceResults(String[] arguments, RegionFunctionContext rfc) {
        Region r = PartitionRegionHelper.getLocalDataForContext(rfc);

        if (r.size() > 0) {
            try {
                Set keys = r.keySet();
                // TODO: Use the function context in meaningful ways, such as finding past transactions within close geospatial distance
                // or computing the time elapsed since a transaction has occurred from the same region
            } catch (Exception ex) {
                LOGGER.error("", ex);
            }
        }

        Map inferenceResults = inferenceProcessor.process( arguments[0], arguments[1] );
        rfc.getResultSender().lastResult( inferenceResults );
    }
}

