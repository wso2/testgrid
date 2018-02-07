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

import org.wso2.carbon.identity.sso.agent.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentX509Credential;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentX509KeyStoreCredential;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Implementation of context event listener for fetch SSO related details.
 */
public class SSOContextEventListener implements ServletContextListener {

    private static Logger logger = Logger.getLogger(SSOContextEventListener.class.getName());

    private static Properties properties = new Properties();

    /**
     * Fetch relevant details from the property file and JKS file.
     */
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        try {
            java.nio.file.Path ssoPropertyFilePath = Paths.
                    get(TestGridUtil.getTestGridHomePath(), "SSO", "testgrid-sso.properties");
            properties.load(Files.newInputStream(ssoPropertyFilePath));

            InputStream keyStoreInputStream;
                java.nio.file.Path configPath = Paths.
                        get(TestGridUtil.getTestGridHomePath(), "SSO", "wso2carbon.jks");
                keyStoreInputStream = Files.newInputStream(configPath);

            SSOAgentX509Credential credential =
                    new SSOAgentX509KeyStoreCredential(keyStoreInputStream,
                            properties.getProperty("KeyStorePassword").toCharArray(),
                            properties.getProperty("IdPPublicCertAlias"),
                            properties.getProperty("PrivateKeyAlias"),
                            properties.getProperty("PrivateKeyPassword").toCharArray());
            SSOAgentConfig config = new SSOAgentConfig();
            config.initConfig(properties);
            config.getSAML2().setSSOAgentX509Credential(credential);
            servletContextEvent.getServletContext().
                    setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);
        } catch (IOException | SSOAgentException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    /**
     * Get the properties of the sample
     * @return Properties
     */
    public static Properties getProperties() {
        return properties;
    }
}
