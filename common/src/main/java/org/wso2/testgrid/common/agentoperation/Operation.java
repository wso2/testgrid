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

package org.wso2.testgrid.common.agentoperation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;

import java.util.Map;

/**
 * Tinkerer operation class to handle response from and to Tinkerer message queue.
 */
public class Operation {

    private String operationId;

    private OperationCode code;

    @JsonIgnoreProperties
    private Map<String, String> metaData;

    /**
     * Get operation id for the given operation
     *
     * @return      The operation id
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Set operation id for the given operation
     *
     * @param operationId   The operation id
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get type of the operation SHELL/PING/STREAM_FILE/ABORT
     *
     * @return  The code
     */
    public OperationCode getCode() {
        return code;
    }

    /**
     * Set the type of the operation
     *
     * @param code The code
     */
    public void setCode(OperationCode code) {
        this.code = code;
    }

    /**
     * Get meta data for file streaming
     *
     * @return      meta data map
     */
    public Map<String, String> getMetaData() {
        return metaData;
    }

    /**
     * Set meta data for file streaming
     *
     * @param data meta data map
     */
    public void setMetaData(Map<String, String> data) {
        this.metaData = data;
    }

    /**
     * Supported operations.
     *
     * @since 1.0.0
     */
    public enum OperationCode {
        SHELL, PING, STREAM_FILE, ABORT
    }

    /**
     * This method is used to convert operation object to a json format.
     *
     * @return json formatted String.
     */
    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
