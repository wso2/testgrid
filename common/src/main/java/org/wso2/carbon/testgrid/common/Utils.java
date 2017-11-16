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

package org.wso2.carbon.testgrid.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.exception.CommandExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * This Util class holds the common utility methods.
 */
public final class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);
    private static final String YAML_EXTENSION = ".yaml";

    /**
     * Executes a command.
     * Used for creating the infrastructure and deployment.
     *
     * @param command Command to execute
     * @return boolean for successful/unsuccessful command execution
     */
    public static boolean executeCommand(String command, File workingDirectory) throws CommandExecutionException {

        if (log.isDebugEnabled()) {
            log.debug("Running shell command : " + command + ", from directory : " + workingDirectory.getName());
        }

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process;

        try {
            if (workingDirectory != null && workingDirectory.exists()) {
                processBuilder.directory(workingDirectory);
            }
            process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.getProperty("line.separator"));
                }
                String result = builder.toString();
                log.info("Execution result : " + result);
                return true;
            } catch (IOException e) {
                throw new CommandExecutionException("Error occurred while fetching execution output of the command '"
                        + command + "'", e);
            }
        } catch (IOException e) {
            throw new CommandExecutionException("Error occurred while executing the command '" + command + "', " +
                    "from directory '" + workingDirectory.getName() + "", e);
        }
    }

    /**
     * This Utility method is used to return the folder location of the TestScenario.
     */
    public static String getTestScenarioLocation(TestScenario scenario, String testPlanHome) {
        return Paths.get(testPlanHome, scenario.getSolutionPattern()).toAbsolutePath().toString();
    }

    /**
     * This Utility method is used to check if the given file is a yaml file.
     */
    public static boolean isYamlFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            return fileName.endsWith(YAML_EXTENSION);
        }
        return false;
    }
}
