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

package org.wso2.testgrid.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.agent.listeners.OperationResponseListener;
import org.wso2.testgrid.common.agentoperation.OperationRequest;
import org.wso2.testgrid.common.agentoperation.OperationSegment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class executes operations.
 *
 * @since 1.0.0
 */
public class OperationExecutor extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(OperationExecutor.class);

    private OperationResponseListener operationResponseListener;
    private volatile boolean abortExecution = false;
    private OperationRequest operationRequest;
    private OperationSegment operationSegment;

    /**
     * Constructor of the OperationExecutor to initialize object
     *
     * @param operationResponseListener     Response write buffer
     */
    public OperationExecutor(OperationResponseListener operationResponseListener) {
        this.operationResponseListener = operationResponseListener;
        this.operationSegment = new OperationSegment();
        this.operationRequest = new OperationRequest();
    }

    /**
     * Set default operation details
     *
     * @param operationRequest      The operation request
     * @param operationSegment      The operation segment
     */
    public void setOperationDetails(OperationRequest operationRequest, OperationSegment operationSegment) {
        this.operationRequest = operationRequest;
        this.operationSegment = operationSegment;
    }
    /**
     * Execute command send from the Tinkerer on current agent
     *
     */
    public void run() {
        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", operationRequest.getRequest());
            process = processBuilder.start();
            try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), UTF_8);
                InputStreamReader errorStreamReader = new InputStreamReader(process.getErrorStream(), UTF_8)) {
                readStreamline(inputStreamReader, errorStreamReader, process, operationSegment);
            }
        } catch (IOException e) {
            String message = "Error occurred when executing command: " + operationRequest.getRequest();
            logger.error(message, e);
            operationSegment.setExitValue(1);
            operationSegment.setResponse(message);
            operationResponseListener.sendResponse(operationSegment);
        }
    }

    /**
     * Read result of InputStreamReader which is output of process execution
     *
     * @param inputStreamReader     Output stream of the process execution
     * @param errorStreamReader     Error stream of the process execution
     * @param process               Running process
     * @param operationSegment     Operation response to be send
     * @throws IOException
     */
    private void readStreamline(InputStreamReader inputStreamReader, InputStreamReader errorStreamReader,
                                Process process, OperationSegment operationSegment)
            throws IOException {
        try (BufferedReader inputBufferedReader = new BufferedReader(inputStreamReader);
             BufferedReader errorBufferedReader = new BufferedReader(errorStreamReader)) {
            readBuffer(inputBufferedReader, operationSegment, process, false);
            readBuffer(errorBufferedReader, operationSegment, process, true);
        }
    }

    /**
     * Read streaming buffer of process to get execution result
     *
     * @param reader                BufferedReader of the process
     * @param operationSegment     Response to send to Tinkerer
     * @param process               Command running process
     * @param isError               false if input stream, true if error stream
     */
    private void readBuffer(BufferedReader reader, OperationSegment operationSegment, Process process,
                            boolean isError) {
        String line;
        String output = "";
        int lineCount = 0;
        int maximumLineCount = 10;
        try {
            while ((line = reader.readLine()) != null) {
                synchronized (this) {
                    if (this.abortExecution) {
                        process.destroy();
                        process.waitFor();
                        operationSegment.setCompleted(true);
                        operationSegment.setExitValue(process.exitValue());
                        operationSegment.setResponse(output);
                        operationResponseListener.sendResponse(operationSegment);
                        return;
                    }
                    lineCount++;
                    if (lineCount >= maximumLineCount) {
                        output = output.concat(line);
                        lineCount = 0;
                        operationSegment.setCompleted(false);
                        operationSegment.setExitValue(0);
                        operationSegment.setResponse(output);
                        operationResponseListener.sendResponse(operationSegment);
                        output = "";
                    } else {
                        output = output.concat(line.concat(System.lineSeparator()));
                    }
                }
            }
            if (isError) {
                operationSegment.setCompleted(true);
                operationSegment.setExitValue(process.exitValue());
            }
            operationSegment.setResponse(output);
            operationResponseListener.sendResponse(operationSegment);
        } catch (IOException e) {
            logger.warn("Operation abort for operation id " + operationSegment.getOperationId(), e);
        } catch (InterruptedException e) {
            logger.error("Error while reading line for operation id " + operationSegment.getOperationId(), e);
        }
    }

    /**
     * Set current thread to abort executing operation
     *
     * @param abortExecution        Abort state
     */
    public synchronized void setAbortExecution(boolean abortExecution) {
        this.abortExecution = abortExecution;
    }

    /**
     * Send response back to the tinkerer
     *
     * @param operationSegment      Response segment to send
     */
    public void sendResponse(OperationSegment operationSegment) {
        operationResponseListener.sendResponse(operationSegment);
    }
}
