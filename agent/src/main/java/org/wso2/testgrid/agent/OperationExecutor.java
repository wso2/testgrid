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
import org.wso2.testgrid.agent.beans.OperationRequest;
import org.wso2.testgrid.agent.beans.OperationResponse;
import org.wso2.testgrid.agent.listners.OperationResponseListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class executes operations.
 *
 * @since 1.0.0
 */
public class OperationExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AgentApplication.class);

    private OperationResponseListener operationResponseListener;

    public OperationExecutor(OperationResponseListener operationResponseListener) {
        this.operationResponseListener = operationResponseListener;
    }

    public void executeOperation(OperationRequest operationRequest) {
        OperationResponse operationResponse = new OperationResponse();
        operationResponse.setOperationId(operationRequest.getOperationId());
        operationResponse.setCode(operationRequest.getCode());

        switch (operationRequest.getCode()) {
            case SHELL:
                String response = executeCommand(operationRequest.getRequest());
                operationResponse.setResponse(response);
                break;
            case PING:
                operationResponse.setResponse("ACK");
                break;
        }
        operationResponseListener.sendResponse(operationResponse);
    }

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        Process p;
        BufferedReader reader = null;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream(), UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (InterruptedException | IOException e) {
            String message = "Error occurred when executing command: " + command;
            logger.error(message, e);
            return message;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("Error occurred when closing buffered reader.", e);
                }
            }
        }
        return output.toString();
    }

}
