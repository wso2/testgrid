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

import org.wso2.testgrid.deployment.tinkerer.utils.HttpSessionConfigurator;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * This class represents web socket endpoint to manage Remote Sessions from CLI tool.
 *
 * @since 1.0.0
 */
@ServerEndpoint(value = "/client/{clientId}", configurator = HttpSessionConfigurator.class)
public class CLISubscriptionEndpoint extends SubscriptionEndpoint {

    /**
     * Web socket onOpen use when client connect to web socket url.
     *
     * @param session    - Registered session.
     * @param clientId   - Client Identifier
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) {
        super.onOpen(session, clientId);
    }

    /**
     * Web socket onMessage use when client sends a string message.
     *
     * @param session    - Registered  session.
     * @param clientId   - Client Identifier
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam ("clientId") String clientId) {
        super.onMessage(session, message, clientId);
    }

    /**
     * Web socket onMessage use when client sends a byte message.
     *
     * @param session    - Registered  session.
     * @param clientId   - Client Identifier.
     * @param message    - Byte message which needs to send to peer.
     */
    @OnMessage
    public void onMessage(Session session, byte[] message, @PathParam ("clientId") String clientId) {
        super.onMessage(session, message, clientId);
    }

    /**
     * Web socket onClose use to handle  socket connection close.
     *
     * @param session    - Registered  session.
     * @param clientId   - Client Identifier.
     * @param reason     - Status code for web-socket close.
     */
    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam ("clientId") String clientId) {
        super.onClose(session, reason, clientId);
    }

    /**
     * Web socket onError use to handle  socket connection error.
     *
     * @param session    - Registered  session.
     * @param throwable  - Web socket exception.
     * @param clientId   - Client Identifier.
     */
    @OnError
    public void onError(Session session, Throwable throwable, @PathParam ("clientId") String clientId) {
        super.onError(session, throwable, clientId);
    }
}
