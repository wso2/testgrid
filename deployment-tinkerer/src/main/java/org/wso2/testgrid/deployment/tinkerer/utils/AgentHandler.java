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

package org.wso2.testgrid.deployment.tinkerer.utils;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.agentoperation.OperationRequest;
import org.wso2.testgrid.common.agentoperation.OperationSegment;
import org.wso2.testgrid.deployment.tinkerer.SessionManager;
import org.wso2.testgrid.deployment.tinkerer.exception.AgentHandleException;


import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.websocket.Session;

/**
 * Handle sending shell script operation to the agent and stream result back to the test runner.
 */
public class AgentHandler implements Observer {
    private static final Logger logger = LoggerFactory.getLogger(AgentHandler.class);
    private ChunkedOutput<String> output;
    private OperationRequest operationRequest;
    private String testPlanId;
    private String instanceName;

    /**
     * Agent handler constructor to initialize streaming object.
     *
     * @param output ChunkedOutput to write back result
     * @param operationRequest Request from the test runner
     * @param testPlanId    The test plan id
     * @param instanceName  The instant name
     */
    public AgentHandler(ChunkedOutput<String> output, OperationRequest operationRequest, String testPlanId,
                        String instanceName) {
        this.output = output;
        this.operationRequest = operationRequest;
        this.testPlanId = testPlanId;
        this.instanceName = instanceName;
    }

    /**
     * Send command to the Agent and wait for response from the agent
     *
     * @throws AgentHandleException
     */
    public void startSendCommand() throws AgentHandleException {
        SessionManager sessionManager = SessionManager.getInstance();
        Agent agent = sessionManager.getAgent(testPlanId, instanceName);
        if (agent != null && sessionManager.hasAgentSession(agent.getAgentId())) {
            Session wsSession = sessionManager.getAgentSession(agent.getAgentId());
            try {
                sendMessageToAgent(operationRequest, wsSession, agent.getAgentId());
                logger.info("Generate new message queue with id: " + operationRequest.getOperationId() + " code: " +
                        operationRequest.getCode() + " command: " + operationRequest.getRequest());

            } catch (IOException e) {
                throw new AgentHandleException("Error while sending command to agent " + operationRequest.getRequest() +
                        " on agent " + agent.getAgentId() + " instant name " +
                        this.instanceName, e);
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
        SessionManager sessionManager = SessionManager.getInstance();
        Agent agent = sessionManager.getAgent(testPlanId, instanceName);
        OperationSegment resultOperation = new OperationSegment();
        resultOperation.setResponse("");
        try {
            // Dequeue all messages with relevant operation id
            OperationSegment operationSegment = sessionManager.
                    dequeueOperationQueueMessages(this.operationRequest.getOperationId());
            // Append dequeue result to the result
            resultOperation.setResponse(
                    resultOperation.getResponse().concat(operationSegment.getResponse()));
            resultOperation.setOperationId(this.operationRequest.getOperationId());
            resultOperation.setCode(operationSegment.getCode());
            resultOperation.setMetaData(operationSegment.getMetaData());
            // Check if operation execution completed
            if (operationSegment.getCompleted()) {
                logger.info("Operation execution completed for operation id " + this.operationRequest.getOperationId()
                        + " on " + agent.getAgentId() + " for test plan " + testPlanId);
                resultOperation.setCompleted(true);
                resultOperation.setExitValue(operationSegment.getExitValue());
                sessionManager.removeOperationQueueMessages(this.operationRequest.getOperationId());
                this.output.write(resultOperation.toJSON() + "\r\n");
                this.output.close();
            }
            // Send response only if it contain response
            if (!resultOperation.getResponse().equals("")) {
                logger.info("Sending result segment to test runner " + agent.getAgentId() +
                        " for test plan " + testPlanId);
                this.output.write(resultOperation.toJSON() + "\r\n");
                resultOperation.setResponse("");
            }
        } catch (IOException e) {
            logger.error("Error while executing command " + operationRequest.getRequest() +
                    " on agent " + agent.getAgentId() + " for test plan " + testPlanId + " instant name " +
                    this.instanceName, e);
            try {
                this.output.close();
            } catch (IOException errorOutput) {
                logger.error("Error while close output connection " + operationRequest.getRequest() +
                        " on agent " + agent.getAgentId() + " for test plan " + testPlanId + " instant name " +
                        this.instanceName, errorOutput);
            }
        }
    }

    /**
     * Send command to the agent through the given web socket session
     *
     * @param operationRequest      The operation request
     * @param session               The session to agent
     * @param agentId       The agent id
     * @throws IOException
     */
    public void sendMessageToAgent (OperationRequest operationRequest,
                                    Session session, String agentId) throws IOException {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.addNewOperationQueue(operationRequest.getOperationId(), operationRequest.getCode(), agentId);
        session.getBasicRemote().sendText(operationRequest.toJSON());
    }
}
