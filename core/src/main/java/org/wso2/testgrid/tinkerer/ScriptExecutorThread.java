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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;


/**
 * Thread to stream data from tinkerer and dump result into a file.
 */
public class ScriptExecutorThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExecutorThread.class);

    public static final int MAX_BUFFER_IDLE_TIME = 900000;  // Maximum waiting time to update message queue

    private Response response;
    private Path testGridShellStreamPath;
    private String operationId;
    private volatile boolean isCompleted;
    private volatile int exitValue;
    private int segmentCount;

    /**
     * Initialize streaming thread with operation details.
     *
     * @param operationId          The operation id
     * @param response              Response from the tinkerer
     * @param filePath              File path to store result
     */
    public ScriptExecutorThread(String operationId, Response response, Path filePath) {
        this.operationId = operationId;
        this.response = response;
        this.testGridShellStreamPath = filePath;
        this.isCompleted = false;
        this.exitValue = 0;
        this.segmentCount = 0;
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
        long initTime = Calendar.getInstance().getTimeInMillis();
        long currentTime;
        while ((chunk = input.read()) != null) {
            synchronized (this) {
                currentTime = Calendar.getInstance().getTimeInMillis();
                if (initTime + MAX_BUFFER_IDLE_TIME < currentTime) {
                    logger.warn("Execution time out for operation " + this.operationId);
                    break;
                }
                operationSegment = gson.fromJson(chunk, OperationSegment.class);
                writeDataToFile(operationSegment, this.segmentCount + 1);
                this.segmentCount++;
            }
        }
        synchronized (this) {
            this.isCompleted = true;
            this.exitValue = operationSegment.getExitValue();
            logger.info("Streaming success with exit value " + operationSegment.getExitValue() + " for operation " +
                    this.operationId);
        }
    }

    /**
     * Write command execution result into a file.
     *
     * @param operationSegment      The response to write
     * @param segmentId             Segment id to write
     */
    private void writeDataToFile(OperationSegment operationSegment, int segmentId) {
        String fileToWrite = Paths.get(this.testGridShellStreamPath.toString(),
                this.operationId.concat("_".concat(Integer.toString(segmentId)).concat(".txt"))).toString();
        try {
            FileUtil.saveFile(operationSegment.getResponse(), fileToWrite, false);
        } catch (TestGridException e) {
            logger.error("Unable Write data into a file for operation id " + operationSegment.getOperationId(), e);
        }
    }

    /**
     * Get the exit value of the current operation.
     *
     * @return      The exit value
     */
    public synchronized int getExitValue() {
        return exitValue;
    }

    /**
     * Get operation execution status is completed or not.
     *
     * @return      Execution completed of not completed
     */
    public synchronized boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Inner class to generate jersey client with GenericType.
     */
    static class ChunkObject extends GenericType<ChunkedInput<String>> { }

    /**
     * Get current read Segment count
     *
     * @return  Current segment count
     */
    public synchronized int getSegmentCount() {
        return segmentCount;
    }
}
