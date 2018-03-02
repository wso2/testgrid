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

package org.wso2.testgrid.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Implementation of configuration context which will contain functions relates with property file.
 * Example: Retrieve property value by property key.
 */
public class ConfigurationContext {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationContext.class);
    private static Properties properties;

    static {
        try {
            properties = new Properties();
            Path configPath = Paths.get(TestGridUtil.getTestGridHomePath(), TestGridConstants.TESTGRID_CONFIG_FILE);
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            logger.error(StringUtil.concatStrings(
                    "Error while trying to read ", TestGridConstants.TESTGRID_CONFIG_FILE));
        }
    }

    /**
     * Retrieve property from the property file.
     * @param property Property key as in the property file.
     * @return Property value read from property file.
     */
    public static String getProperty(Enum property) {
        return properties.getProperty(property.toString());
    }

    /**
     * Defines the testgrid configuration properties.
     */
    public enum ConfigurationProperties {

        /**
         * Database URL propertyName
         */
        DB_URL("DB_URL"),

        /**
         * Database user propertyName
         */
        DB_USER("DB_USER"),

        /**
         * Property for assword of database user
         */
        DB_USER_PASS("DB_USER_PASS"),

        /**
         * Jenkins host propertyName
         */
        JENKINS_HOST("JENKINS_HOST"),

        /**
         * Property for Jenkins user authorization key
         */
        JENKINS_USER_AUTH_KEY("JENKINS_USER_AUTH_KEY"),

        /**
         * SSO login URL propertyName
         */
        SSO_LOGIN_URL("SSO_LOGIN_URL"),

        /**
         * Property to check if SSO is enabled
         */
        ENABLE_SSO("ENABLE_SSO");

        private String propertyName;

        /**
         * Sets the name of the property.
         *
         * @param propertyName name of the property
         */
        ConfigurationProperties(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public String toString() {
            return this.propertyName;
        }
    }
}
