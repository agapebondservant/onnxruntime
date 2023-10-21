package test.gemfire;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.execute.*;
import org.apache.geode.json.JsonDocument;
import org.apache.geode.json.JsonDocumentFactory;
import org.apache.geode.json.JsonParseException;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheTransactionManager;
import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.apache.geode.security.ResourcePermission;

public class InferenceClient {

    public static void main(String[] args) {
        Properties clientCacheProps = new Properties();
        clientCacheProps.setProperty("log-level", "config");

        ClientCache cache = new ClientCacheFactory(clientCacheProps).addPoolLocator("gfanomaly-locator-0.gfanomaly-locator.anomaly-ns.svc.cluster.local", 10334)
                .create();

        Region region = cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("mds-region-greenplum");
        Execution execution = FunctionService.onRegion(region)
                .setArguments(args);
        ResultCollector collector = execution.execute("OnnxInferenceFunction");
        List<Map> results = (List<Map>) collector.getResult();
        for (Map result : results) {
            System.out.println(result);
        }
    }
}
