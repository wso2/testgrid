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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.sso.agent.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentX509Credential;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.web.api.SSOService;

import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Implementation of context event listener which contains the SSO related details (X509 Credential of TestGrid,
 * configurations in SSO property file) that will be used when generating SAML request.
 */
public class SSOContextEventListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(SSOService.class);
    private static Properties properties = new Properties();

    /**
     * Fetch relevant details from
     * {@link org.wso2.testgrid.web.utils.Constants#SSO_PROPERTY_FILE_NAME} property file and
     * {@link org.wso2.testgrid.web.utils.Constants#JKS_FILE_NAME} JKS file.
     */
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        SSOConfigurationReader ssoConfigurationReader = new SSOConfigurationReader();
        try {
            SSOAgentX509Credential credential = ssoConfigurationReader.getIdPX509Credential();
            SSOAgentConfig config = new SSOAgentConfig();
            config.initConfig(ssoConfigurationReader.getSSOProperties());
            config.getSAML2().setSSOAgentX509Credential(credential);
            servletContextEvent.getServletContext().
                    setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);
        } catch (SSOAgentException | TestGridException e) {
            logger.error(e.getMessage(), e);
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
