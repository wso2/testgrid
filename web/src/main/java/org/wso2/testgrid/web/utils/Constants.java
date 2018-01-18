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

package org.wso2.testgrid.web.utils;

/**
 * This class will handle all the constants in the web-app.
 */
public class Constants {
    public static final String JENKINS_TEMPLATE_JOB_URI =
            "/job/templates/job/velocityTemplateJob/config.xml";
    public static final String BLUE_OCEAN_URI = "/blue/organizations/jenkins";
    public static final String JENKINS_CRUMB_ISSUER_URI = "/crumbIssuer/api/json";
    public static final String CRUMB = "crumb";
    public static final String JENKINS_HOST = "JENKINS_HOST";
    public static final String JENKINS_CRUMB_HEADER_NAME = "Jenkins-Crumb";
    public static final String JENKINS_USER_AUTH_KEY = "JENKINS_USER_AUTH_KEY";

    /* Terms used in Jenkins template job. */
    public static final String PRODUCT_NAME = "$productName";
    public static final String PRODUCT_CHANNEL = "$productChannel";
    public static final String PRODUCT_VERSION = "$productVersion";
    public static final String INFRASTRUCTURE_REPO = "$infrastructureRepo";
    public static final String DEPLOYMENT_REPO = "$deploymentRepo";
    public static final String SCENARIO_REPO = "$scenariosRepo";
    public static final String INFRA_LOCATION = "$infraLocation";
    public static final String DEPLOYMENT_LOCATION = "$deploymentLocation";
    public static final String SCENARIOS_LOCATION = "$scenariosLocation";

}
