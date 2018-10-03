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

/**
 * Object to communicate between stream reader and listener.
 */
public class StreamResponse {
    private String response;
    private boolean completed;
    private StreamType streamType;

    /**
     * Initialize object with response
     *
     * @param response      Response to send
     * @param completed     State of execution
     * @param streamType    Type of stream buffer
     */
    public StreamResponse (String response, boolean completed, StreamType streamType) {
        this.response = response;
        this.completed = completed;
        this.streamType = streamType;
    }

    /**
     * Set stream read state as completed
     *
     * @param completed         state of the read stream
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Check if process completed
     *
     * @return          Current state of the stream
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Set response to send to observer
     *
     * @param response      Response to send
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * Get response sent
     *
     * @return      The response
     */
    public String getResponse() {
        return response;
    }

    /**
     * Get type of streaming buffer
     *
     * @return  Type of streaming buffer
     */
    public StreamType getStreamType() {
        return streamType;
    }

    /**
     * Type of stream to handle
     */
    public enum StreamType {
        INPUT, ERROR
    }
}
