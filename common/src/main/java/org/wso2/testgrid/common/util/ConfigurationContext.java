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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * Implementation of configuration context which will contain functions relates with property file.
 * Example: Retrieve property value by property key.
 */
public class ConfigurationContext {
    private static Optional<ConfigurationContext> configurationContext;
    private static boolean initialized = false;
    private static Properties properties;
    public static final Logger logger = LoggerFactory.getLogger(ConfigurationContext.class);

    /**
     * Retrieve property from the property file.
     * @param property Property key as in the property file.
     * @return Property value read from property file.
     */
    public static String getProperty(String property) {
        return properties.getProperty(property);
    }

    /**
     * Construct nullable singleton Configuration Context.
     */
    private ConfigurationContext() {
        properties = new Properties();
        try {
            Path configPath = Paths.get(TestGridUtil.getTestGridHomePath(), "config.properties");
            InputStream inputStream = Files.newInputStream(configPath);
            properties.load(inputStream);
            initialized = true;
        } catch (IOException e) {
            logger.error("Error occurred while trying to read config.properties", e);
        }
    }

    /**
     * Get singleton configuration context.
     * On creation, return an Optional.empty if file access failed
     *
     * @return configurationContext containing properties
     */
    public static Optional<ConfigurationContext> getInstance() {
        if (!initialized) {
            configurationContext = Optional.of(new ConfigurationContext());
            return initialized ? configurationContext : Optional.empty();
        }
        return configurationContext;
    }
}
