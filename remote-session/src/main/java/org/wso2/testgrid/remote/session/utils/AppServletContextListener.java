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

package org.wso2.testgrid.remote.session.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.remote.session.beans.Operation;
import org.wso2.testgrid.remote.session.beans.OperationRequest;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * This class listen for web app lifecycle and run heartbeat.
 */
public class AppServletContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppServletContextListener.class);

    private Timer heartBeatTimer;

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        heartBeatTimer.cancel();
    }

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        TimerTask hbTimerTask = new HeartBeatTimerTask();
        heartBeatTimer = new Timer(true);
        heartBeatTimer.scheduleAtFixedRate(hbTimerTask, Constants.HEARTBEAT_INTERVAL, Constants.HEARTBEAT_INTERVAL);
    }

    static final class HeartBeatTimerTask extends TimerTask {

        @Override
        public void run() {
            SessionManager sessionManager = SessionManager.getInstance();
            for (String agentId : sessionManager.getAgentIds()) {
                sendHeartBeat(sessionManager, agentId);
            }
        }

        private void sendHeartBeat(SessionManager sessionManager, String agentId) {
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
                    }
                }
                sessionManager.removeOperationResponse(operationRequest.getOperationId());
            } catch (IOException e) {
                String message = "Error occurred while sending operation to agent: " + agentId;
                logger.error(message, e);
            }
        }
    }
}
