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
 *
 */

package org.wso2.testgrid.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.TestGridException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Implementation of a dynamic property file reader, which can be used to read properties from TestGrid property files.
 * Ex: Read values from testgrid build-output property file.
 * This also includes enums for possible defined properties in each Testgrid property files.
 */
public class PropertyFileReader {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationContext.class);

    /**
     * Retrieve property from the property file.
     * @param property Property key as in the property file.
     * @return Property value read from property file.
     */
    public String getProperty(Enum property, String propertyFilePath) throws TestGridException {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(propertyFilePath))) {
            properties.load(inputStream);
            return properties.getProperty(property.toString());
        } catch (IOException e) {
            String msg =
                    "Error while trying to read " + propertyFilePath + "to retrieve property " + property.toString();
            logger.error(msg);
            throw new TestGridException(msg, e);
        }
    }

    /**
     * Defines the Testgrid build output properties.
     */
    public enum BuildOutputProperties {

        /**
         * Git revision of the source used to build the product.
         */
        GIT_REVISION("GIT_REVISION"),

        /**
         * Git repository and branch of the product.
         */
        GIT_LOCATION("GIT_LOCATION");

        private String propertyName;

        /**
         * Sets the name of the property.
         *
         * @param propertyName name of the property
         */
        BuildOutputProperties(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public String toString() {
            return this.propertyName;
        }
    }
}
