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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.agent.OperationExecutor;
import org.wso2.testgrid.common.agentoperation.OperationRequest;
import org.wso2.testgrid.common.agentoperation.OperationSegment;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
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
public class ClientEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ClientEndpoint.class);
    private static volatile Map<String, OperationExecutor> operationExecutorHashMap = new HashMap<>();

    private ExecutorService executorService;
    private Session userSession = null;
    private Credentials credentials;
    private URI endpointURI;
    private int retryAttempt = 0;
    private boolean hasClientConnected = false;
    private boolean isShuttingDown = false;

    /**
     * Create {@link ClientEndpoint} instance.
     *
     * @param endpointURI - Web socket server endpoint.
     * @param userName    - Username required for basic auth.
     * @param password    - Password required for basic auth.
     */
    public ClientEndpoint(URI endpointURI, String userName, String password) {
        this.endpointURI = endpointURI;
        if (userName != null && password != null) {
            this.credentials = new Credentials(userName, password);
        } else {
            this.credentials = null;
        }
    }

    /**
     * Create web socket client connection using {@link ClientManager}.
     */
    public void connectClient() {
        ClientManager client = ClientManager.createClient();
        if (credentials != null) {
            client.getProperties().put(ClientProperties.CREDENTIALS, this.credentials);
        }
        try {
            client.connectToServer(this, endpointURI);
            retryAttempt = 0;
            hasClientConnected = true;
        } catch (DeploymentException | IOException e) {
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
        executorService = Executors.newFixedThreadPool(10);
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
        executorService.shutdown();
    }

    /**
     * Callback hook for Message Events.
     *
     * <p>This method will be invoked when a client send a message.
     *
     * @param message The text message.
     */
    @OnMessage
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
            justification = "No use of returned Future<?> from executor service submit().")
    public void onMessage(String message) {
        logger.info("Operation received: " + message);
        OperationRequest operationRequest = new Gson().fromJson(message, OperationRequest.class);
        OperationSegment operationSegment = new OperationSegment();
        operationSegment.setOperationId(operationRequest.getOperationId());
        operationSegment.setCode(operationRequest.getCode());
        OperationExecutor operationExecutor = new OperationExecutor(response -> {
            // send message to web socket
            if (logger.isDebugEnabled()) {
                logger.debug("Sending message: " + response.toJSON());
            }
            sendMessage(response.toJSON());
        });
        switch (operationRequest.getCode()) {
            case SHELL:
                operationExecutor.setOperationDetails(operationRequest, operationSegment);
                operationExecutorHashMap.put(operationRequest.getOperationId(), operationExecutor);
                executorService.submit(() -> operationExecutor.start());
                break;
            case PING:
                operationSegment.setResponse("ACK");
                operationSegment.setCompleted(true);
                executorService.submit(() -> operationExecutor.sendResponse(operationSegment));
                break;
            case ABORT:
                OperationExecutor operationExecutorAbort = operationExecutorHashMap.
                        get(operationRequest.getOperationId());
                if (operationExecutorAbort != null) {
                    operationExecutorAbort.setAbortExecution(true);
                }
                break;
            default:
                logger.warn("No operations found for the given command " + operationRequest.getRequest() +
                        " operation id " + operationRequest.getOperationId());
                break;
        }
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
}
