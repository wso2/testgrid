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

import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.ConfigurationContext;
import org.wso2.testgrid.common.util.ConfigurationContext.ConfigurationProperties;
import org.wso2.testgrid.web.bean.TestPlanRequest;
import org.wso2.testgrid.web.utils.Constants;

import javax.ws.rs.core.HttpHeaders;

import static org.wso2.testgrid.web.utils.Constants.JENKINS_TEMPLATE_JOB_URI;

import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.USER_AGENT;

/**
 * Class of JenkinsJobConfigurationProvider which retrieve an existing config.xml from Jenkins and
 * prepare new config.xml to new job.
 */
public class JenkinsJobConfigurationProvider {

    /**
     * Returns the configuration file for the testPlanRequest which can be used to create new Jenkins job.
     * @param testPlanRequest request for creating new test plan.
     * @return configuration file for Jenkins job.
     */
    public String getConfiguration(TestPlanRequest testPlanRequest) throws IOException, TestGridException {
        String template = retrieveConfigXmlFromJenkins();
        String[][] replacements = {
                {Constants.PRODUCT_NAME, testPlanRequest.getTestPlanName()},
                {Constants.PRODUCT_VERSION, "deprecated"}, //Version and channel is planned to be removed from TestGrid.
                {Constants.PRODUCT_CHANNEL, "deprecated"},
                {Constants.INFRASTRUCTURE_REPO, "\"" + testPlanRequest.getInfrastructure().getRepository() + "\""},
                {Constants.DEPLOYMENT_REPO, "\"" + testPlanRequest.getDeployment().getRepository() + "\""},
                {Constants.SCENARIO_REPO, "\"" + testPlanRequest.getScenarios().getRepository() + "\""},
                {Constants.INFRA_LOCATION, "\"" + testPlanRequest.getInfrastructure().getRepository() + "\""},
                {Constants.DEPLOYMENT_LOCATION, "\"" + testPlanRequest.getDeployment().getRepository() + "\""},
                {Constants.SCENARIOS_LOCATION, "\"" + testPlanRequest.getScenarios().getRepository() + "\""}
        };
        return mergeTemplate(template, replacements);
    }

    /**
     * Retrieves configuration file of the template job in Jenkins server.
     * @return configuration file of the template job.
     */
    private String retrieveConfigXmlFromJenkins() throws IOException, TestGridException {
        try {
            Content content =  Request.
                    Get(ConfigurationContext.getProperty(
                            ConfigurationProperties.JENKINS_HOST) + JENKINS_TEMPLATE_JOB_URI)
                    .addHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                            ConfigurationContext.getProperty(ConfigurationProperties.JENKINS_USER_AUTH_KEY))
                    .execute().returnContent();
            if (content != null) {
                return content.asString().trim();
            } else {
                throw new TestGridException("Configuration file received for Jenkins template job is null.");
            }
        } catch (IOException e) {
            throw new IOException("Request failed while accessing Jenkins template job: " + e.getMessage(), e);
        }
    }

    /**
     * Merge template terms with specific terms of the test plan request.
     * @param template configuration file of the template job.
     * @param replacements string 2D array containing terms to be replaced and with what to be replaced
     * @return configuration file suitable for the new test place request.
     */
    private String mergeTemplate(String template, String[][] replacements) {
        for (String[] replacement : replacements) {
            template = template.replace(replacement[0], replacement[1]);
        }
        return template;
    }
}
