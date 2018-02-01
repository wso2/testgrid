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
package org.wso2.testgrid.logging.plugins;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

/**
 * Looks up the log file location based on product test directory, deployment pattern
 * and the infrastructure combination (operating system, database, jdk) which are
 * created in the runtime
 *
 * @since 1.0.0
 */
@Plugin(name = "path", category = StrLookup.CATEGORY)
public class LogFilePathLookup implements StrLookup {
    private static String logFilePath = "testgrid.log";

    @Override
    public String lookup(String key) {
        return null;
    }

    @Override
    public String lookup(LogEvent logEvent, String key) {
        return logFilePath;
    }

    /**
     * Sets the log file path.
     *
     * @param logFilePath log file path
     */
    public static void setLogFilePath(String logFilePath) {
        LogFilePathLookup.logFilePath = logFilePath;
    }
}
