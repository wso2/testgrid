/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.testgrid.common.util;


/**
 * Utility class for obtaining environment variable values.
 *
 * @since 1.0.0
 */
public class EnvironmentUtil {

    /**
     * Returns the system property value or environment variable value (highest priority for environment variable)
     * for the given key.
     *
     * @param systemVariableKey key of the system property or environment variable
     * @return system property value or environment variable value for the given key
     */
    public static String getSystemVariableValue(String systemVariableKey) {
        String envVariableValue = System.getenv(systemVariableKey);
        return envVariableValue != null ? envVariableValue : System.getProperty(systemVariableKey);
    }
}
