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
import org.wso2.testgrid.common.agentoperation.AgentObservable;
import org.wso2.testgrid.common.agentoperation.Operation;
import org.wso2.testgrid.common.agentoperation.OperationSegment;

import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

/**
 * Observer class to wait till response and send response as segment to the
 * Tinkerer.
 */
public class AgentStreamObserver implements Observer {

    private static final int MAX_LINE_COUNT = 10;
    private static final long MAX_EXECUTION_TIME_OUT = 500;
    private static final Logger logger = LoggerFactory.getLogger(AgentStreamObserver.class);

    private String operationId;
    private OperationResponseListener operationResponseListener;
    private Process process;
    private int lineCount;
    private AgentObservable agentObservable;
    private volatile boolean oneProcessCompleted = false;
    private volatile long initTime;
    private volatile String shellLog = "";
    private volatile boolean abortExecution = false;

    /**
     * Initialize object constructor
     *
     * @param operationResponseListener         Send response back to the Tinkerer
     * @param operationId                       The operation id
     * @param process                           Command execution process
     * @param agentObservable                   Observable agent
     */
    public AgentStreamObserver(OperationResponseListener operationResponseListener, String operationId,
                               Process process, AgentObservable agentObservable) {
        this.operationResponseListener = operationResponseListener;
        this.operationId = operationId;
        this.lineCount = 0;
        this.initTime = Calendar.getInstance().getTimeInMillis();
        this.process = process;
        this.agentObservable = agentObservable;
    }

    /**
     * Handle response from the command execution thread and send response as segment
     * To the tinkerer
     *
     * @param o                 Observable object
     * @param arg               Shell execution result
     */
    @Override
    public synchronized void update(Observable o, Object arg) {
        String shellResult = (String) arg;
        this.lineCount++;
        if (arg == null || this.abortExecution) {
            if (this.abortExecution && process.isAlive()) {
                process.destroy();
            }
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                logger.error("Error while waiting to stop process for operation id " + this.operationId);
            }
            // Response send back after first thread completed
            if (!oneProcessCompleted) {
                OperationSegment finalOperationSegment = new OperationSegment();
                finalOperationSegment.setOperationId(this.operationId);
                finalOperationSegment.setCompleted(true);
                finalOperationSegment.setExitValue(process.exitValue());
                finalOperationSegment.setResponse(this.shellLog);
                finalOperationSegment.setCode(OperationSegment.OperationCode.SHELL);
                this.agentObservable.deleteObserver(this);
                AgentStreamReader.removeObserverHashMapById(this.operationId);
                this.operationResponseListener.sendResponse(finalOperationSegment);
            }
            this.oneProcessCompleted = true;
            return;
        }
        if (lineCount >= MAX_LINE_COUNT ||
                initTime + MAX_EXECUTION_TIME_OUT < Calendar.getInstance().getTimeInMillis()) {
            initTime = Calendar.getInstance().getTimeInMillis();
            this.shellLog = this.shellLog.concat(shellResult);
            lineCount = 0;
            OperationSegment operationSegment = new OperationSegment();
            operationSegment.setOperationId(this.operationId);
            operationSegment.setCompleted(false);
            operationSegment.setExitValue(0);
            operationSegment.setResponse(this.shellLog);
            operationSegment.setCode(Operation.OperationCode.SHELL);
            operationResponseListener.sendResponse(operationSegment);
            this.shellLog = "";
        } else {
            this.shellLog = this.shellLog.concat(shellResult.concat(System.lineSeparator()));
        }
    }

    /**
     * Set abort execution state
     *
     * @param abortExecution        True if abort execution
     */
    public synchronized void setAbortExecution(boolean abortExecution) {
        this.abortExecution = abortExecution;
    }

}
