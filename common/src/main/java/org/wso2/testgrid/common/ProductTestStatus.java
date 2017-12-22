/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.testgrid.common;

import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * This represents a model of the Product with its recent test statuses.
 *
 * @since 1.0.0
 */
@Entity
public class ProductTestStatus {

    @Id
    private String id;
    private String name;
    private String version;
    private String channel;
    private String deploymentPattern;
    private String deploymentPatternId;
    private String status;
    private Timestamp testExecutionTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDeploymentPattern() {
        return deploymentPattern;
    }

    public void setDeploymentPattern(String deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getTestExecutionTime() {
        if (testExecutionTime != null) {
            return new Timestamp(testExecutionTime.getTime());
        }
        return new Timestamp(new Date().getTime());
    }

    public void setTestExecutionTime(Timestamp testExecutionTime) {
        if (testExecutionTime != null) {
            this.testExecutionTime = new Timestamp(testExecutionTime.getTime());
        } else {
            this.testExecutionTime = new Timestamp(new Date().getTime());
        }
    }

    public String getDeploymentPatternId() {
        return deploymentPatternId;
    }

    public void setDeploymentPatternId(String deploymentPatternId) {
        this.deploymentPatternId = deploymentPatternId;
    }
}
