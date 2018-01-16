/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.web.operation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.web.utils.ConfigurationContext;

import static org.wso2.testgrid.web.utils.Constants.BLUE_OCEAN_URI;
import static org.wso2.testgrid.web.utils.Constants.JENKINS_CRUMB_ISSUER_URI;

import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.USER_AGENT;

/**
 * Implementation of Jenkins PipelineManager which do functions relates to pipeline jobs in Jenkins using its APIs.
 */
public class JenkinsPipelineManager {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsPipelineManager.class);

    /**
     * Creates new pipeline job in Jenkins server by calling its REST API.
     * @param configXml configuration file for the new job.
     * @param jobName name for the new job.
     * @return URL to check the status of the new job.
     */
    public String createNewPipelineJob(String configXml, String jobName) throws TestGridException, IOException {
        HttpResponse response = Request
                .Post(ConfigurationContext.getProperty("JENKINS_HOST") + "/createItem?name=" + jobName)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Authorization", "Basic " +
                        ConfigurationContext.getProperty("JENKINS_USER_AUTH_KEY"))
                .addHeader("Jenkins-Crumb", getCrumb())
                .addHeader("Content-Type", "application/xml")
                .bodyString(configXml, ContentType.DEFAULT_TEXT)
                .execute().returnResponse();

        if (response.getCode() == HttpStatus.SC_OK) {
            return getJobSpecificUrl(jobName);
        } else {
            logger.error("Jenkins server error for creating job " + jobName + " " +
                    response.getCode());
            throw new TestGridException("Can not create new job in Jenkins. Received " + response.getCode());
        }
    }

    /**
     * Returns crumb value which has to be used when making POST requests with Jenkins.
     *
     * @return Crumb value.
     */
    private String getCrumb() throws IOException, TestGridException {
        try {
            String response = Request.Get(ConfigurationContext.getProperty("JENKINS_HOST") + JENKINS_CRUMB_ISSUER_URI)
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Authorization", "Basic " +
                            ConfigurationContext.getProperty("JENKINS_USER_AUTH_KEY"))
                    .execute().returnContent().asString().trim();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readValue(response, JsonNode.class);
            if (!jsonNode.get("crumb").isNull()) {
                return jsonNode.get("crumb").toString().replace("\"", "");
            } else {
                throw new TestGridException("Crumb value is null. Can not continue.");
            }
        } catch (IOException e) {
            throw new IOException("Can not get crumb value from Jenkins " + e.getMessage());
        }
    }

    /**
     * Generates the URL which can be used to see the status of the job.
     * @param jobName name of the job.
     * @return URL of the job.
     */
    private String getJobSpecificUrl(String jobName) throws TestGridException {
        return ConfigurationContext.getProperty("JENKINS_HOST") + BLUE_OCEAN_URI + "/" + jobName + "/activity";
    }
}
