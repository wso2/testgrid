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
import org.wso2.testgrid.deployment.tinkerer.beans.Operation;
import org.wso2.testgrid.deployment.tinkerer.beans.OperationRequest;
import org.wso2.testgrid.deployment.tinkerer.utils.Constants;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * This class listen for web app lifecycle and run heartbeat.
 *
 * @since 1.0.0
 */
public class AppServletContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppServletContextListener.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private Timer heartBeatTimer;

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * <p>All ServletContextListeners are notified of context
     * initialization before any filters or servlets in the web
     * application are initialized.
     *
     * @param contextEvent the ServletContextEvent containing the ServletContext
     * that is being initialized
     */
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        TimerTask hbTimerTask = new HeartBeatTimerTask();
        heartBeatTimer = new Timer(true);
        heartBeatTimer.scheduleAtFixedRate(hbTimerTask, Constants.HEARTBEAT_INTERVAL, Constants.HEARTBEAT_INTERVAL);
    }

    /**
     * Receives notification that the ServletContext is about to be
     * shut down.
     *
     * <p>All servlets and filters will have been destroyed before any
     * ServletContextListeners are notified of context
     * destruction.
     *
     * @param contextEvent the ServletContextEvent containing the ServletContext
     * that is being destroyed
     */
    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        heartBeatTimer.cancel();
        executorService.shutdown();
    }

    /**
     * Inner class contains the Heartbeat timer implementation.
     *
     * @since 1.0.0
     */
    static final class HeartBeatTimerTask extends TimerTask {

        @Override
        public void run() {
            final SessionManager sessionManager = SessionManager.getInstance();
            sessionManager.getAgentIds().forEach(agentId -> sendHeartBeat(sessionManager, agentId));
        }

        /**
         * Sends heartbeat to specified agent.
         *
         * @param sessionManager - Session manager which holds the agent session data.
         * @param agentId - Agent Id of the agent.
         */
        @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
                justification = "No use of returned Future<?> from executor service submit().")
        private void sendHeartBeat(SessionManager sessionManager, String agentId) {
            Runnable hbTask = () -> {
                Session wsSession = sessionManager.getAgentSession(agentId);
                try {
                    OperationRequest operationRequest = new OperationRequest();
                    operationRequest.setCode(Operation.OperationCode.PING);
                    operationRequest.setOperationId(UUID.randomUUID().toString());
                    wsSession.getBasicRemote().sendText(operationRequest.toJSON());
                    long initTime = Calendar.getInstance().getTimeInMillis();
                    while (!sessionManager.hasOperationResponse(operationRequest.getOperationId())) {
                        long currentTime = Calendar.getInstance().getTimeInMillis();
                        if (initTime + (Constants.OPERATION_TIMEOUT / 2) < currentTime) {
                            if (wsSession.isOpen()) {
                                wsSession.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                                        "Agent unresponsive"));
                            }
                            sessionManager.removeAgentSession(agentId);
                            logger.error("Removed unresponsive agent: " + agentId);
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignore) {
                            break;
                        }
                    }
                    if (sessionManager.hasOperationResponse(operationRequest.getOperationId())) {
                        sessionManager.removeOperationResponse(operationRequest.getOperationId());
                    }
                } catch (IOException e) {
                    String message = "Error occurred while sending heartbeat to agent: " + agentId;
                    logger.error(message, e);
                }
            };
            executorService.submit(hbTask);
        }
    }
}
