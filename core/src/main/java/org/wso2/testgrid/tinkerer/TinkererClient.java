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
package org.wso2.testgrid.tinkerer;


import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.tinkerer.exception.TinkererOperationException;

/**
 *This is an abstraction for the deployment tinkerer client.
 *
 * @since 1.0.0
 */
public abstract class TinkererClient {

    private String tinkererBase;
    private String tinkererUserName;
    private String tinkererPassword;

    /**
     *This constructor will save the current deployment tinkerer properties as a super class.
     *
     */
    TinkererClient() {
        this.tinkererBase = ConfigurationContext.getProperty(ConfigurationContext
                .ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH);
        this.tinkererUserName = ConfigurationContext.getProperty(ConfigurationContext
                .ConfigurationProperties.DEPLOYMENT_TINKERER_USERNAME);
        this.tinkererPassword = ConfigurationContext.getProperty(ConfigurationContext
                .ConfigurationProperties.DEPLOYMENT_TINKERER_PASSWORD);
    }

    public String getTinkererBase() {
        return tinkererBase;
    }

    public void setTinkererBase(String tinkererBase) {
        this.tinkererBase = tinkererBase;
    }

    public String getTinkererUserName() {
        return tinkererUserName;
    }

    public void setTinkererUserName(String tinkererUserName) {
        this.tinkererUserName = tinkererUserName;
    }

    public String getTinkererPassword() {
        return tinkererPassword;
    }

    public void setTinkererPassword(String tinkererPassword) {
        this.tinkererPassword = tinkererPassword;
    }

    /**
     * This method will download all the log files from the instance.
     * The log files location will be derived from Test type and Operating system.
     * all the files inside the derived log location will be downloaded to the destination
     *
     * @param deploymentCreationResult the output form the deployment stage that contains the information
     *                                 about the instances.
     * @param testPlan The Test plan object for the job
     * @throws TinkererOperationException When there is an error downloading the logs.
     */
    public abstract void downloadLogs(DeploymentCreationResult deploymentCreationResult, TestPlan testPlan)
            throws TinkererOperationException;
}

