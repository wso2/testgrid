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

package org.wso2.testgrid.test.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Executor for shell commands
 */
public class ShellExecutor {

    private static Logger logger = LoggerFactory.getLogger(ShellExecutor.class);

    /**
     * StreamGobbler to handle process builder output
     */
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }

    /**
     * Executed the TG Jar with parsed arguments
     * @param jarLocation location of the executable Jar
     * @param args execution arguments
     * @return state of the execution, whether success or fail
     * @throws IntegrationTestException
     */
    protected int executeJar(String jarLocation, String[] args) throws IntegrationTestException {

        Consumer<String> errorConsumer = logger::error;
        String cmdArray[] = { "java", "-jar", jarLocation };
        String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args)).toArray(String[]::new);
        String homeDirectory = System.getProperty(Constants.TG_UNZIP_LOCATION, ".");
        ProcessBuilder builder = new ProcessBuilder();
        // Setting the TESTGRID_HOME environment variable
        builder.environment().put(Constants.TG_HOME_ENV_NAME, System.getProperty(Constants.TG_UNZIP_LOCATION));

        if (getOSName().toLowerCase().startsWith("windows")) {
            // Execute Windows commands
            logger.error("Windows Support is not available!");
        } else {
            builder.command(cmdArgs);
        }

        builder.directory(new File(homeDirectory));
        Process process;
        int execState;
        try {
            process = builder.start();
            StreamGobbler streamGobbler = new StreamGobbler(process.getErrorStream(), errorConsumer);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            execState = process.waitFor();
        } catch (InterruptedException e) {
            logger.error("Interrupted Exception occurred while executing the Testgrid JAR", e);
            throw new IntegrationTestException("Interrupted Exception occurred while executing the Testgrid JAR", e);
        } catch (IOException e) {
            logger.error("IO Exception occurred while executing the Testgrid JAR", e);
            throw new IntegrationTestException("IO Exception occurred while executing the Testgrid JAR", e);
        }
        return execState;
    }

    /**
     * Return the system property value of os.name.
     * System.getProperty("os.name").
     *
     * @return Operating System name
     */
    private static String getOSName() {
        return System.getProperty("os.name");
    }
}


