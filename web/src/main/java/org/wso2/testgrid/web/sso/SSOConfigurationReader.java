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
package org.wso2.testgrid.web.sso;

import org.wso2.carbon.identity.sso.agent.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentX509Credential;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentX509KeyStoreCredential;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.web.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * This class will handle all IO related tasks with external files.
 * Reads {@link Constants#SSO_PROPERTY_FILE_NAME} and {@link Constants#JKS_FILE_NAME}.
 */
public class SSOConfigurationReader {

    /**
     * Read {@link Constants#JKS_FILE_NAME} JKS file and return X509Credential of Identity Provider.
     * @return X509Credential of Identity Server.
     * @throws TestGridException if an error occur while reading JKS file.
     */
    public SSOAgentX509Credential getIdPX509Credential() throws TestGridException {
        Properties properties = getSSOProperties();
        try {
            java.nio.file.Path configPath = Paths.
                    get(TestGridUtil.getTestGridHomePath(), Constants.SSO_DIRECTORY, Constants.JKS_FILE_NAME);

            InputStream keyStoreInputStream = Files.newInputStream(configPath);
            SSOAgentX509Credential credential;

            credential = new SSOAgentX509KeyStoreCredential(keyStoreInputStream,
                    properties.getProperty(Constants.PROPERTYNAME_KEYSTORE_PASSWORD).toCharArray(),
                    properties.getProperty(Constants.PROPERTYNAME_IDP_PUBLIC_KEY_ALIAS),
                    properties.getProperty(Constants.PROPERTYNAME_PRIVATE_KEY_ALIAS),
                    properties.getProperty(Constants.PROPERTYNAME_PRIVATE_KEY_PASSWORD).toCharArray());
            return credential;
        } catch (IOException | SSOAgentException e) {
            throw new TestGridException("Error occurred while reading JKS file to fetch IdP's credential.", e);
        }
    }

    /**
     * Load properties from {@link Constants#SSO_PROPERTY_FILE_NAME}.
     * @return All properties in the property file.
     */
    Properties getSSOProperties() throws TestGridException {
        Properties properties = new Properties();
        try {
            java.nio.file.Path ssoPropertyFilePath = Paths.
                    get(TestGridUtil.getTestGridHomePath(), Constants.SSO_DIRECTORY,
                            Constants.SSO_PROPERTY_FILE_NAME);
            properties.load(Files.newInputStream(ssoPropertyFilePath));
        } catch (IOException e) {
            throw new TestGridException("Error occurred while reading SSO property file.", e);
        }
        return properties;
    }
}
