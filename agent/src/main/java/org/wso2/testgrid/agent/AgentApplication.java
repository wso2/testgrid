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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.agent.beans.OperationRequest;
import org.wso2.testgrid.agent.beans.OperationResponse;
import org.wso2.testgrid.agent.listners.OperationResponseListner;
import org.wso2.testgrid.agent.websocket.ClientEndpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.UUID;
import javax.websocket.CloseReason;

/**
 * Agent bootstrap class
 */
public class AgentApplication implements OperationResponseListner {

    private static final Logger logger = LoggerFactory.getLogger(AgentApplication.class);
    private static final String AGENT_PROP_FILE = File.separator + "opt" + File.separator + "testgrid" +
            File.separator + "agent-config.properties";

    private ClientEndpoint clientEndPoint;

    public static void main(String args[]) {
        new AgentApplication().startService();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void startService() {
        String wsEndpoint = "ws://localhost:8080/remote-session/v0.9/agent/";
        String agentId = "orphan:" + UUID.randomUUID().toString();

        Properties prop = new Properties();
        InputStream input = null;
        try {
            if (new File(AGENT_PROP_FILE).exists()) {
                logger.info("Loading configurations from: " + AGENT_PROP_FILE);
                input = new FileInputStream(AGENT_PROP_FILE);
                prop.load(input);
                wsEndpoint = prop.getProperty("wsEndpoint");
                if (prop.containsKey("testPlanId") && prop.containsKey("nodeId")) {
                    agentId = prop.getProperty("testPlanId") + ":" + prop.getProperty("nodeId");
                }
            } else {
                logger.info("Using default configurations.");
            }
        } catch (IOException ex) {
            logger.error("Error occurred when reading prop file: " + ex.getMessage(), ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("Error occurred when closing prop file stream: " + e.getMessage(), e);
                }
            }
        }

        try {
            logger.info("Agent ID: " + agentId);
            // open web socket
            clientEndPoint = new ClientEndpoint(new URI(wsEndpoint + agentId));

            clientEndPoint.addClientHandler(new ClientEndpoint.ClientHandler() {
                @Override
                public void handleMessage(String message) {
                    logger.info("Operation received: " + message);
                    OperationRequest operationRequest = new Gson().fromJson(message, OperationRequest.class);
                    OperationExecutor operationExecutor = new OperationExecutor(AgentApplication.this);
                    operationExecutor.executeOperation(operationRequest);
                }

                @Override
                public void handleClose(CloseReason reason) {
                    logger.warn("Client disconnected. Retrying to connect.");
                    clientEndPoint.connectClient();
                }
            });

            clientEndPoint.connectClient();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> clientEndPoint.closeConnection(
                    new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Client terminated"))));
        } catch (URISyntaxException e) {
            logger.error("URISyntaxException exception: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendResponse(OperationResponse response) {
        // send message to websocket
        if (logger.isDebugEnabled()) {
            logger.debug("Sending message: " + response.toJSON());
        }
        clientEndPoint.sendMessage(response.toJSON());
    }
}
