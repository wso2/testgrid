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

package org.wso2.testgrid.deployment.tinkerer;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.agentoperation.OperationRequest;
import org.wso2.testgrid.common.agentoperation.OperationSegment;
import org.wso2.testgrid.deployment.tinkerer.exception.AgentHandleException;


import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.websocket.Session;

/**
 * Handle sending shell script operation to the agent and stream result back to the test runner.
 */
public class AgentStreamHandler implements Observer {
    private static final Logger logger = LoggerFactory.getLogger(AgentStreamHandler.class);
    private ChunkedOutput<String> streamingBuffer;
    private OperationRequest operationRequest;
    private String agentId;

    /**
     * Default constructor to handle command in agent.
     */
    public AgentStreamHandler() {
    }
    /**
     * Agent handler constructor to initialize streaming object.
     *
     * @param streamingBuffer ChunkedOutput buffer to write back result
     * @param operationRequest Request from the test runner
     */
    public AgentStreamHandler(ChunkedOutput<String> streamingBuffer, OperationRequest operationRequest,
                              String agentId) {
        this.streamingBuffer = streamingBuffer;
        this.operationRequest = operationRequest;
        this.agentId = agentId;
    }

    /**
     * Send command to the Agent and wait for response from the agent.
     *
     * @throws AgentHandleException
     */
    public void startSendCommand() throws AgentHandleException {
        SessionManager sessionManager = SessionManager.getInstance();

        Agent agent = sessionManager.getAgent(this.agentId);
        if (agent != null && sessionManager.hasAgentSession(agent.getAgentId())) {
            Session wsSession = sessionManager.getAgentSession(agent.getAgentId());
            try {
                wsSession.getBasicRemote().sendText(operationRequest.toJSON());
                sessionManager.addNewOperationQueue(this.operationRequest.getOperationId(),
                        this.operationRequest.getCode(), this.agentId);
                logger.info("Generate new message queue with id: " + operationRequest.getOperationId() + " code: " +
                        operationRequest.getCode() + " command: " + operationRequest.getRequest());
            } catch (IOException e) {
                throw new AgentHandleException("Error while sending command to agent " + operationRequest.getRequest() +
                        " on agent " + agent.getAgentId(), e);
            }
        }
    }

    /**
     * This is the observer to check if new messages are available.
     * If new messages available for this object with given operation id then, send result data back to the test plan
     * executor.
     *
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        String foundOperationId = (String) arg;
        if (foundOperationId.equals(this.operationRequest.getOperationId())) {
            SessionManager sessionManager = SessionManager.getInstance();
            OperationSegment resultOperation = new OperationSegment();
            resultOperation.setResponse("");
            try {
                // Dequeue all messages with relevant operation id
                OperationSegment operationSegment = sessionManager.
                        dequeueOperationQueueMessages(this.operationRequest.getOperationId());
                // Append dequeue result to the result
                if (operationSegment == null) {
                    logger.info("No operation found for operation id " + this.operationRequest.getOperationId()
                            + " on " + this.agentId);
                    return;
                }
                resultOperation.setResponse(
                        resultOperation.getResponse().concat(operationSegment.getResponse()));
                resultOperation.setOperationId(this.operationRequest.getOperationId());
                resultOperation.setCode(operationSegment.getCode());
                resultOperation.setMetaData(operationSegment.getMetaData());
                // Check if operation execution completed
                if (operationSegment.getCompleted()) {
                    logger.info("Operation execution completed for operation id " +
                            this.operationRequest.getOperationId() + " on " + this.agentId);
                    resultOperation.setCompleted(true);
                    resultOperation.setExitValue(operationSegment.getExitValue());
                    sessionManager.removeOperationQueueMessages(this.operationRequest.getOperationId());
                    sessionManager.getAgentObservable().deleteObserver(this);
                    this.streamingBuffer.write(resultOperation.toJSON() + "\r\n");
                    this.streamingBuffer.close();
                    return;
                }
                // Send response only if it contain response
                if (!resultOperation.getResponse().equals("")) {
                    logger.debug("Sending result segment to test runner " + this.agentId);
                    this.streamingBuffer.write(resultOperation.toJSON() + "\r\n");
                    resultOperation.setResponse("");
                }
            } catch (IOException e) {
                logger.warn("Error while writing result to the output. " + operationRequest.getRequest() +
                        " on agent " + this.agentId, e);
                abortOperation(this.operationRequest.getOperationId(), this.agentId);
                try {
                    this.streamingBuffer.close();
                } catch (IOException errorOutput) {
                    logger.error("Error while close output connection " + operationRequest.getRequest() +
                            " on agent " + this.agentId, errorOutput);
                }
            }
        }
    }

    /**
     * Send command to agent to abort executing process.
     *
     * @param operationId       The operation id to abort
     * @param agentId           Agent id
     * @return                  True if success, else false.
     */
    public boolean abortOperation(String operationId, String agentId) {
        SessionManager sessionManager = SessionManager.getInstance();
        if (sessionManager.getOperationRequest(operationId) != null) {
            Session session = sessionManager.getAgentSession(agentId);
            if (session != null) {
                OperationRequest abortOperationRequest = new OperationRequest();
                abortOperationRequest.setOperationId(operationId);
                abortOperationRequest.setCode(OperationRequest.OperationCode.ABORT);
                try {
                    session.getBasicRemote().sendText(abortOperationRequest.toJSON());
                } catch (IOException e) {
                    logger.error("Error occurred while sending abort operation to agent " + agentId, e);
                    SessionManager.getOperationQueueMap().get(operationId).setOperationAsCompleted(1);
                    SessionManager.getAgentObservable().notifyObservable(null);
                    return false;
                }
                return true;
            } else {
                // If socket connection break remove the message queue
                logger.info("No session found to sending abort message to agent " + agentId);
                SessionManager.getOperationQueueMap().get(operationId).setOperationAsCompleted(1);
                SessionManager.getAgentObservable().notifyObservable(null);
                return false;
            }

        } else {
            return false;
        }
    }
}
