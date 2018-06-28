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

package org.wso2.testgrid.common;

import java.io.Serializable;

/**
 * This class holds Agent data.
 *
 * @since 1.0.0
 */
public class Agent implements Serializable {

    private static final long serialVersionUID = 9208056724380972876L;

    private String agentId;
    private String provider;
    private String region;
    private String testPlanId;
    private String instanceId;
    private String instanceName;
    private String instanceIp;
    private String instanceUser;

    public Agent(String agentId) {
        this.agentId = agentId;
        String[] params = agentId.split(":");
        if (params.length == 5) {
            this.provider = params[0];
            this.region = params[1];
            this.testPlanId = params[2];
            this.instanceId = params[3];
            this.instanceIp = params[4];
        }
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTestPlanId() {
        return testPlanId;
    }

    public void setTestPlanId(String testPlanId) {
        this.testPlanId = testPlanId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceIp() {
        return instanceIp;
    }

    public void setInstanceIp(String instanceIp) {
        this.instanceIp = instanceIp;
    }

    public String getInstanceUser() {
        return instanceUser;
    }

    public void setInstanceUser(String instanceUser) {
        this.instanceUser = instanceUser;
    }
}
