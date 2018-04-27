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

/**
 * Agent bootstrap class
 */
public class AgentApplication implements OperationResponseListner {

    private static final Logger logger = LoggerFactory.getLogger(AgentApplication.class);
    private static final String AGENT_PROP_FILE = File.pathSeparator + "opt" + File.pathSeparator + "testgrid" +
            File.pathSeparator + "agent-config.properties";

    private ClientEndpoint clientEndPoint;

    public static void main(String args[]) {
        new AgentApplication().startService();
    }

    private synchronized void startService() {
        String wsEndpoint = "ws://localhost:8080/remote-session/agent/";
        String agentId = "6718f976-7150-316d-9ec6-db29b7a2675f:wso2-is-node-1";

        Properties prop = new Properties();
        InputStream input = null;
        try {
            if (new File(AGENT_PROP_FILE).exists()) {
                input = new FileInputStream(AGENT_PROP_FILE);
                prop.load(input);
                wsEndpoint = prop.getProperty("wsEndpoint");
                agentId = prop.getProperty("test-plan-id") + ":" + prop.getProperty("node-id");
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
            // open websocket
            clientEndPoint = new ClientEndpoint(new URI(wsEndpoint + agentId));

            // add listener
            clientEndPoint.addMessageHandler(message -> {
                logger.info("Operation received: " + message);
                OperationRequest operationRequest = new Gson().fromJson(message, OperationRequest.class);
                OperationExecutor operationExecutor = new OperationExecutor(AgentApplication.this);
                operationExecutor.executeOperation(operationRequest);
            });

            while (true) {
                try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (URISyntaxException e) {
            logger.error("URISyntaxException exception: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void sendResponse(OperationResponse response) {
        // send message to websocket
        if (logger.isDebugEnabled()) {
            logger.debug("Sending message: " + response.toJSON());
        }
        clientEndPoint.sendMessage(response.toJSON());
    }
}
