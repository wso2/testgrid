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
package org.wso2.testgrid.common.config;

/**
 * Represents a given test-plan that'll be read to run a
 * given test-run.
 *
 * @since 1.0.0
 */
public class TestPlan {

    private String version;
    private InfrastructureConfig infrastructureConfig = new InfrastructureConfig();
    private DeploymentConfig deploymentConfig = new DeploymentConfig();
    private ScenarioConfig scenarioConfig = new ScenarioConfig();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public InfrastructureConfig getInfrastructureConfig() {
        return infrastructureConfig;
    }

    public void setInfrastructureConfig(InfrastructureConfig infrastructureConfig) {
        this.infrastructureConfig = infrastructureConfig;
    }

    public ScenarioConfig getScenarioConfig() {
        return scenarioConfig;
    }

    public void setScenarioConfig(ScenarioConfig scenarioConfig) {
        this.scenarioConfig = scenarioConfig;
    }

    public DeploymentConfig getDeploymentConfig() {
        return deploymentConfig;
    }

    public void setDeploymentConfig(DeploymentConfig deploymentConfig) {
        this.deploymentConfig = deploymentConfig;
    }

}
