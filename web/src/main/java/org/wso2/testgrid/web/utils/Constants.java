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

import org.wso2.testgrid.common.TestGridConstants;

/**
 * This class will handle all the constants in the web-app.
 */
public class Constants extends TestGridConstants {
    public static final String JENKINS_TEMPLATE_JOB_URI =
            "/job/templates/job/velocityTemplateJob/config.xml";
    public static final String BLUE_OCEAN_URI = "/blue/organizations/jenkins";
    public static final String JENKINS_CRUMB_ISSUER_URI = "/crumbIssuer/api/json";
    public static final String CRUMB = "crumb";
    public static final String JENKINS_CRUMB_HEADER_NAME = "Jenkins-Crumb";
    public static final String WEBAPP_CONTEXT = "/testgrid/dashboard";

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

    /* Constants relates to SSO configurations. */
    public static final String LOGIN_URI = WEBAPP_CONTEXT + "/login";
    public static final String STATIC_DATA_URI = WEBAPP_CONTEXT + "/static";
    public static final String ACS_URI = WEBAPP_CONTEXT + "/api/acs";
    public static final String JKS_FILE_NAME = "wso2carbon.jks";
    public static final String SSO_DIRECTORY = "SSO";
    public static final String BACKEND_API_URI = WEBAPP_CONTEXT + "/api/";

    public static final String PROPERTYNAME_KEYSTORE_PASSWORD = "KeyStorePassword";
    public static final String PROPERTYNAME_PRIVATE_KEY_ALIAS = "PrivateKeyAlias";
    public static final String PROPERTYNAME_PRIVATE_KEY_PASSWORD = "PrivateKeyPassword";
    public static final String PROPERTYNAME_IDP_PUBLIC_KEY_ALIAS = "IdPPublicCertAlias";

    public static final String SSO_PROPERTY_FILE_NAME = "testgrid-sso.properties";
    public static final String SAML_BINDING_HTTP_POST = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    public static final String SAML_BINDING_HTTP_REDIRECT = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";
    public static final String HTTP_BINDING_HTTP_POST = "HTTP-POST";
    public static final String HTTP_BINDING_HTTP_REDIRECT = "HTTP-Redirect";
}
