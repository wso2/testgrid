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

package org.wso2.testgrid.deployment.tinkerer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.deployment.tinkerer.beans.OperationResponse;
import org.wso2.testgrid.deployment.tinkerer.providers.AWSProvider;
import org.wso2.testgrid.deployment.tinkerer.providers.Provider;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class manage sessions of agents corresponding to agent ids.
 *
 * @since 1.0.0
 */
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private static final SessionManager sessionManager = new SessionManager();
    private static volatile Map<String, Session> agentSessions = new HashMap<>();
    private static volatile Map<String, Agent> agents = new HashMap<>();
    private static volatile Map<String, OperationResponse> operationResponses = new HashMap<>();

    private SessionManager() {
    }

    /**
     * Return instance of {@link SessionManager}.
     *
     * @return the instance of {@link SessionManager}.
     */
    public static SessionManager getInstance() {
        return sessionManager;
    }

    /**
     * Create agent session entry to store {@link Session} object against to the agent id.
     *
     * @param agentId      - Id of the agent.
     * @param agentSession - {@link Session} belongs to the agent.
     */
    public synchronized void createAgentSession(String agentId, Session agentSession) {
        Agent agent = new Agent(agentId);
        String provider = agent.getProvider();
        String region = agent.getRegion();
        String instanceId = agent.getInstanceId();
        if (provider != null && region != null && instanceId != null) {
            agent.setInstanceName(getInstanceName(provider, region, instanceId));
            agent.setInstanceUser(getInstanceUser(provider, region, instanceId));
            agentSessions.put(agentId, agentSession);
            agents.put(agentId, agent);
        }
    }

    /**
     * Remove {@link Session} entry when agent leaves.
     *
     * @param agentId - Id of the agent.
     */
    public synchronized void removeAgentSession(String agentId) {
        agentSessions.remove(agentId);
        agents.remove(agentId);
    }

    /**
     * Get the agent by specifying the test plan id and instance name.
     *
     * @param testPlanId   - Test plan id which spawned the agent.
     * @param instanceName - Name of the Instance which contains the agent.
     * @return the unique agent if exists, null otherwise.
     */
    public Agent getAgent(String testPlanId, String instanceName) {
        Optional<Map.Entry<String, Agent>> agentOptional = agents.entrySet().stream()
                .filter(entry -> {
                    Agent agent = entry.getValue();
                    if (agent != null && testPlanId != null && instanceName != null) {
                        return testPlanId.equals(agent.getTestPlanId()) && instanceName.equals(agent.getInstanceName());
                    }
                    return false;
                }).findFirst();
        return agentOptional.map(Map.Entry::getValue).orElse(null);
    }

    /**
     * Check the existence of agent {@link Session} specified by the agent id.
     *
     * @param agentId - Id of he agent.
     * @return true if {@link Session} exists, false otherwise.
     */
    public boolean hasAgentSession(String agentId) {
        return agentSessions.containsKey(agentId);
    }

    /**
     * Get the {@link Session} of the agent.
     *
     * @param agentId - Id of the agent.
     * @return the {@link Session} of the agent.
     */
    public Session getAgentSession(String agentId) {
        return agentSessions.get(agentId);
    }

    /**
     * Get all agents registered with.
     *
     * @return A {@link List<Agent>} of agent Ids.
     */
    public List<Agent> getAgents() {
        return new ArrayList<>(agents.values());
    }

    /**
     * Get all agent ids registered with.
     *
     * @return A {@link List} of agent Ids.
     */
    public List<String> getAgentIds() {
        return new ArrayList<>(agentSessions.keySet());
    }

    /**
     * Add {@link OperationResponse} corresponding to an agent.
     *
     * @param operationResponse - The {@link OperationResponse} received.
     */
    public synchronized void addOperationResponse(OperationResponse operationResponse) {
        operationResponses.put(operationResponse.getOperationId(), operationResponse);
    }

    /**
     * Check the existence of {@link OperationResponse} specified by operationId.
     *
     * @param operationId - Id of the {@link org.wso2.testgrid.deployment.tinkerer.beans.Operation}.
     * @return true if {@link OperationResponse} exists, false otherwise.
     */
    public boolean hasOperationResponse(String operationId) {
        return operationResponses.containsKey(operationId);
    }

    /**
     * Retrieve {@link OperationResponse} specified by operationId.
     *
     * @param operationId - Id of the {@link OperationResponse}.
     * @return Corresponding {@link OperationResponse}.
     */
    public OperationResponse retrieveOperationResponse(String operationId) {
        OperationResponse operationResponse = operationResponses.get(operationId);
        removeOperationResponse(operationId);
        return operationResponse;
    }

    /**
     * Remove {@link OperationResponse}.
     *
     * @param operationId - Id of the {@link OperationResponse}.
     */
    public synchronized void removeOperationResponse(String operationId) {
        operationResponses.remove(operationId);
    }

    /**
     * Get name of the instance name specified for the cloud provider.
     *
     * @param provider   - Provider name.
     * @param region     - Provider region.
     * @param instanceId - Id of the instance.
     * @return Name of the instance.
     */
    private static String getInstanceName(String provider, String region, String instanceId) {
        Provider infraProvider = null;
        switch (provider) {
            case "aws":
                infraProvider = new AWSProvider();
                break;
            default:
                logger.warn("Unknown cloud provider: " + provider);
        }
        if (infraProvider != null) {
            return infraProvider.getInstanceName(region, instanceId);
        } else {
            return instanceId;
        }
    }

    /**
     * Get the instance username that is used to log into the instance depending on the cloud provider.
     *
     * @param provider Provider Name
     * @param region Region of the instance
     * @param instanceId ID of the instance
     * @return Username of the instance
     */
    private static String getInstanceUser(String provider, String region, String instanceId) {

        Provider infraProvider = null;
        switch (provider) {
            case "aws":
                infraProvider = new AWSProvider();
                break;
            default:
                logger.warn("Unknown cloud provider: " + provider);
        }
        if (infraProvider != null) {
            Optional<String> instanceUserName = infraProvider.getInstanceUserName(region, instanceId);
            return instanceUserName.orElse(null);
        } else {
            return null;
        }
    }

}
