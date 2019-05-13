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
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Responsible in executing given shell scripts
 *
 * @since 1.0
 */
public class ShellExecutor {

    private static Logger logger = LoggerFactory.getLogger("Shell");

    private Path workingDirectory;

    public ShellExecutor() {
        //todo: Set this to the test-run workspace
        this(Paths.get(TestGridUtil.getTestGridHomePath()));
    }

    public ShellExecutor(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * StreamGobbler to handle process builder output.
     *
     * @since 1.0
     */
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                    .forEach(consumer);
        }
    }

    /**
     * Returns current working directory.
     *
     * @return relative path of the working directory
     */
    public String getWorkingDirectory() {
        return workingDirectory.toString();
    }

    /**
     * Executes a shell command.
     *
     * @param command Command to execute
     * @return boolean for successful/unsuccessful command execution (success == true)
     * @throws CommandExecutionException if an {@link IOException} occurs while executing the command
     */
    public int executeCommand(String command) throws CommandExecutionException {

        if (logger.isDebugEnabled()) {
            logger.debug("Running shell command : " + command + ", from working directory : " + workingDirectory);
        }
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            if (workingDirectory != null) {
                File workDirectory = workingDirectory.toFile();
                if (workDirectory.exists()) {
                    processBuilder.directory(workDirectory);
                }
            }
            Process process = processBuilder.start();

            StreamGobbler outputStreamGobbler = new StreamGobbler(process.getInputStream(), msg -> {
                msg = reduceLogVerbosity(msg);
                logger.info(msg);
            });
            StreamGobbler errorStreamGobbler = new StreamGobbler(process.getErrorStream(), msg -> {
                Consumer<String> c = !msg.startsWith("+ ") ? logger::error : logger::info; // handle 'set -o xtrace'
                msg = reduceLogVerbosity(msg);
                c.accept(msg);
            });

            executor.execute(outputStreamGobbler);
            executor.execute(errorStreamGobbler);

            return process.waitFor();

        } catch (IOException e) {
            throw new CommandExecutionException(
                    "Error occurred while executing the command '" + command + "', " + "from directory '"
                            + workingDirectory.toString(), e);
        } catch (InterruptedException e) {
            throw new CommandExecutionException(
                    "InterruptedException occurred while executing the command '" + command + "', " + "from directory '"
                            + workingDirectory.toString(), e);
        } finally {
            executor.shutdownNow();
        }
    }

    private String reduceLogVerbosity(String msg) {
        return msg.replace("INFO  [org.wso2.carbon.automation.extensions.servers.utils.ServerLogReader] - ",
                "[Server] - ");
    }

    /**
     * Executes a shell command.
     *
     * @param command Command to execute
     * @param environment environment variables to be set before execution of the script
     * @return boolean for successful/unsuccessful command execution (success == true)
     *
     * @throws CommandExecutionException if an {@link IOException} occurs while executing the command
     */
    public int executeCommand(String command, Map<String, String> environment) throws CommandExecutionException {

        if (logger.isDebugEnabled()) {
            logger.debug("Running shell command : " + command + ", from directory : " + workingDirectory.toString());
        }
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            if (workingDirectory != null) {
                File workDirectory = workingDirectory.toFile();
                if (workDirectory.exists()) {
                    processBuilder.directory(workDirectory);
                }
            }
            if (environment.size() > 0) {
                processBuilder.environment().putAll(environment);
            }
            Process process = processBuilder.start();

            StreamGobbler outputStreamGobbler = new StreamGobbler(process.getInputStream(), logger::info);
            StreamGobbler errorStreamGobbler = new StreamGobbler(process.getErrorStream(), logger::error);

            executor.execute(outputStreamGobbler);
            executor.execute(errorStreamGobbler);

            return process.waitFor();

        } catch (IOException e) {
            throw new CommandExecutionException(
                    "Error occurred while executing the command '" + command + "', " + "from directory '"
                            + workingDirectory.toString(), e);
        } catch (InterruptedException e) {
            throw new CommandExecutionException(
                    "InterruptedException occurred while executing the command '" + command + "', " + "from directory '"
                            + workingDirectory.toString(), e);
        } finally {
            executor.shutdownNow();
        }
    }
}
