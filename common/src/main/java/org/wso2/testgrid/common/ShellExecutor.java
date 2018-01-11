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

package org.wso2.testgrid.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.CommandExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Responsible in executing given shell scripts
 *
 * @since 1.0
 */
public class ShellExecutor {

    private static Logger logger = LoggerFactory.getLogger(ShellExecutor.class);

    private File workingDirectory;

    public ShellExecutor(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * StreamGobbler to handle process builder output.
     *
     * @since 1.0
     */
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private StringBuilder stringBuilder = new StringBuilder();

        public StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            String line;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                }
            } catch (IOException e) {
                logger.error("Error occurred while generating the output ", e);
            }
        }

        /**
         * Get the value of the output streams.
         *
         * @return String value of the {@link StringBuilder}
         */
        public String getOutput() {
            return stringBuilder.toString();
        }
    }

    /**
     * Returns current working directory.
     *
     * @return working directory
     */
    public String getWorkingDirectory() {
        return workingDirectory.getAbsolutePath();
    }

    /**
     * Executes a shell command.
     *
     * @param command Command to execute
     * @return boolean for successful/unsuccessful command execution (success == true)
     * @throws CommandExecutionException if an {@link IOException} occurs while executing the command
     */
    public boolean executeCommand(String command) throws CommandExecutionException {

        if (logger.isDebugEnabled()) {
            logger.debug("Running shell command : " + command + ", from directory : " + workingDirectory.getName());
        }
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        try {
            if (workingDirectory != null && workingDirectory.exists()) {
                processBuilder.directory(workingDirectory);
            }
            Process process = processBuilder.start();

            StreamGobbler outputStreamGobbler = new StreamGobbler(process.getInputStream());
            StreamGobbler errorStreamGobbler = new StreamGobbler(process.getErrorStream());
            outputStreamGobbler.run();
            errorStreamGobbler.run();
            int status = process.waitFor();
            if (status > 0) {
                logger.error("Execution result : " + errorStreamGobbler.getOutput());
                return false;
            }
            logger.info("Execution result : " + outputStreamGobbler.getOutput());
            return true;
        } catch (IOException e) {
            throw new CommandExecutionException(
                    "Error occurred while executing the command '" + command + "', " + "from directory '"
                            + workingDirectory.getName() + "", e);
        } catch (InterruptedException e) {
            throw new CommandExecutionException(
                    "InterruptedException occurred while executing the command '" + command + "', " + "from directory '"
                            + workingDirectory.getName() + "", e);
        }
    }
}
