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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * This Util class holds the common utility methods.
 */
public final class Utils {

    private static final String YAML_EXTENSION = ".yaml";

    /**
     * Executes a command.
     * Used for creating the infrastructure and deployment.
     *
     * @param command Command to execute
     * @return boolean for successful/unsuccessful command execution
     */
    public static boolean executeCommand(String command, File workingDirectory) {

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        Process process;
        try {
            if (workingDirectory !=null && workingDirectory.exists()) {
                processBuilder.directory(workingDirectory);
            }
            process = processBuilder.start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            System.out.println(result);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This Utility method is used to access Environment variables.
     */
    public static String readEnvironmentVariable(String variable) {
        return System.getenv(variable);
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
