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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.agent.beans;

/**
 * This class holds operation response data.
 *
 * @since 1.0.0
 */
public class OperationResponse extends Operation {

    private String response;

    private int exitValue;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * Get execution exitValue
     *
     * @return execution exitValue
     */
    public int getExitValue() {
        return exitValue;
    }

    /**
     * Set execution exitValue
     *
     * @param exitValue execution exitValue
     */
    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }
}