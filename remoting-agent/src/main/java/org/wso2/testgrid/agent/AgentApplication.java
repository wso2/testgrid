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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.agent.websocket.ClientEndpoint;
import org.wso2.testgrid.common.util.EnvironmentUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.websocket.CloseReason;

/**
 * Agent bootstrap class.
 *
 * @since 1.0.0
 */
public class AgentApplication {

    private static final Logger logger = LoggerFactory.getLogger(AgentApplication.class);
    private static Path agentPropFilePath;

    static {
        try {
            switch (EnvironmentUtil.getOperatingSystemType()) {
                case Linux:
                case MacOS:
                case Other:
                    agentPropFilePath = Paths.get(
                            new URI ("file:///opt/testgrid/agent-config.properties"));
                    break;
                case Windows:
                    agentPropFilePath = Paths.get(
                        new URI ("file:///C:/testgrid/app/agent-config.properties"));
                break;
            }
        } catch (URISyntaxException e) {
            logger.error("Invalid agent configuration file uri.", e);
        }
    }

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
        String wsEndpoint;
        String agentId;
        String userName;
        String password;

        Properties prop = new Properties();
        InputStream input = null;
        try {
            if (Files.exists(agentPropFilePath)) {
                input = new FileInputStream(agentPropFilePath.toFile());
                prop.load(input);
                wsEndpoint = prop.getProperty("wsEndpoint");
                //aws:us-west-1:6718f976-7150-316d-9ec6-db29b7a2675f:i-0658922f45d25856f
                agentId = prop.getProperty("provider") + ":" + prop.getProperty("region") + ":" +
                        prop.getProperty("testPlanId") + ":" + prop.getProperty("instanceId") + ":" +
                        prop.getProperty("instanceIP");
                userName = prop.getProperty("userName");
                password = prop.getProperty("password");
            } else {
                logger.warn("Agent configurations not found in " + agentPropFilePath.toString());
                return;
            }
        } catch (IOException ex) {
            logger.error("Error occurred when reading prop file: " + ex.getMessage(), ex);
            return;
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
            String agentEndpoint;
            if (wsEndpoint.endsWith("/")) {
                agentEndpoint = wsEndpoint + agentId;
            } else {
                agentEndpoint = wsEndpoint + "/" + agentId;
            }
            clientEndPoint = new ClientEndpoint(new URI(agentEndpoint), userName, password);
            clientEndPoint.connectClient();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> clientEndPoint.closeConnection(
                    new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Client terminated"))));
        } catch (URISyntaxException e) {
            logger.error("URISyntaxException exception: " + e.getMessage(), e);
        }
    }
}
