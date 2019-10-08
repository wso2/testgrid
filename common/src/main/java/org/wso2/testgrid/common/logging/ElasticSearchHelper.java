package org.wso2.testgrid.common.logging;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.config.ConfigurationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class ElasticSearchHelper {

    private static Logger logger = LoggerFactory.getLogger(KibanaDashboardBuilder.class);
    private String esEndpoint;

    public ElasticSearchHelper() {

        logger.info("Initializing KibanaDashboardBuilder..");
        esEndpoint = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.ES_ENDPOINT_URL);

    }
    /**
     * Will return all indices given under a certain namespace in the elastic search instance
     * @param nameSpace
     * @return
     */
    public ArrayList<String> getAllIndexes(String nameSpace) {
        String data = "{ \"query\" : { \"bool\" : { \"should\" : [ { \"term\" : { \"namespace\" : \"" +
                nameSpace + "\" " + "} } ] } }, \"aggs\":{ \"unique_ids\": { \"terms\": { \"field\": \"_index\"  } }}}";
        String[] command = {"curl", "-s", "-XGET", esEndpoint, "-H", "Content-Type: application/json", "-d", data};
        ProcessBuilder curlCommand = new ProcessBuilder(command);
        Process execCurl;
        StringBuilder responseStrBuilder = new StringBuilder();
        ArrayList<String> indexList = new ArrayList<>();

        try (BufferedReader reader =  new BufferedReader(new InputStreamReader(curlCommand.start().getInputStream(),
                StandardCharsets.UTF_8))) {
            while (true) {
                String inputStr = reader.readLine();
                if (inputStr == null) {
                    break;
                }
                responseStrBuilder.append(inputStr);
            }
            JSONObject j = new JSONObject(responseStrBuilder.toString());
            JSONArray indexJSONList = j.getJSONObject("aggregations").getJSONObject("unique_ids").
                    getJSONArray("buckets");


            for (int i = 0; i < indexJSONList.length(); i++) {
                JSONObject indexJson = indexJSONList.getJSONObject(i);
                indexList.add((String) indexJson.get("key"));
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return indexList;
    }

}
