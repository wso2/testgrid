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
package org.wso2.testgrid.agent.websocket;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 * This class holds web socket client implementation for agent
 */
@javax.websocket.ClientEndpoint
public class ClientEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ClientEndpoint.class);
    private Session userSession = null;
    private ClientHandler clientHandler;
    private URI endpointURI;
    private int retryAttempt = 0;
    private boolean clientConnected = false;

    public ClientEndpoint(URI endpointURI) {
        this.endpointURI = endpointURI;
    }

    public void connectClient() {
        try {
            ClientManager client = ClientManager.createClient();
            client.connectToServer(this, endpointURI);
            retryAttempt = 0;
            clientConnected = true;
        } catch (Exception e) {
            clientConnected = false;
            int delay = 5000;
            if (++retryAttempt > 3) {
                delay *= 3;
            } else {
                delay *= retryAttempt;
            }
            logger.warn("Failed to connect with Web Socket endpoint. " + e.getMessage());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    logger.warn("Retrying to connect with Web Socket endpoint. Attempt: " + retryAttempt);
                    connectClient();
                }
            }, delay);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        logger.info("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason      the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        logger.info("closing web socket session: '" + userSession.getId() + "'. Reason: " + reason.getReasonPhrase());
        this.userSession = null;
        if (this.clientHandler != null && clientConnected) {
            clientConnected = false;
            this.clientHandler.handleClose(reason);
        }
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.clientHandler != null) {
            this.clientHandler.handleMessage(message);
        }
    }

    /**
     * register client callback handler
     *
     * @param clientHandler the clientHandler which is going to add.
     */
    public void addClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    /**
     * Send a message.
     *
     * @param message the message which is going to send.
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * Close current connection.
     *
     * @param reason the reason which is going to send.
     */
    public void closeConnection(CloseReason reason) {
        if (this.userSession != null) {
            try {
                this.userSession.close(reason);
            } catch (IOException e) {
                logger.error("Error on closing WS connection.", e);
            }
        }
    }

    /**
     * Client handler interface to register call back.
     */
    public interface ClientHandler {

        void handleMessage(String message);

        void handleClose(CloseReason reason);
    }
}
