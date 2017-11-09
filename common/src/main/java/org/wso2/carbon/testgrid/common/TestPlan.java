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
    private String testRepoDir;
    private String infraRepoDir;
    private Status status;
    private TestReport testReport;
    private Deployment deployment;

    @Element(description = "value to uniquely iden4tify the TestPlan")
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
    @Element(description = "additional script to be run after infrastructure creation step")
    private Script infrastructureScript;
    @Element(description = "additional script to be run after deployment step")
    private Script deploymentScript;

    public enum Status {
        EXECUTION_PLANNED ("Execution Planned"), INFRASTRUCTURE_PREPARATION ("Infrastructure Preparation"),
        INFRASTRUCTURE_READY ("Infrastructure Ready"), INFRASTRUCTURE_ERROR ("Infrastructure Error"),
        INFRASTRUCTURE_DESTROY_ERROR ("Infrastructure Destroy Error"), DEPLOYMENT_PREPARATION ("Deployment Preparation"),
        DEPLOYMENT_READY ("Deployment Ready"), DEPLOYMENT_ERROR ("Deployment Error"),
        SCENARIO_EXECUTION ("Scenario Execution"), SCENARIO_EXECUTION_ERROR ("Scenario Execution Error"),
        SCENARIO_EXECUTION_COMPLETED ("Scenario Execution Completed"), REPORT_GENERATION ("Report Generation"),
        REPORT_GENERATION_ERROR ("Report Generation Error"), EXECUTION_COMPLETED ("Execution Completed");

        private final String name;

        Status(String status) {
            name = status;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum DeployerType {
        PUPPET ("puppet"), ANSIBLE ("ansible"), CHEF ("chef");

        private final String name;

        DeployerType(String deployer) {
            name = deployer;
        }

        public String toString() {
            return this.name;
        }
    }

    /**
     * Returns the id of the test plan.
     *
     * @return id of the test plan
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the test plan.
     *
     * @param id - Generated id of the test plan
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the list of TestScenarios available under this test plan.
     *
     * @return list of TestScenarios in the test plan
     */
    public List<TestScenario> getTestScenarios() {
        return testScenarios;
    }

    /**
     * Sets the test scenarios to this test plan.
     *
     * @param testScenarios - Generated id of the test plan
     */
    public void setTestScenarios(List<TestScenario> testScenarios) {
        this.testScenarios = testScenarios;
    }

    /**
     * Returns the home directory location of test plan.
     *
     * @return home directory location of the test plan
     */
    public String getHome() {
        return home;
    }

    /**
     * Sets the home directory location of test plan.
     *
     * @param home - home directory location of the test plan
     */
    public void setHome(String home) {
        this.home = home;
    }

    /**
     * Returns the created time of the test plan.
     *
     * @return the created time of the test plan
     */
    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    /**
     * Sets the created time of test plan.
     *
     * @param createdTimeStamp - created time of the test plan
     */
    public void setCreatedTimeStamp(long createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    /**
     * Returns the name of the test plan.
     *
     * @return the name of the test plan
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of test plan.
     *
     * @param name - name of the test plan
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the deployment pattern name of the test plan in which it was created.
     *
     * @return the deployment pattern name of the test plan
     */
    public String getDeploymentPattern() {
        return deploymentPattern;
    }

    /**
     * Sets the deployment pattern name of the test plan in which it was created.
     *
     * @param deploymentPattern - deployment pattern name of the test plan
     */
    public void setDeploymentPattern(String deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    /**
     * Returns the completed time of the test plan.
     *
     * @return the completed time of the test plan
     */
    public long getCompletedTimeStamp() {
        return completedTimeStamp;
    }

    /**
     * Sets the completed time of the test plan.
     *
     * @param completedTimeStamp - completed time of the test plan
     */
    public void setCompletedTimeStamp(long completedTimeStamp) {
        this.completedTimeStamp = completedTimeStamp;
    }

    /**
     * Returns the current status of the test plan.
     *
     * @return the status of the test plan
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the test plan.
     *
     * @param status - current status of the test plan
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the deployer-type (puppet/ansible) of the test plan.
     *
     * @return the deployer-type (puppet/ansible) of the test plan
     */
    public DeployerType getDeployerType() {
        return deployerType;
    }

    /**
     * Sets the deployer-type (puppet/ansible) of the test plan using the string value.
     *
     * @param deployerType - string deployer-type (puppet/ansible) of the test plan
     */
    public void setDeployerType(String deployerType) {
        this.deployerType = DeployerType.valueOf(deployerType.toUpperCase());
    }

    /**
     * Sets the deployer-type (puppet/ansible) of the test plan.
     *
     * @param deployerType - deployer-type (puppet/ansible) of the test plan
     */
    public void setDeployerType(DeployerType deployerType) {
        this.deployerType = deployerType;
    }

    /**
     * Returns the test execution results of this test plan.
     *
     * @return the test execution results of this test plan
     */
    public TestReport getTestReport() {
        return testReport;
    }

    /**
     * Sets the test execution results of this test plan.
     *
     * @param testReport - the test execution results of this test plan
     */
    public void setTestReport(TestReport testReport) {
        this.testReport = testReport;
    }

    /**
     * Returns the deployment information of the test plan.
     *
     * @return the deployment information of the test plan
     */
    public Deployment getDeployment() {
        return deployment;
    }

    /**
     * Sets the deployment information of the test plan.
     *
     * @param deployment - the deployment information of the test plan
     */
    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    /**
     * Returns if the test plan is enabled or not.
     *
     * @return if the test plan is enabled or not
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets if the test plan is enabled or not.
     *
     * @param enabled - test plan is enabled or not
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the description of the test plan.
     *
     * @return the name of the test plan
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the test plan.
     *
     * @param description - description of the test plan
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the path of the test plans' test artifacts.
     *
     * @return the path of the test plans' test artifacts.
     */
    public String getTestRepoDir() {
        return testRepoDir;
    }

    /**
     * Sets the path of the test plans' test artifacts.
     *
     * @param testRepoDir - the path of the test plans' test artifacts.
     */
    public void setTestRepoDir(String testRepoDir) {
        this.testRepoDir = testRepoDir;
    }

    /**
     * Returns the location of additional infrastructure scripts attached with this test plan.
     *
     * @return additional infrastructureScript script path
     */
    public Script getInfrastructureScript() {
        return infrastructureScript;
    }

    /**
     * Sets an additional infrastructure script path to this test plan to be executed after the general infrastructure
     * has completed.
     *
     * @param infrastructureScript - additional infrastructureScript script path
     */
    public void setInfrastructureScript(Script infrastructureScript) {
        this.infrastructureScript = infrastructureScript;
    }

    /**
     * Returns the location of additional deployment scripts attached with this test plan.
     *
     * @return the location of additional deployment scripts
     */
    public Script getDeploymentScript() {
        return deploymentScript;
    }

    /**
     * Sets an additional deployment script path to this test plan to be executed after the general deployment has
     * completed.
     *
     * @param deploymentScript - additional deployment script path
     */
    public void setDeploymentScript(Script deploymentScript) {
        this.deploymentScript = deploymentScript;
    }

    /**
     * Returns the path of the test plans' infrastructure artifacts.
     *
     * @return the path of the test plans' infrastructure artifacts
     */
    public String getInfraRepoDir() {
        return infraRepoDir;
    }

    /**
     * Sets the path of the test plans' infrastructure artifacts.
     *
     * @param infraRepoDir - the path of the test plans' infrastructure artifacts
     */
    public void setInfraRepoDir(String infraRepoDir) {
        this.infraRepoDir = infraRepoDir;
    }
}
