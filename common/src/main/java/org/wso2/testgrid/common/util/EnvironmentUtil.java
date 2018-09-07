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
package org.wso2.testgrid.common.util;

import java.util.Locale;

/**
 * Utility class for obtaining environment variable values.
 *
 * @since 1.0.0
 */
public class EnvironmentUtil {
    /**
     * types of Operating Systems
     */
    public enum OSType {
        Windows, MacOS, Linux, Other
    };

    private static OSType detectedOS;
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

    /**
     * Get the type of OS the current running environment
     *
     * @return OS type
     */
    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String foundOS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((foundOS.indexOf("mac") >= 0) || (foundOS.indexOf("darwin") >= 0)) {
                detectedOS = OSType.MacOS;
            } else if (foundOS.indexOf("win") >= 0) {
                detectedOS = OSType.Windows;
            } else if (foundOS.indexOf("nux") >= 0) {
                detectedOS = OSType.Linux;
            } else {
                detectedOS = OSType.Other;
            }
        }
        return detectedOS;
    }
}
