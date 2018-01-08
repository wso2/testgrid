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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.exception.TestGridLoggingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * This Util class holds the common utility methods.
 *
 * @since 1.0.0
 */
public final class TestGridUtil {

    private static final Logger logger = LoggerFactory.getLogger(TestGridUtil.class);
    private static final String TESTGRID_HOME_ENV = "TESTGRID_HOME";

    /**
     * Executes a command.
     * Used for creating the infrastructure and deployment.
     *
     * @param command Command to execute
     * @return boolean for successful/unsuccessful command execution
     */
    public static boolean executeCommand(String command, File workingDirectory) throws CommandExecutionException {

        if (logger.isDebugEnabled()) {
            logger.debug("Running shell command : " + command + ", from directory : " + workingDirectory.getName());
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
                logger.info("Execution result : " + result);
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

        if (logger.isDebugEnabled()) {
            logger.debug("Running shell command : " + command + ", from directory : " + workingDirectory.getName());
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
                logger.info("Execution result : " + result);
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
     * Returns the path of the test grid home.
     *
     * @return test grid home path
     */
    public static String getTestGridHomePath() {
        String testGridHome = EnvironmentUtil.getSystemVariableValue(TESTGRID_HOME_ENV);
        Path testGridHomePath = Paths.get(testGridHome);
        return testGridHomePath.toAbsolutePath().toString();
    }

    /**
     * Returns the directory location where the test run artifacts resides.
     *
     * @param testPlan test plan for getting the test run artifacts location
     * @return path of the test run artifacts
     */
    public static Path getTestRunArtifactsDirectory(TestPlan testPlan) throws TestGridLoggingException {
        try {
            DeploymentPattern deploymentPattern = testPlan.getDeploymentPattern();
            Product product = deploymentPattern.getProduct();
            int testRunNumber = testPlan.getTestRunNumber();

            String productDir = StringUtil.concatStrings(product.getName(), "_", product.getVersion()
                    , "_", product.getChannel());
            String deploymentDir = deploymentPattern.getName();
            String infraDir = getInfraParamUUID(testPlan.getInfraParameters());

            return Paths.get(productDir, deploymentDir, infraDir, String.valueOf(testRunNumber));
        } catch (TestGridException e) {
            throw new TestGridLoggingException(
                    "Error in getting the test run artifacts directory location " +
                    "([PRODUCT_NAME_VERSION_CHANNEL]/[DEPLOYMENT_PATTERN_NAME]/[INFRA_PARAM_UUID]/[TEST_RUN_NUMBER]");
        }
    }

    /**
     * Returns a UUID specific to the infra parameters.
     *
     * @param infraParams infra parameters to get the UUID
     * @return UUID specific to the infra parameters
     */
    private static String getInfraParamUUID(String infraParams) throws TestGridException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> sortedMap = new TreeMap<>();

            // Get sorted map from the JSON object string
            Map<String, Object> infraParamMap = mapper
                    .readValue(infraParams, new TypeReference<Map<String, String>>() {
                    });

            // Copy values to the tree map to get the values sorted
            infraParamMap.forEach(sortedMap::put);
            String stringToConvertToUUID = sortedMap.toString().toLowerCase(Locale.ENGLISH);
            return UUID.nameUUIDFromBytes(stringToConvertToUUID.getBytes(Charset.defaultCharset())).toString();
        } catch (JsonParseException e) {
            throw new TestGridException(StringUtil.concatStrings("Error in parsing JSON ", infraParams), e);
        } catch (JsonMappingException e) {
            throw new TestGridException(StringUtil.concatStrings("Error in mapping JSON ", infraParams), e);
        } catch (IOException e) {
            throw new TestGridException(StringUtil.concatStrings("IO Exception when trying to map JSON ",
                    infraParams), e);
        }
    }
}
