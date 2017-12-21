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

import org.wso2.testgrid.common.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Defines a model object of TestPlan with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = TestPlan.TEST_PLAN_TABLE)
public class TestPlan extends AbstractUUIDEntity implements Serializable {

    /**
     * TestPlan table name.
     */
    public static final String TEST_PLAN_TABLE = "test_plan";

    /**
     * Column names of the table.
     */
    public static final String STATUS_COLUMN = "status";
    public static final String LOG_LOCATION_COLUMN = "logLocation";
    public static final String DEPLOYMENT_PATTERN_COLUMN = "deploymentPattern";

    private static final long serialVersionUID = 9208083074380972876L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status;

    @Column(name = "log_location")
    private String logLocation;

    @Column(name = "infra_parameters")
    private String infraParameters;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = DeploymentPattern.class,
               fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "DEPLOYMENTPATTERN_id", referencedColumnName = ID_COLUMN)
    private DeploymentPattern deploymentPattern;

    @OneToMany(mappedBy = "testPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestScenario> testScenarios = new ArrayList<>();

    @Transient
    private DeployerType deployerType;

    @Transient
    private Deployment deployment;

    @Transient
    private String testRepoDir;

    @Transient
    private String infraRepoDir;

    @Transient
    private Script infrastructureScript;

    @Transient
    private Script deploymentScript;

    /**
     * Returns the status of the infrastructure.
     *
     * @return infrastructure status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the infrastructure.
     *
     * @param status infrastructure status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the location of the log file
     *
     * @return log file location
     */
    public String getLogLocation() {
        return logLocation;
    }

    /**
     * Sets the location of the log file.
     *
     * @param logLocation log file location
     */
    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * Returns the infra parameters.
     *
     * @return infra parameters
     */
    public String getInfraParameters() {
        return infraParameters;
    }

    /**
     * Sets the infra parameters.
     *
     * @param infraParameters infra parameters
     */
    public void setInfraParameters(String infraParameters) {
        this.infraParameters = infraParameters;
    }

    /**
     * Returns the deployment pattern associated with.
     *
     * @return deployment pattern associated with
     */
    public DeploymentPattern getDeploymentPattern() {
        return deploymentPattern;
    }

    /**
     * Sets the deployment pattern associated with.
     *
     * @param deploymentPattern deployment pattern associated with
     */
    public void setDeploymentPattern(DeploymentPattern deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    /**
     * Returns the associated test scenarios.
     *
     * @return associated test scenarios
     */
    public List<TestScenario> getTestScenarios() {
        return testScenarios;
    }

    /**
     * Sets the associated test scenarios.
     *
     * @param testScenarios associated test scenarios
     */
    public void setTestScenarios(List<TestScenario> testScenarios) {
        this.testScenarios = testScenarios;
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
     * Sets the deployer-type (puppet/ansible) of the test plan.
     *
     * @param deployerType deployer-type (puppet/ansible) of the test plan
     */
    public void setDeployerType(DeployerType deployerType) {
        this.deployerType = deployerType;
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
     * @param deployment the deployment information of the test plan
     */
    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
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
     * @param testRepoDir the path of the test plans' test artifacts.
     */
    public void setTestRepoDir(String testRepoDir) {
        this.testRepoDir = testRepoDir;
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
     * @param infraRepoDir the path of the test plans' infrastructure artifacts
     */
    public void setInfraRepoDir(String infraRepoDir) {
        this.infraRepoDir = infraRepoDir;
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
     * @param infrastructureScript additional infrastructureScript script path
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
     * @param deploymentScript additional deployment script path
     */
    public void setDeploymentScript(Script deploymentScript) {
        this.deploymentScript = deploymentScript;
    }

    /**
     * This defines the supported deployment automation tools.
     *
     * @since 1.0.0
     */
    public enum DeployerType {

        /**
         * Defines the puppet automation.
         */
        PUPPET("PUPPET"),

        /**
         * Defines the ansible automation.
         */
        ANSIBLE("ANSIBLE"),

        /**
         * Defines the ansible automation.
         */
        AWS_CF("AWS_CF"),

        /**
         * Defines the chef automation.
         */
        CHEF("CHEF");

        private final String deployerType;

        /**
         * Sets the deployer type of the test plan.
         *
         * @param deployerType deployer type of the test plan
         */
        DeployerType(String deployerType) {
            this.deployerType = deployerType;
        }

        @Override
        public String toString() {
            return this.deployerType;
        }
    }

    @Override
    public String toString() {
        return StringUtil.concatStrings("TestPlan{",
                "id='", this.getId(), "\'",
                ", status='", status, "\'",
                ", logLocation='", logLocation, "\'",
                ", createdTimestamp='", this.getCreatedTimestamp(), "\'",
                ", modifiedTimestamp='", this.getModifiedTimestamp(), "\'",
                ", deploymentPattern='", deploymentPattern, "\'",
                '}');
    }
}
