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

package org.wso2.carbon.testgrid.common;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.List;

/**
 * This represents a model of the TestPlan which includes all the necessary data to run the required SolutionPatterns.
 * A single deployment will have a single TestPlan.
 */
@Configuration(namespace = "wso2.testgrid.testplan", description = "TestGrid Testplan Configuration Parameters")
public class TestPlan {

    private int id;
    private long createdTimeStamp;
    private long completedTimeStamp;
    //Dir of TestPlan home directory
    private String home;
    private String repoDir;
    private Status status;
    private TestReport testReport;
    private Deployment deployment;

    @Element(description = "value to uniquely identify the TestPlan")
    private String name;

    @Element(description = "value to uniquely identify the deployment pattern")
    private String deploymentPattern;

    @Element(description = "list of test scenarios to be executed")
    private List<TestScenario> testScenarios;

    @Element(description = "type of the deployer (puppet/chef etc)")
    private DeployerType deployerType;

    @Element(description = "flag to enable or disable the testplan")
    private boolean enabled;

    @Element(description = "description about the test plan")
    private String description;

    @Element(description = "holds the configuration related to the infrastructure")
    private Infrastructure infrastructure;


    public enum Status {
        EXECUTION_PLANNED, INFRASTRUCTURE_PREPARATION, INFRASTRUCTURE_READY, INFRASTRUCTURE_ERROR, DEPLOYMENT_PREPARATION,
        DEPLOYMENT_READY, DEPLOYMENT_ERROR, SCENARIO_EXECUTION, SCENARIO_EXECUTION_ERROR, SCENARIO_EXECUTION_COMPLETED,
        REPORT_GENERATION, REPORT_GENERATION_ERROR, EXECUTION_COMPLETED
    }

    public enum DeployerType {
        PUPPET ("puppet"), ANSIBLE ("ansible"), CHEF ("chef");

        private final String name;

        DeployerType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TestScenario> getTestScenarios() {
        return testScenarios;
    }

    public void setTestScenarios(List<TestScenario> testScenarios) {
        this.testScenarios = testScenarios;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(long createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeploymentPattern() {
        return deploymentPattern;
    }

    public void setDeploymentPattern(String deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    public long getCompletedTimeStamp() {
        return completedTimeStamp;
    }

    public void setCompletedTimeStamp(long completedTimeStamp) {
        this.completedTimeStamp = completedTimeStamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public DeployerType getDeployerType() {
        return deployerType;
    }

    public void setDeployerType(DeployerType deployerType) {
        this.deployerType = deployerType;
    }

    public TestReport getTestReport() {
        return testReport;
    }

    public void setTestReport(TestReport testReport) {
        this.testReport = testReport;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDeployerType(String deployerType) {
        this.deployerType = DeployerType.valueOf(deployerType.toUpperCase());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }

    public String getRepoDir() {
        return repoDir;
    }

    public void setRepoDir(String repoDir) {
        this.repoDir = repoDir;
    }
}
