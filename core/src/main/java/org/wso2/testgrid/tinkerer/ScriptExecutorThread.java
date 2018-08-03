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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.tinkerer;

import com.google.gson.Gson;
import org.glassfish.jersey.client.ChunkedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.agentoperation.OperationSegment;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.FileUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import static org.wso2.testgrid.common.TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY;

/**
 * Thread to stream data from tinkerer and dump result into a file.
 */
public class ScriptExecutorThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExecutorThread.class);

    private Response response;
    private Path testGridShellStreamPath;
    private String operationId;
    private boolean isCompleted;
    private int exitValue;

    /**
     * Initialize streaming thread with operation details.
     *
     * @param operationId          The operation id
     * @param response              Response from the tinkerer
     */
    public ScriptExecutorThread(String operationId, Response response) {
        this.operationId = operationId;
        this.response = response;
        this.testGridShellStreamPath = Paths.get(System.getProperty(TESTGRID_HOME_SYSTEM_PROPERTY), "shell");
        this.isCompleted = false;
        this.exitValue = 0;
        // Create folder to store result in testgrid home if it not already exist
        if (!Files.exists(this.testGridShellStreamPath)) {
            boolean fileStatus = new File(this.testGridShellStreamPath.toString()).mkdirs();
            if (!fileStatus) {
                logger.error("Unable to create new directory for operation id " + this.operationId);
            }
        }
    }

    /**
     * Start running thread to read result which sent from the tinkerer.
     */
    @Override
    public void run() {
        Gson gson = new Gson();
        ChunkedInput<String> input = this.response.readEntity(new ChunkObject());
        String chunk;
        OperationSegment operationSegment = new OperationSegment();
        while ((chunk = input.read()) != null) {
            operationSegment = gson.fromJson(chunk, OperationSegment.class);
            writeDataToFile(operationSegment);
        }
        this.isCompleted = true;
        this.exitValue = operationSegment.getExitValue();
        logger.info("Streaming success with exit value " + operationSegment.getExitValue() + " for operation " +
        this.operationId);
    }

    /**
     * Write command execution result into a file.
     *
     * @param operationSegment      The response to write
     */
    private void writeDataToFile(OperationSegment operationSegment) {
        try {
            FileUtil.saveFile(operationSegment.getResponse(), this.testGridShellStreamPath.toString(),
                    operationSegment.getOperationId().concat(".log"));
        } catch (TestGridException e) {
            logger.error("Unable Write data into a file for operation id " + operationSegment.getOperationId(), e);
        }
    }

    /**
     * Get the exit value of the current operation.
     *
     * @return      The exit value
     */
    public int getExitValue() {
        return exitValue;
    }

    /**
     * Get is operation running completed in current operation.
     *
     * @return      Execution completed of not completed
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Inner class to generate jersey client with GenericType.
     */
    static class ChunkObject extends GenericType<ChunkedInput<String>> { }
}
