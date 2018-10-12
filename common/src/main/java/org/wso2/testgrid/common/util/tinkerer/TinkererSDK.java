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

package org.wso2.testgrid.common.util.tinkerer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.agentoperation.OperationRequest;
import org.wso2.testgrid.common.agentoperation.OperationSegment;
import org.wso2.testgrid.common.config.ConfigurationContext;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Execute commands on remote agent through tinkerer.
 */
public class TinkererSDK {

    private static final Logger logger = LoggerFactory.getLogger(TinkererSDK.class);

    private String tinkererHost;
    private String authenticationToken;

    /**
     * Initialize details of the tinkerer host and authentication details.
     */
    public TinkererSDK() {
        this.tinkererHost = ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH);
        String authenticationString = ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_USERNAME) + ":" +
                ConfigurationContext.getProperty(
                        ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_PASSWORD);
        this.authenticationToken = "Basic " + Base64.getEncoder().encodeToString(
                authenticationString.getBytes(StandardCharsets.UTF_8));
        if (this.tinkererHost == null) {
            logger.warn("Tinkerer host does not initialized properly");
        }
    }

    /**
     * Send shell command as http request to the Tinkerer and get back the result as asynced result.
     *
     * @param agentId   id of the agent to send command
     * @param command   command to execute
     * @return      Response handler as Async response
     */
    public AsyncCommandResponse executeCommandAsync(String agentId, String command) {
        Client client = ClientBuilder.newClient();
        String operationId = UUID.randomUUID().toString();
        OperationRequest operationRequest = new OperationRequest(command,
                OperationRequest.OperationCode.SHELL, agentId);
        operationRequest.setOperationId(operationId);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(operationRequest);
        logger.info("Sending async commands to " + this.tinkererHost  + " agent " + agentId);
        Response response = client.target(this.tinkererHost)
                .path("stream-operation")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .post(Entity.entity(jsonRequest,
                        MediaType.APPLICATION_JSON));
        // Save tinkerer execution logs into a temporary file path which provided by java
        Path filePath = Paths.get(System.getProperty("java.io.tmpdir"));
        ScriptExecutorThread scriptExecutorThread = new ScriptExecutorThread(operationRequest.getOperationId()
                , response, filePath);
        scriptExecutorThread.start();
        AsyncCommandResponse asyncCommandResponse = new AsyncCommandResponse(operationId,
                filePath, scriptExecutorThread);
        asyncCommandResponse.setOperationId(operationRequest.getOperationId());
        return asyncCommandResponse;
    }

    /**
     * Send shell command as http request to the Tinkerer and get back the result as synced response.
     *
     * @param agentId   id of the agent to send command
     * @param testPlanId    The test plan id
     * @param instantName   The instant name
     * @param command   command to execute
     * @return  Response handler as Sync response
     */
    public SyncCommandResponse executeCommandSync(String agentId, String testPlanId, String instantName,
                                                  String command) {
        Client client = ClientBuilder.newClient();
        String operationId = UUID.randomUUID().toString();
        OperationRequest operationRequest = new OperationRequest(command,
                OperationRequest.OperationCode.SHELL, agentId);
        operationRequest.setOperationId(operationId);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(operationRequest);
        logger.info("Sending sync commands to " + this.tinkererHost + " agent " + agentId);
        Response response = client.target(this.tinkererHost + "test-plan/" + testPlanId
                + "/agent/" + instantName)
                .path("operation")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .post(Entity.entity(jsonRequest,
                        MediaType.APPLICATION_JSON));
        OperationSegment operationSegment = new Gson().
                fromJson(response.readEntity(String.class), OperationSegment.class);
        SyncCommandResponse syncCommandResponse = new SyncCommandResponse();
        syncCommandResponse.setResponse(operationSegment.getResponse());
        syncCommandResponse.setExitValue(operationSegment.getExitValue());
        return syncCommandResponse;
    }

    /**
     * Abort running operation by sending abort command.
     *
     * @param operationId       Operation id of the process to abort
     * @param agentId           Agent id which running operation
     * @return                  The result status
     */
    public int abortExecution(String operationId, String agentId) {
        Client client = ClientBuilder.newClient();
        OperationRequest operationRequest = new OperationRequest("", OperationRequest.OperationCode.ABORT, agentId);
        operationRequest.setOperationId(operationId);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(operationRequest);
        Response response = client.target(this.tinkererHost)
                .path("abort")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .post(Entity.entity(jsonRequest,
                        MediaType.APPLICATION_JSON));
        return response.getStatus();
    }

    /**
     * Get a list of agent for given set of test plans
     *
     * @param testPlanId    The test plan id
     * @return      List of agents for given test plan id
     */
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    public List<Agent> getAgentListByTestPlanId(String testPlanId) {
        Client client = ClientBuilder.newClient();
        Response response = client.target(this.tinkererHost + "test-plan/" + testPlanId)
                .path("agents")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .get();
        Type listType =  new TypeToken<List<Agent>>() { }.getType();
        return new Gson().fromJson(response.readEntity(String.class), listType);
    }

    /**
     * Get a list of all agents from Tinkerer
     *
     * @return      List of all Tinkerer agents
     */
    public List<Agent> getAllAgentList() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(this.tinkererHost)
                .path("agents")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .get();
        Type listType =  new TypeToken<List<Agent>>() { }.getType();
        return new Gson().fromJson(response.readEntity(String.class), listType);
    }

    public List<String> getAllTestPlanIds() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(this.tinkererHost)
                .path("test-plans")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .get();
        Type listType =  new TypeToken<List<String>>() { }.getType();
        return new Gson().fromJson(response.readEntity(String.class), listType);
    }

    /**
     * Get a list of agent by given test plan id and instant name.
     *
     * @param testPlanId        The test plan id
     * @param instantName       Instant name of the node
     * @return                  List of agents
     */
    public List<Agent> getAgentListByInstantName(String testPlanId, String instantName) {
        List<Agent> agentList = new ArrayList<>();
        for (Agent agent : getAgentListByTestPlanId(testPlanId)) {
            if (agent.getInstanceName().equals(instantName)) {
                agentList.add(agent);
            }
        }
        return agentList;
    }

    /**
     * Set Tinkerer host
     *
     * @param tinkererHost  The Tinkerer host
     */
    public void setTinkererHost(String tinkererHost) {
        this.tinkererHost = tinkererHost;
    }
}

