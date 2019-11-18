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

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.config.ConfigurationContext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
    private RestHighLevelClient esClient;


    public void open() {
        esEndpoint = ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.ES_ENDPOINT_URL);
        try {
            String urlString =
                    new URL(esEndpoint).getHost();
            RestClientBuilder builder = RestClient.builder(new HttpHost(urlString, 80, "http"));
            esClient = new RestHighLevelClient(builder);
            logger.info("Initialized Elastic Stack Helper");
        } catch (MalformedURLException e) {
            logger.error("Could not initialize ElasticSearch Helper cannot talk to Elastic Search");
        }
    }
    /**
     * Will return all indices given under a certain k8s namespace in the elastic search instance
     * @param nameSpace
     * @return
     */
    public ArrayList<String> getAllIndexes(String nameSpace) {

        ArrayList<String> indexList = new ArrayList<>();
        open();
        if (esClient != null) {
            try {
                QueryBuilder namespaceMatchQuery = QueryBuilders.boolQuery()
                        .should(QueryBuilders.termQuery("namespace", nameSpace));
                AggregationBuilder uniqueIndexAggregator
                        = AggregationBuilders.terms("unique_ids").field("_index");

                SearchSourceBuilder querySourceBuilder = new SearchSourceBuilder();
                querySourceBuilder.aggregation(uniqueIndexAggregator).query(namespaceMatchQuery);

                SearchRequest searchRequest = new SearchRequest().source(querySourceBuilder);
                SearchResponse searchResponse = esClient.search(searchRequest);
                Terms aggregationOutput =  searchResponse.getAggregations().get("unique_ids");

                for (Terms.Bucket indexBucket : aggregationOutput.getBuckets()) {
                    indexList.add(indexBucket.getKey().toString());
                }
                esClient.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.error("Could not get index list");
            }
        } else {
            logger.error("Client not initialized Could not get index list");
        }

        return indexList;
    }

}
