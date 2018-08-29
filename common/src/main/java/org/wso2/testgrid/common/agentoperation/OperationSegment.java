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

/**
 * Tinkerer response class to handle segment response from Tinkerer message queue.
 */
public class OperationSegment extends Operation {

    private int exitValue;

    private String response;

    private boolean completed;

    /**
     * Get exit value of the execution result
     *
     * @return      exit value of the operation
     */
    public int getExitValue() {
        return exitValue;
    }

    /**
     * Set exit value of the execution result
     *
     * @param exitValue     exit value of the operation
     */
    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }

    /**
     * Get content of execution result
     *
     * @return      The response
     */
    public String getResponse() {
        return response;
    }

    /**
     * Set content of the execution result
     *
     * @param response      The response
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * Check operation execution state is completed or not
     *
     * @return      Completed state
     */
    public boolean getCompleted() {
        return completed;
    }

    /**
     * Set operation execution state
     *
     * @param completed     Completed state
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

