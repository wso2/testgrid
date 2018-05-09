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

package org.wso2.testgrid.deployment.tinkerer.websocket;

import com.google.gson.Gson;
import org.wso2.testgrid.deployment.tinkerer.beans.OperationResponse;
import org.wso2.testgrid.deployment.tinkerer.utils.HttpSessionConfigurator;
import org.wso2.testgrid.deployment.tinkerer.utils.SessionManager;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * This class represents web socket endpoint to connect from agent.
 *
 * @since 1.0.0
 */
@ServerEndpoint(value = "/agent/{agentId}", configurator = HttpSessionConfigurator.class)
public class AgentSubscriptionEndpoint extends SubscriptionEndpoint {

    /**
     * Web socket onOpen use when agent connect to web socket url.
     *
     * @param session - Web socket Session.
     * @param agentId - agent Identifier.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("agentId") String agentId) {
        super.onOpen(session, agentId);
        SessionManager.getInstance().createAgentSession(agentId, session);
    }

    /**
     * Web socket onMessage use when agent sends a string message.
     *
     * @param session - Registered  session.
     * @param message - String message  which needs to send to peer.
     * @param agentId - agent Identifier.
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("agentId") String agentId) {
        super.onMessage(session, message, agentId);
        OperationResponse operationResponse = new Gson().fromJson(message, OperationResponse.class);
        SessionManager.getInstance().addOperationResponse(operationResponse);
    }

    /**
     * Web socket onMessage use when agent sends a byte message.
     *
     * @param session - Registered  session.
     * @param message - Byte message  which needs to send to peer.
     * @param agentId - agent Identifier.
     */
    @OnMessage
    public void onMessage(Session session, byte[] message, @PathParam("agentId") String agentId) {
        super.onMessage(session, message, agentId);
    }

    /**
     * Web socket onClose use to handle  socket connection close.
     *
     * @param session - Registered  session.
     * @param agentId - agent Identifier.
     * @param reason  - Status code for web-socket close.
     */
    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam("agentId") String agentId) {
        super.onClose(session, reason, agentId);
        SessionManager.getInstance().removeAgentSession(agentId);
    }

    /**
     * Web socket onError use to handle  socket connection error.
     *
     * @param session   - Registered  session.
     * @param throwable - Web socket exception.
     * @param agentId   - agent Identifier.
     */
    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("agentId") String agentId) {
        super.onError(session, throwable, agentId);
    }
}
