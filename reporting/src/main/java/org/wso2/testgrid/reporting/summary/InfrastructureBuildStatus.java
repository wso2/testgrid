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

package org.wso2.testgrid.reporting.summary;

import java.util.List;

/**
 * todo.
 */
public class InfrastructureBuildStatus {
    private List<String> successStatus;
    /**
     * todo
     */
    private List<List<String>> failedStatus;

    public List<String> getSuccessStatus() {
        return successStatus;
    }

    public void setSuccessStatus(List<String> successStatus) {
        this.successStatus = successStatus;
    }

    public List<List<String>> getFailedStatus() {
        return failedStatus;
    }

    public void setFailedStatus(List<List<String>> failedStatus) {
        this.failedStatus = failedStatus;
    }

    public List<String> getUnknownStatus() {
        return unknownStatus;
    }

    public void setUnknownStatus(List<String> unknownStatus) {
        this.unknownStatus = unknownStatus;
    }

    private List<String> unknownStatus;

}
