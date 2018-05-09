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

import com.google.gson.Gson;
import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.agent.OperationExecutor;
import org.wso2.testgrid.agent.beans.OperationRequest;
import org.wso2.testgrid.agent.beans.OperationResponse;
import org.wso2.testgrid.agent.listeners.OperationResponseListener;

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
 * This class holds web socket client implementation for agent.
 *
 * @since 1.0.0
 */
@javax.websocket.ClientEndpoint
public class ClientEndpoint implements OperationResponseListener {

    private static final Logger logger = LoggerFactory.getLogger(ClientEndpoint.class);
    private Session userSession = null;
    private URI endpointURI;
    private int retryAttempt = 0;
    private boolean hasClientConnected = false;
    private boolean isShuttingDown = false;

    public ClientEndpoint(URI endpointURI) {
        this.endpointURI = endpointURI;
    }

    public void connectClient() {
        try {
            ClientManager client = ClientManager.createClient();
            client.connectToServer(this, endpointURI);
            retryAttempt = 0;
            hasClientConnected = true;
        } catch (Exception e) {
            hasClientConnected = false;
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
        logger.info("Connected to web socket session: " + userSession.getId());
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
        logger.info("Closing web socket session: '" + userSession.getId() + "'. Code: " +
                reason.getCloseCode().toString() + " Reason: " + reason.getReasonPhrase());
        this.userSession = null;
        if (hasClientConnected && !isShuttingDown) {
            hasClientConnected = false;
            logger.info("Retrying to connect.");
            connectClient();
        }
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        logger.info("Operation received: " + message);
        OperationRequest operationRequest = new Gson().fromJson(message, OperationRequest.class);
        OperationExecutor operationExecutor = new OperationExecutor(this);
        operationExecutor.executeOperation(operationRequest);
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
        if (reason.getCloseCode().equals(CloseReason.CloseCodes.GOING_AWAY)) {
            isShuttingDown = true;
        }
        if (this.userSession != null) {
            try {
                this.userSession.close(reason);
            } catch (IOException e) {
                logger.error("Error on closing WS connection.", e);
            }
        }
    }

    @Override
    public void sendResponse(OperationResponse response) {
        // send message to web socket
        if (logger.isDebugEnabled()) {
            logger.debug("Sending message: " + response.toJSON());
        }
        sendMessage(response.toJSON());
    }
}
