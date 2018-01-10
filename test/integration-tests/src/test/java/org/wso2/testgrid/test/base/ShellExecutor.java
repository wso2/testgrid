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
 * Executor for shell commands.
 *
 * @since 1.0.0
 */
public class ShellExecutor {

    private static Logger logger = LoggerFactory.getLogger(ShellExecutor.class);

    // Specifies where the distribution should be unzipped, this is parsed an a system property from pom.xml
    public static final String TESTGRID_UNZIP_LOCATION = System.getProperty("project.build.directory", ".");
    public static final String TG_HOME_ENV_NAME = "TESTGRID_HOME";

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

        @Override public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }

    /**
     * Executes the Testgrid Jar with parsed arguments.
     *
     * @param jarLocation location of the executable Jar
     * @param args        execution arguments
     * @return state of the execution, whether success or fail
     */
    protected int executeJar(String jarLocation, String[] args) throws IOException, InterruptedException {

        Consumer<String> errorConsumer = logger::error;
        String cmdArray[] = { "java", "-jar", jarLocation };
        String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args)).toArray(String[]::new);
        ProcessBuilder builder = new ProcessBuilder();
        // Setting the TESTGRID_HOME environment variable
        builder.environment().put(TG_HOME_ENV_NAME, TESTGRID_UNZIP_LOCATION);

        if (getOSName().toLowerCase().startsWith("windows")) {
            // Execute Windows commands
            logger.error("Windows Support is not available!");
        } else {
            builder.command(cmdArgs);
        }

        builder.directory(new File(TESTGRID_UNZIP_LOCATION));
        Process process;
        process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getErrorStream(), errorConsumer);
        Executors.newSingleThreadExecutor().submit(streamGobbler);

        return process.waitFor();
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


