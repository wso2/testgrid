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

import java.io.IOException;
import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * This class represents common web socket endpoint to manage Remote Sessions
 */
public class SubscriptionEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionEndpoint.class);

    /**
     * Web socket onOpen use when client connect to web socket url
     *
     * @param session  - Web socket Session
     * @param clientId - client Identifier
     */
    public void onOpen(Session session, String clientId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Web Socket open from client with RemoteSession id: " + session.getId() +
                    " client id: " + clientId);
        }
    }

    /**
     * Web socket onMessage - When client sends a message
     *
     * @param session  - Registered  session.
     * @param message  - String Message which needs to send to peer
     * @param clientId - client Identifier
     */
    public void onMessage(Session session, String message, String clientId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received message from client for RemoteSession id: " + session.getId() +
                    " client id: " + clientId);
        }
    }

    /**
     * Web socket onMessage use When client sends a message
     *
     * @param session  - Registered  session.
     * @param clientId - client Identifier
     * @param message  - Byte Message which needs to send to peer
     */
    public void onMessage(Session session, byte[] message, String clientId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received message from client for RemoteSession id: " + session.getId() +
                    " client id: " + clientId);
        }
    }

    /**
     * Web socket onClose use to handle  socket connection close
     *
     * @param session  - Registered  session.
     * @param clientId - client Identifier
     * @param reason   - Status code for web-socket close.
     */
    public void onClose(Session session, CloseReason reason, String clientId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Web Socket closed due to " + reason.getReasonPhrase() + ", for session ID:" +
                    session.getId() + ", for request URI - " + session.getRequestURI() + " client  id: " + clientId);
        }
    }

    /**
     * Web socket onError use to handle  socket connection error
     *
     * @param session   - Registered  session.
     * @param throwable - Web socket exception
     * @param clientId  - client Identifier
     */
    public void onError(Session session, Throwable throwable, String clientId) {
        if (throwable instanceof IOException) {
            // This is normal if client terminated without graceful shutdown.
            logger.warn("Client connection lost. " + throwable.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error occurred in session ID: " + session.getId() + ", client id: " + clientId +
                        ", for request URI - " + session.getRequestURI() +
                        ", " + throwable.getMessage(), throwable);
            }
        } else {
            logger.error("Error occurred in session ID: " + session.getId() + ", client id: " + clientId +
                    ", for request URI - " + session.getRequestURI() + ", " + throwable.getMessage(), throwable);
        }
        try {
            if (session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Unexpected Error Occurred"));
            }
        } catch (IOException ex) {
            // This is normal if client terminated without graceful shutdown.
            logger.warn("Failed to disconnect the client. " + ex.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error details:", ex);
            }
        }
    }
}
