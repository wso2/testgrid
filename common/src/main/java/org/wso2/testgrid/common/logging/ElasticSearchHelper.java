/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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

/**
 * This class is responsible for handling and communicating with TestGrid Elastic Search Instance provided in the
 * TestGrid configuration files
 *
 * @since 1.0.0
 *
 */

class ElasticSearchHelper {

    private static Logger logger = LoggerFactory.getLogger(KibanaDashboardBuilder.class);
    private String esEndpoint;

    public ElasticSearchHelper() {

        logger.info("Initializing Elastic Search Helper ..");
        esEndpoint = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.ES_ENDPOINT_URL).concat("/_search");

    }
    /**
     * Will return all indices given under a certain k8s namespace in the elastic search instance
     * @param nameSpace
     * @return
     */
    public ArrayList<String> getAllIndexes(String nameSpace) {
        String queryData = "{ \"query\" : { \"bool\" : { \"should\" : [ { \"term\" : { \"namespace\" : \"" +
                nameSpace + "\" " + "} } ] } }, \"aggs\":{ \"unique_ids\": { \"terms\": { \"field\": \"_index\"  } }}}";
        String[] commandTerms = {"curl", "-s", "-XGET", esEndpoint, "-H", "Content-Type: application/json", "-d",
                queryData};
        ProcessBuilder curlCommand = new ProcessBuilder(commandTerms);

        StringBuilder curlOutput = new StringBuilder();
        // Stores unique indices
        ArrayList<String> indexList = new ArrayList<>();

        try (BufferedReader curlOutputReader =  new BufferedReader(
                new InputStreamReader(curlCommand.start().getInputStream(), StandardCharsets.UTF_8))) {
            while (true) {
                String outputLine = curlOutputReader.readLine();
                if (outputLine == null) {
                    break;
                }
                curlOutput.append(outputLine);
            }
            JSONObject curlOutputJson = new JSONObject(curlOutput.toString());

            JSONArray indexJSONList = curlOutputJson.getJSONObject("aggregations").getJSONObject("unique_ids").
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
