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

package org.wso2.testgrid.remote.session.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.remote.session.utils.HttpSessionConfigurator;

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
 */
@ServerEndpoint(value = "/client/{deviceId}", configurator = HttpSessionConfigurator.class)
public class CLISubscriptionEndpoint extends SubscriptionEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(CLISubscriptionEndpoint.class);

    /**
     * Web socket onOpen use when client connect to web socket url
     *
     * @param session    - Registered session.
     * @param deviceId   - Device Identifier
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("deviceId") String deviceId) {

    }

    /**
     * Web socket onMessage use when client sends a string message
     *
     * @param session    - Registered  session.
     * @param deviceId   - Device Identifier
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam ("deviceId") String deviceId) {
        super.onMessage(session, message, deviceId);
    }

    /**
     * Web socket onMessage use when client sends a byte message
     *
     * @param session    - Registered  session.
     * @param deviceId   - Device Identifier
     * @param message    - Byte message which needs to send to peer
     */
    @OnMessage
    public void onMessage(Session session, byte[] message, @PathParam ("deviceId") String deviceId) {
        super.onMessage(session, message, deviceId);
    }

    /**
     * Web socket onClose use to handle  socket connection close
     *
     * @param session    - Registered  session.
     * @param deviceId   - Device Identifier
     * @param reason     - Status code for web-socket close.
     */
    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam ("deviceId") String deviceId) {
        super.onClose(session, reason, deviceId);
    }

    /**
     * Web socket onError use to handle  socket connection error
     *
     * @param session    - Registered  session.
     * @param throwable  - Web socket exception
     * @param deviceId   - Device Identifier
     */
    @OnError
    public void onError(Session session, Throwable throwable, @PathParam ("deviceId") String deviceId) {
        super.onError(session, throwable, deviceId);
    }
}
