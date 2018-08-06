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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.agentoperation.OperationRequest;
import org.wso2.testgrid.common.agentoperation.OperationSegment;
import org.wso2.testgrid.deployment.tinkerer.beans.OperationQueue;
import org.wso2.testgrid.deployment.tinkerer.providers.InfraProviderFactory;
import org.wso2.testgrid.deployment.tinkerer.providers.Provider;
import org.wso2.testgrid.deployment.tinkerer.utils.AgentObserver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.websocket.Session;

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
    private static volatile Map<String, OperationQueue> operationQueueMap = new HashMap<>();
    private static volatile AgentObserver agentObserver = new AgentObserver();

    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private SessionManager() {
        // Create folder to store overflow execution result in testgrid home if it not already exist
        Path persistedFilePath = Paths.get(OperationQueue.PERSISTED_FILE_PATH);
        if (!Files.exists(persistedFilePath)) {
            boolean fileStatus = new File(persistedFilePath.toString()).mkdirs();
            if (!fileStatus) {
                logger.error("Unable to create new directory for folder" + persistedFilePath.toString());
            }
        }
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
            Optional<Provider> infrastructureProvider = InfraProviderFactory
                    .getInfrastructureProvider(provider);
            infrastructureProvider
                    .ifPresent(infraProvider -> infraProvider.getInstanceUserName(region, instanceId)
                            .ifPresent(agent::setInstanceUser));
            infrastructureProvider
                    .ifPresent(infraProvider -> infraProvider.getInstanceName(region, instanceId)
                            .ifPresent(agent::setInstanceName));
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
     * Get OperationQueue for a given operation id
     *
     * @param operationId   operation id of relevant OperationQueue
     * @return  OparationQueue for the relevant operation id
     */
    public synchronized OperationQueue getOperationRequest(String operationId) {
        Optional<Map.Entry<String, OperationQueue>> operationOptional = operationQueueMap.entrySet().stream()
                .filter(entry -> {
                    OperationQueue operationQueue = entry.getValue();
                    if (operationQueue != null && operationId != null) {
                        return operationId.equals(operationQueue.getOperationId());
                        }
                        return false;
                    }).findFirst();
        return operationOptional.map(Map.Entry::getValue).orElse(null);
    }

    /**
     * Get operationQueueMap
     *
     * @return      operationQueueMap
     */
    public static synchronized Map<String, OperationQueue> getOperationQueueMap() {
        return operationQueueMap;
    }

    /**
     * Add new Operation queue to the operationQueueMap
     *
     * @param operationId   operation id for the new message queue
     * @param code          Type of the operation
     * @param agentId       The agent id
     */
    public synchronized void addNewOperationQueue(String operationId, OperationRequest.OperationCode code,
                                                  String agentId) {
        OperationQueue operationQueue = new OperationQueue(operationId, code, agentId);
        operationQueueMap.put(operationId, operationQueue);
    }

    /**
     * Get list of messages as single OperationSegment object for given operation id
     *
     * @param operationId       operation id of the message
     * @return
     */
    public OperationSegment getOperationQueueMessages(String operationId) {
        String returnMessage = "";
        OperationSegment tempOperationSegment = new OperationSegment();
        OperationQueue operationQueue = getOperationRequest(operationId);
        if (operationQueue != null) {
            for (String operationSegment : operationQueue.getMessageQueue()) {
                returnMessage = returnMessage.concat(operationSegment);
            }
            tempOperationSegment.setCompleted(operationQueue.isCompleted());
            tempOperationSegment.setExitValue(operationQueue.getExitValue());
            tempOperationSegment.setCode(operationQueue.getCode());
            tempOperationSegment.setOperationId(operationId);
        }
        tempOperationSegment.setResponse(returnMessage);
        return tempOperationSegment;
    }

    /**
     * Dequeue all new messages from operationQueueMap if any new messages are available
     *
     * @param operationId   operation id to select message queue
     * @return
     */
    public synchronized OperationSegment dequeueOperationQueueMessages(String operationId) {
        OperationSegment operationSegment = getOperationQueueMessages(operationId);
        OperationQueue operationQueue = operationQueueMap.get(operationId);
        if (operationQueue != null) {
            operationQueueMap.get(operationId).resetMessageQueue();
            operationQueueMap.get(operationId).updateLastConsumedTime();
        }
        return operationSegment;
    }

    /**
     * Check for given operationId message queue have at least one response from agent
     *
     * @param operationId   The operation id
     * @return      has new messages
     */
    public synchronized boolean hasMessageQueueResponse(String operationId) {
        OperationQueue operationQueue = operationQueueMap.get(operationId);
        if (operationQueue != null) {
            if (operationQueue.getMessageQueue().size() > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Remove message queue from operationQueueMap by using operation id
     *
     * @param operationId   operation id for relevant message queue
     */
    public synchronized void removeOperationQueueMessages(String operationId) {
        operationQueueMap.remove(operationId);
    }

    /**
     * Get agent observer
     *
     * @return  agent observer
     */
    public static AgentObserver getAgentObserver() {
        return agentObserver;
    }
}

