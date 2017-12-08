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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.CommandExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This Util class holds the common utility methods.
 *
 * @since 1.0.0
 */
public final class TestGridUtil {

    private static final Log log = LogFactory.getLog(TestGridUtil.class);
    private static final String TESTGRID_HOME_ENV = "TESTGRID_HOME";

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
     * Executes a command.
     * Used to execute a script with given deployment details as environment variables.
     *
     * @param command          Command to execute.
     * @param workingDirectory Directory the command is executed.
     * @param deployment       Deployment details for environment.
     * @return The output of script execution as a String.
     * @throws CommandExecutionException When there is an error executing the command.
     */
    public static String executeCommand(String command, File workingDirectory, Deployment deployment)
            throws CommandExecutionException {

        if (log.isDebugEnabled()) {
            log.debug("Running shell command : " + command + ", from directory : " + workingDirectory.getName());
        }

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process;

        try {
            if (workingDirectory != null && workingDirectory.exists()) {
                processBuilder.directory(workingDirectory);
            }
            if (deployment != null) {
                Map<String, String> environment = processBuilder.environment();
                for (Host host : deployment.getHosts()) {
                    environment.put(host.getLabel(), host.getIp());
                }
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
                return result;
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
        return Paths.get(testPlanHome, scenario.getName()).toAbsolutePath().toString();
    }

    /**
     * Returns the path of the test grid home.
     *
     * @return test grid home path
     */
    public static String getTestGridHomePath() {
        String testGridHome = EnvironmentUtil.getSystemVariableValue(TESTGRID_HOME_ENV);
        Path testGridHomePath = Paths.get(testGridHome);
        return testGridHomePath.toAbsolutePath().toString();
    }
}
