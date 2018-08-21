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
import org.wso2.testgrid.common.agentoperation.OperationSegment;
import org.wso2.testgrid.common.config.ConfigurationContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private static final int MAX_NUMBER_OF_THREAD = 10;
    private String tinkererHost;
    private String authenticationToken;
    private String testPlanId;

    /**
     * Tinkerer constructor class.
     */
    public TinkererSDK() {}

    /**
     * Initialize details of the tinkerer host and test plan details.
     *
     * @param testPlanId            The test plan id
     */
    public TinkererSDK(String testPlanId) {
        this.tinkererHost = ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH);
        String authenticationString = ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_USERNAME) + ":" +
                ConfigurationContext.getProperty(
                        ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_PASSWORD);
        this.authenticationToken = "Basic " + Base64.getEncoder().encodeToString(
                authenticationString.getBytes(StandardCharsets.UTF_8));
        this.testPlanId = testPlanId;
    }

    /**
     * Send shell command as http request to the Tinkerer and get back the result.
     *
     * @return      Response handler as CommandResponse
     */
    public CommandResponse executeCommand(String instanceName, String command, boolean async) {
        Client client = ClientBuilder.newClient();
        String operationId = UUID.randomUUID().toString();
        OperationRequest operationRequest = new OperationRequest(command,
                OperationRequest.OperationCode.SHELL);
        operationRequest.setOperationId(operationId);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(operationRequest);
        String requestLink = tinkererHost + "test-plan/" + this.testPlanId + "/agent/" + instanceName;
        logger.info("Sending commands to " + requestLink + " test plan " + this.testPlanId + " agent " +
                instanceName);
        Response response = client.target(requestLink)
                .path("stream-operation")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .post(Entity.entity(jsonRequest,
                        MediaType.APPLICATION_JSON));
        if (async) {
            Path filePath = Paths.get(System.getProperty("java.io.tmpdir"), operationId.concat(".txt"));
            ScriptExecutorThread scriptExecutorThread = new ScriptExecutorThread(operationRequest.getOperationId()
                    , response, filePath);
            ExecutorService threadPool = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREAD);
            threadPool.execute(scriptExecutorThread);
            AsyncCommandResponse asyncCommandResponse = new AsyncCommandResponse(filePath, scriptExecutorThread);
            asyncCommandResponse.setOperationId(operationRequest.getOperationId());
            return asyncCommandResponse;
        } else {
            OperationSegment operationSegment = new Gson().
                    fromJson(response.readEntity(String.class), OperationSegment.class);
            SyncCommandResponse syncCommandResponse = new SyncCommandResponse();
            syncCommandResponse.setResponse(operationSegment.getResponse());
            syncCommandResponse.setExitValue(operationSegment.getExitValue());
            return syncCommandResponse;
        }
    }

    /**
     * Abort running operation by sending abort command.
     *
     * @return                  The result status
     */
    public int abortExecution(String operationId, String instanceName) {
        Client client = ClientBuilder.newClient();
        OperationRequest operationRequest = new OperationRequest("", OperationRequest.OperationCode.ABORT);
        operationRequest.setOperationId(operationId);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(operationRequest);
        Response response = client.target(tinkererHost + "test-plan/" + this.testPlanId +
                "/agent/" + instanceName)
                .path("abort")
                .request()
                .header(HttpHeaders.AUTHORIZATION, this.authenticationToken)
                .post(Entity.entity(jsonRequest,
                        MediaType.APPLICATION_JSON));
        return response.getStatus();
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

