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

import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.TestGridUtil;

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
    private static InputStream inputStream;
    private static boolean isFileAccessed;
    static {
        try {
            Path configPath = Paths.get(TestGridUtil.getTestGridHomePath(), "testgrid-web-config.properties");
            inputStream = Files.newInputStream(configPath);
            isFileAccessed = true;
        } catch (IOException e) {
            isFileAccessed = false;
        }
    }
    private static Properties properties = new Properties();

    /**
     * Retrieve property from the property file.
     * @param property Property key as in the property file.
     * @return Property value read from property file.
     */
    public static String getProperty(String property) throws TestGridException {
        if (!isFileAccessed) {
            throw new TestGridException("Unable to access property file.");
        }
        if (inputStream != null) {
            try {
                properties.load(inputStream);
                return properties.getProperty(property);
            } catch (IOException e) {
                throw new TestGridException("Can not read property " + property + " in property file.");
            }
        } else {
            throw new TestGridException("Can not get property " + property + " .Property file is null.");
        }
    }
}
