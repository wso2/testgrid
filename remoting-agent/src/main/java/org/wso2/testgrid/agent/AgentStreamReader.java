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
import org.wso2.testgrid.common.agentoperation.OperationSegment;
import org.wso2.testgrid.common.exception.CommandExecutionException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Execute command on the agent and start two thread to read input
 * and error stream logs from the agent.
 */
public class AgentStreamReader {
    private static final Logger logger = LoggerFactory.getLogger(AgentStreamReader.class);
    private static volatile Map<String, AgentStreamObserver> observerHashMap = new HashMap<>();

    private OperationResponseListener operationResponseListener;
    private String operationId;

    /**
     * Initiate AgentStreamReader with default configurations
     *
     * @param operationResponseListener         Listener to send message back to the Tinkerer
     * @param operationId                       The operation id
     */
    public AgentStreamReader(OperationResponseListener operationResponseListener, String operationId) {
        this.operationResponseListener = operationResponseListener;
        this.operationId = operationId;
    }

    /**
     * Execute command on the agent and start stream reader thread.
     *
     * @param command               Command to execute
     * @throws CommandExecutionException        Command execution exceptions
     */
    public void executeCommand(String command) throws CommandExecutionException {
        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            process = processBuilder.start();
            AgentObservable agentObservable = new AgentObservable();
            AgentStreamObserver agentStreamObserver = new AgentStreamObserver(this.operationResponseListener,
                    this.operationId, process, agentObservable);
            observerHashMap.put(this.operationId, agentStreamObserver);
            agentObservable.addObserver(agentStreamObserver);
            AgentStreamGobbler errorGobbler = new AgentStreamGobbler(StreamResponse.StreamType.ERROR,
                    process.getErrorStream(), agentObservable);
            AgentStreamGobbler outputGobbler = new AgentStreamGobbler(StreamResponse.StreamType.INPUT,
                    process.getInputStream(), agentObservable);
            outputGobbler.start();
            errorGobbler.start();
        } catch (IOException e) {
            throw new CommandExecutionException("Error while starting the process for the operation " +
                    this.operationId, e);
        }
    }

    /**
     * Get AgentStreamObserver from the hash map by given operation id.
     *
     * @param operationId       Operation id of the observer
     * @return                  Instance of AgentStreamObserver
     */
    public static AgentStreamObserver getAgentStreamObserverById(String operationId) {
        return observerHashMap.get(operationId);
    }

    /**
     * Remove observer from the observer hash map.
     *
     * @param operationId       The operation id of the observer to remove
     */
    public static void removeAgentStreamObserverById(String operationId) {
        observerHashMap.remove(operationId);
    }

    /**
     * Send response back to the tinkerer.
     *
     * @param operationSegment      Response segment to send
     */
    public void sendResponse(OperationSegment operationSegment) {
        operationResponseListener.sendResponse(operationSegment);
    }

}
