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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.config.ConfigurationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

/**
 * This class is responsible for building the Kibana dashboard URL used to display the log content
 * of a AWS cloudformation stack.This class is a singleton implementation to make sure the log URL
 * is built for all the consecutive stacks created
 * during a TestPlan
 *
 * @since 1.0.0
 *
 */
public class KibanaDashboardBuilder {

    private static Logger logger = LoggerFactory.getLogger(KibanaDashboardBuilder.class);
    private String kibanaEndpoint;
    private String dashboardCtxFormat;
    private String instanceLogFilterFormat;
    private String allLogsFilter;
    private String allLogsFilterEncodedSection;
    private String allLogsFilterJsonSection;
    private Map<String, Map<String, String>> allInstances;
    private Set<String> allK8SNameSpaces;
    private String allLogsFilterEncodedSectionK8S;
    private String allLogsFilterJsonSectionK8S;
    private String instanceLogFilterFormatK8S;

    private static volatile KibanaDashboardBuilder kibanaDashboardBuilder;
    private static final Object lock = new Object();

    /**
     * private constructor to populate all the fields from reading the config.properties file
     *
     */
    private KibanaDashboardBuilder() {

        logger.info("Initializing KibanaDashboardBuilder..");
        kibanaEndpoint = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.KIBANA_ENDPOINT_URL);
        dashboardCtxFormat = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.KIBANA_DASHBOARD_STR);
        instanceLogFilterFormat = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.KIBANA_FILTER_STR);
        allLogsFilter = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.KIBANA_ALL_LOGS_FILTER);
        allLogsFilterEncodedSection = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.REPEATABLE_ALL_LOGS_FILTER_STRING);
        allLogsFilterJsonSection = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.REPEATABLE_ALL_LOGS_JSON);
        allLogsFilterEncodedSectionK8S = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.REPEATABLE_ALL_LOGS_FILTER_STRING_K8S);
        allLogsFilterJsonSectionK8S = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.REPEATABLE_ALL_LOGS_JSON_K8S);
        instanceLogFilterFormatK8S = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.KIBANA_FILTER_STR_K8S);
        allInstances = new HashMap<>();
        allK8SNameSpaces = new HashSet<>();
    }

    /**
     * This method returns the current instance of {@link KibanaDashboardBuilder}. it is the only
     * method capable of creating an instance and is responsible for the singleton behavior of
     * the class.
     *
     * @return the available {@link KibanaDashboardBuilder} instance
     */
    public static KibanaDashboardBuilder getKibanaDashboardBuilder() {
        if (kibanaDashboardBuilder == null) {
            kibanaDashboardBuilder = new KibanaDashboardBuilder();
        }
        return kibanaDashboardBuilder;
    }

    /**
     * This method is responsible for building the Kibana dashboard for the available stacks.
     *
     * @param currentInstancesMap Map of instanceName and instance-id
     * @param currentStackName CloudFormation stack name
     * @param shortenURL weather to shorten URL or not, primarily used for unit testing purposes
     * @return Optional of dashboard link shortened to make it compatible with database column
     * restrictions
     */
    public Optional<String> buildDashBoard(Map<String, String> currentInstancesMap
            , String currentStackName, boolean shortenURL) {

        if (kibanaEndpoint == null || dashboardCtxFormat == null || instanceLogFilterFormat == null) {
            logger.warn("Kibana endpoint configuration not found in testgrid config. Server log view may not work!" +
                    "Kibana Endpoint : " + kibanaEndpoint +
            "\nDashbboardCtxFormat : " + dashboardCtxFormat +
            "\ninstanceLogFilterFormat : " + instanceLogFilterFormat);
            return Optional.empty();
        }
        //TODO implement filtering unwanted nodes with no log input i.e BastianNode , PuppetMaster
        allInstances.put(currentStackName, currentInstancesMap);
        String instanceLogFilter;
        StringJoiner filtersStr = new StringJoiner(",");

        for (Map.Entry<String, Map<String, String>> allInstancesEntry : allInstances.entrySet()) {
            Map<String, String> instanceMap = allInstancesEntry.getValue();
            for (Map.Entry<String, String> entry : instanceMap.entrySet()) {
                instanceLogFilter = instanceLogFilterFormat
                        .replaceAll("#_INSTANCE_ID_#", entry.getKey())
                        .replaceAll("#_LABEL_#", entry.getValue())
                        .replaceAll("#_STACK_NAME_#", allInstancesEntry.getKey());
                filtersStr.add(instanceLogFilter);
            }
        }

        StringJoiner allLogsStr = new StringJoiner(",");
        StringJoiner allLogsJson = new StringJoiner(",");

        for (String stackName : allInstances.keySet()) {
            allLogsStr.add(allLogsFilterEncodedSection.replaceAll("#_STACK_NAME_#", stackName));
            allLogsJson.add(allLogsFilterJsonSection.replaceAll("#_STACK_NAME_#", stackName));
        }
        allLogsFilter = allLogsFilter
                .replaceAll("#_ALL_LOGS_FILTER_SECTION_#", allLogsStr.toString())
                .replaceAll("#_REPEATABLE_ALL_LOGS_JSON_SECTION_#", allLogsJson.toString());
        filtersStr.add(allLogsFilter);
        String logDownloadCtx = dashboardCtxFormat.replace("#_NODE_FILTERS_#", filtersStr.toString())
                .replaceAll("#_STACK_NAME_#", currentStackName);

        logger.info("Extended kibana dasbhoard url");
        logger.info(kibanaEndpoint + logDownloadCtx);
        if (shortenURL) {
            return shortenKibanaURL(logDownloadCtx);
        } else {
            return Optional.of(kibanaEndpoint + logDownloadCtx);
        }

    }

    /**
     * This method is responsible for building a Temporary Kibana dashboard for TestGrid jobs in K8S environments
     * (including Helm) which only includes filters under the namespace
     * @param currentNameSpace CloudFormation stack name
     * @param shortenURL weather to shorten URL or not, primarily used for unit testing purposes
     * @return Optional of dashboard link shortened to make it compatible with database column
     * restrictions
     */
    public Optional<String> buildK8STempDashBoard(String currentNameSpace, boolean shortenURL) {

        if (kibanaEndpoint == null || dashboardCtxFormat == null || instanceLogFilterFormat == null) {
            logger.warn("Kibana endpoint configuration not found in testgrid config. Server log view may not work!" +
                    "Kibana Endpoint : " + kibanaEndpoint +
                    "\nDashbboardCtxFormat : " + dashboardCtxFormat +
                    "\ninstanceLogFilterFormat : " + instanceLogFilterFormat);
            return Optional.empty();
        }

        allK8SNameSpaces.add(currentNameSpace);
        String instanceLogFilter;
        StringJoiner filtersStr = new StringJoiner(",");


        StringJoiner allLogsStr = new StringJoiner(",");
        StringJoiner allLogsJson = new StringJoiner(",");

        for (String nameSpace : allK8SNameSpaces) {
            logger.info(nameSpace);
            logger.info(allLogsFilterEncodedSectionK8S);
            allLogsStr.add(allLogsFilterEncodedSectionK8S.replaceAll("#_NAMESPACE_#", nameSpace));
            allLogsJson.add(allLogsFilterJsonSectionK8S.replaceAll("#_NAMESPACE_#", nameSpace));
        }
        allLogsFilter = allLogsFilter
                .replaceAll("#_ALL_LOGS_FILTER_SECTION_#", allLogsStr.toString())
                .replaceAll("#_REPEATABLE_ALL_LOGS_JSON_SECTION_#", allLogsJson.toString());
        filtersStr.add(allLogsFilter);
        String logDownloadCtx = dashboardCtxFormat.replace("#_NODE_FILTERS_#", filtersStr.toString())
                .replaceAll("#_NAMESPACE_#", currentNameSpace);

        logger.info("Extended kibana dasbhoard url");
        logger.info(kibanaEndpoint + logDownloadCtx);
        if (shortenURL) {
            return shortenKibanaURL(logDownloadCtx);
        } else {
            return Optional.of(kibanaEndpoint + logDownloadCtx);
        }

    }


    /**
     * Builds the final Permanent dashboard for helm deployments which includes filters for all pods
     * @param currentNameSpace CloudFormation stack name
     * @param shortenURL weather to shorten URL or not, primarily used for unit testing purposes
     * @return Optional of dashboard link shortened to make it compatible with database column
     *      * restrictions
     */
    public Optional<String> buildK8SPermaDashBoard(String currentNameSpace, boolean shortenURL) {

        if (kibanaEndpoint == null || dashboardCtxFormat == null || instanceLogFilterFormat == null) {
            logger.warn("Kibana endpoint configuration not found in TestGrid config. Server log view may not work!" +
                    "Kibana Endpoint : " + kibanaEndpoint +
                    "\nDashbboardCtxFormat : " + dashboardCtxFormat +
                    "\ninstanceLogFilterFormat : " + instanceLogFilterFormat);
            return Optional.empty();
        }


        String instanceLogFilter;
        StringJoiner filtersStr = new StringJoiner(",");
        allK8SNameSpaces.add(currentNameSpace);
        for (String nameSpace : allK8SNameSpaces) {
            ElasticSearchHelper esHelper = new ElasticSearchHelper();
            ArrayList<String> instanceMap = esHelper.getAllIndexes(nameSpace);
            for (String instances : instanceMap) {
                instanceLogFilter = instanceLogFilterFormatK8S
                        .replaceAll("#_INSTANCE_ID_#", instances)
                        .replaceAll("#_NAMESPACE_#", nameSpace);
                filtersStr.add(instanceLogFilter);
            }
        }

        StringJoiner allLogsStr = new StringJoiner(",");
        StringJoiner allLogsJson = new StringJoiner(",");

        for (String nameSpace : allK8SNameSpaces) {
            allLogsStr.add(allLogsFilterEncodedSectionK8S.replaceAll("#_NAMESPACE_#", nameSpace));
            allLogsJson.add(allLogsFilterJsonSectionK8S.replaceAll("#_NAMESPACE_#", nameSpace));
        }
        allLogsFilter = allLogsFilter
                .replaceAll("#_ALL_LOGS_FILTER_SECTION_#", allLogsStr.toString())
                .replaceAll("#_REPEATABLE_ALL_LOGS_JSON_SECTION_#", allLogsJson.toString());
        filtersStr.add(allLogsFilter);
        String logDownloadCtx = dashboardCtxFormat.replace("#_NODE_FILTERS_#", filtersStr.toString())
                .replaceAll("#_NAMESPACE_#", currentNameSpace);

        logger.info("Extended kibana dasbhoard url");
        logger.info(kibanaEndpoint + logDownloadCtx);
        if (shortenURL) {
            return shortenKibanaURL(logDownloadCtx);
        } else {
            return Optional.of(kibanaEndpoint + logDownloadCtx);
        }
    }

    /**
     * This method is responsible for shortening the final Kibana dashboard URL string by calling the
     * Kibana URL endpoint.
     *
     * If the operation is not successful it will return an {@link Optional} empty because having the original
     * URL saved in the database will throw an Exception for exceeding character count. instead the original URL
     * is being printed out to the logs so the developer can troubleshoot later.
     *
     * @param dashboardURL original URL string
     * @return {@link Optional<String>} with the shortened URL
     */
    private Optional<String> shortenKibanaURL(String dashboardURL) {

        String shortenUrl;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(kibanaEndpoint + "/shorten");
            StringEntity entity = new StringEntity("{\"url\" : \"" + dashboardURL + "\"}");
            request.addHeader("Content-Type", "application/json;charset=utf-8");
            request.addHeader("kbn-xsrf", "true");
            request.setEntity(entity);
            CloseableHttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String shortUrlId = EntityUtils.toString(httpResponse.getEntity());
                shortenUrl =  kibanaEndpoint + "/goto/" + shortUrlId;
                logger.info("Shortened URL");
                logger.info(shortenUrl);
                return Optional.of(shortenUrl);
            } else {
                logger.warn("Request to Kibana to retrieve shortened URL for dashboard returned status code:" +
                        httpResponse.getStatusLine().getStatusCode() + "\n. View logs at: " +
                        kibanaEndpoint + dashboardURL);
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.error("Exception occurred while calling for URL shortening API at " +
                    "Kibana Endpoint : " + kibanaEndpoint + "\n" +
                    "View logs at " + kibanaEndpoint + dashboardURL);
            return Optional.empty();
        }
    }
}
