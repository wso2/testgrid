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

package org.wso2.testgrid.tinkerer;

import com.google.gson.Gson;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.agentoperation.OperationRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Execute commands on remote agent through tinkerer.
 */
public class AgentScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AgentScriptExecutor.class);

    private String tinkererHost;
    private String authenticationToken;
    private String testPlanId;
    private String instanceName;
    private static volatile Map<String, ScriptExecutorThread> executorThreadHashMap = new HashMap<>();

    /**
     * AgentScriptExecutor constructor class
     */
    public AgentScriptExecutor() {}

    /**
     * Initialize details of the tinkerer host and test plan details.
     *
     * @param tinkererHost          Tinkerer host name
     * @param authenticationToken   Authentication token in Base64 format
     * @param testPlanId            The test plan id
     * @param instanceName          Instance name on the remote agent
     */
    public AgentScriptExecutor(String tinkererHost, String authenticationToken, String testPlanId,
                               String instanceName) {
        this.tinkererHost = tinkererHost;
        this.authenticationToken = authenticationToken;
        this.testPlanId = testPlanId;
        this.instanceName = instanceName;
    }

    /**
     * Send shell command as http request to the Tinkerer and get back the result.
     *
     * @param command       Command to execute on agent
     * @return              Exit value of the execution
     */
    public String executeStreamCommand(String command) {
        Client client = ClientBuilder.newClient();
        String operationId = UUID.randomUUID().toString();
        OperationRequest operationRequest = new OperationRequest(command,
                OperationRequest.OperationCode.SHELL);
        operationRequest.setOperationId(operationId);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(operationRequest);
        logger.info("Sending commands to " + this.tinkererHost + " test plan " + this.testPlanId + " agent " +
                this.instanceName);
        Response response = client.target(tinkererHost + "test-plan/" + this.testPlanId +
                "/agent/" + this.instanceName)
                .path("stream-shell")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .post(Entity.entity(jsonRequest,
                        MediaType.APPLICATION_JSON));
        ScriptExecutorThread scriptExecutorThread = new ScriptExecutorThread(operationRequest.getOperationId()
                , response);
        executorThreadHashMap.put(operationRequest.getOperationId(), scriptExecutorThread);
        scriptExecutorThread.start();
        return operationRequest.getOperationId();
    }

    /**
     * Check if operation execution end or still proceeding.
     *
     * @param operationId       The operation id
     * @return                  Current state of the operation
     */
    public boolean isOperationCompleted(String operationId) {
        return executorThreadHashMap.get(operationId).isCompleted();
    }

    /**
     * Get the exit value of the operation.
     *
     * @param operationId       The operation id
     * @return                  The exit value
     */
    public int getExitValue(String operationId) {
        return executorThreadHashMap.get(operationId).getExitValue();
    }
}

