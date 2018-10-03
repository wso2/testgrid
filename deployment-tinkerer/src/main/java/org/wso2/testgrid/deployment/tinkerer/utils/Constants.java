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

package org.wso2.testgrid.deployment.tinkerer.utils;

/**
 * This class holds the constants related to remote session web app.
 *
 * @since 1.0.0
 */
public final class Constants {

    public static final String HTTP_HEADERS = "HttpHeaders";
    public static final int OPERATION_TIMEOUT = 900000; //Operation timeout 15 minutes interval in milliseconds.
    public static final int PING_HEARTBEAT_INTERVAL = 60000; //Heartbeat interval in milliseconds.
    public static final int MESSAGE_QUEUE_INTERVAL = 2000; // Message queue refresh interval in milliseconds
    public static final int AGENT_WAIT_TIMEOUT = 500; // Timeout wait to execute operation on agent without streaming
    public static final double MAX_QUEUE_CONTENT_LENGTH = 5e6; // Maximum size of the each of the message queue
    public static final int MAX_LAST_CONSUME_TIMEOUT = 900000;  // Maximum waiting time to dequeue message queue
    public static final int MAX_LAST_UPDATED_TIMEOUT = 900000;  // Maximum waiting time to update message queue

    private Constants() {
    }
}
