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

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.wso2.testgrid.web.utils.Constants.BLUE_OCEAN_URI;
import static org.wso2.testgrid.web.utils.Constants.JENKINS_HOME;
import static org.wso2.testgrid.web.utils.Constants.JENKINS_USER_AUTH_KEY;

import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.USER_AGENT;

/**
 * Implementation of creating pipeline jobs from config.xml files.
 */
public class PipelineManager {
    private static final Logger logger = LoggerFactory.getLogger(PipelineManager.class);

    /**
     * Creates new pipeline job in Jenkins server by calling its REST API.
     * @param configXml configuration file for the new job.
     * @param jobName name for the new job.
     * @return URL to check the status of the new job.
     */
    public String createNewPipelineJob(String configXml, String jobName) throws Exception {
        HttpResponse response = Request.Post(JENKINS_HOME + "/createItem?name=" + jobName)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Authorization", "Basic " + JENKINS_USER_AUTH_KEY)
                .addHeader("Jenkins-Crumb", getCrumb())
                .addHeader("Content-Type", "application/xml")
                .bodyString(configXml, ContentType.DEFAULT_TEXT)
                .execute().returnResponse();

        if (response.getCode() == HttpStatus.SC_OK) {
            return getJobSpecificUrl(jobName);
        } else {
            logger.error("Jenkins server error for creating job " + jobName + " " +
                    response.getCode());
            throw new Exception("Can not create new job in Jenkins.");
        }
    }

    /**
     * Returns crumb value which has to be used when making POST requests with Jenkins.
     *
     * @return Crumb value.
     */
    private String getCrumb() throws IOException {
            String response =  Request.Get(JENKINS_HOME + "/crumbIssuer/api/json")
                    .addHeader("User-Agent", USER_AGENT)
                    .addHeader("Authorization", "Basic " + JENKINS_USER_AUTH_KEY)
                    .execute().returnContent().asString().trim();
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.get("crumb").toString();
    }

    /**
     * Generates the URL which can be used to see the status of the job.
     * @param jobName name of the job.
     * @return URL of the job.
     */
    private String getJobSpecificUrl(String jobName) {
        return JENKINS_HOME + BLUE_OCEAN_URI + "/" + jobName;
    }
}
