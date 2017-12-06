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

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.config.annotation.Ignore;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * This represents a model of the TestPlan which includes all the necessary data to run the required SolutionPatterns.
 * <p>
 * A single deployment will have a single TestPlan.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = TestPlan.TEST_PLAN_TABLE)
@Configuration(namespace = "wso2.testgrid.testplan", description = "TestGrid Testplan Configuration Parameters")
public class TestPlan extends AbstractUUIDEntity implements Serializable {

    /**
     * Test plan table name.
     */
    public static final String TEST_PLAN_TABLE = "test_plan";

    /**
     * Column names of the table.
     */
    public static final String NAME_COLUMN = "name";
    public static final String START_TIMESTAMP_COLUMN = "startTimestamp";
    public static final String MODIFIED_TIMESTAMP_COLUMN = "modifiedTimestamp";
    public static final String STATUS_COLUMN = "status";
    public static final String DEPLOYMENT_PATTERN_COLUMN = "deploymentPattern";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String INFRA_RESULT_COLUMN = "infraResult";
    public static final String PRODUCT_TEST_PLAN_COLUMN = "productTestPlan";

    private static final long serialVersionUID = -4345126378695708155L;

    @Element(description = "value to uniquely identify the TestPlan")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Ignore
    private Timestamp startTimestamp;

    @Column(name = "modified_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Ignore
    private Timestamp modifiedTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Ignore
    private Status status;

    @Element(description = "value to uniquely identify the deployment pattern")
    @Column(name = "deployment_pattern", nullable = true)
    private String deploymentPattern;

    @Element(description = "description about the test plan")
    @Column(name = "description")
    private String description;

    @Ignore
    @OneToOne(optional = false, cascade = CascadeType.ALL, targetEntity = InfraResult.class)
    private InfraResult infraResult;

    @Ignore
    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = ProductTestPlan.class)
    @PrimaryKeyJoinColumn(name = "PRODUCTTESTPLAN_id", referencedColumnName = ID_COLUMN)
    private ProductTestPlan productTestPlan;

    @Transient
    @Element(description = "list of test scenarios to be executed")
    private List<TestScenario> testScenarios;

    @Transient
    @Element(description = "type of the deployer (puppet/chef etc)")
    private DeployerType deployerType;

    @Transient
    private Deployment deployment;

    @Transient
    @Element(description = "flag to enable or disable the testplan")
    private boolean enabled;

    @Transient
    private String testRepoDir;

    @Transient
    private String infraRepoDir;

    @Transient
    @Element(description = "additional script to be run after infrastructure creation step")
    private Script infrastructureScript;

    @Transient
    @Element(description = "additional script to be run after deployment step")
    private Script deploymentScript;

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
     * @param name name of the test plan
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the start time of the test plan.
     *
     * @return the start time of the test plan
     */
    public Timestamp getStartTimestamp() {
        return new Timestamp(startTimestamp.getTime());
    }

    /**
     * Sets the start time of test plan.
     *
     * @param startTimestamp start time of the test plan
     */
    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = new Timestamp(startTimestamp.getTime());
    }

    /**
     * Returns the modified time of the test plan.
     *
     * @return the modified time of the test plan
     */
    public Timestamp getModifiedTimestamp() {
        return new Timestamp(modifiedTimestamp.getTime());
    }

    /**
     * Sets the modified time of the test plan.
     *
     * @param modifiedTimestamp modified time of the test plan
     */
    public void setModifiedTimestamp(Timestamp modifiedTimestamp) {
        this.modifiedTimestamp = new Timestamp(modifiedTimestamp.getTime());
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
     * @param status current status of the test plan
     */
    public void setStatus(Status status) {
        this.status = status;
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
     * @param deploymentPattern deployment pattern name of the test plan
     */
    public void setDeploymentPattern(String deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
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
     * @param description description of the test plan
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the infra result for the test plan.
     *
     * @return infra result for the test plan
     */
    public InfraResult getInfraResult() {
        return infraResult;
    }

    /**
     * Sets the infra result for the test plan.
     *
     * @param infraResult infra result for the test plan
     */
    public void setInfraResult(InfraResult infraResult) {
        this.infraResult = infraResult;
    }

    /**
     * Returns the product test plan associated with the test plan.
     *
     * @return product test plan associated with the test plan
     */
    public ProductTestPlan getProductTestPlan() {
        return productTestPlan;
    }

    /**
     * Sets the product test plan associated with the test plan.
     *
     * @param productTestPlan product test plan associated with the test plan
     */
    public void setProductTestPlan(ProductTestPlan productTestPlan) {
        this.productTestPlan = productTestPlan;
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
     * @param testScenarios Generated id of the test plan
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
     * @param enabled test plan is enabled or not
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    @Override
    public String toString() {
        return "TestPlan{" +
               "id='" + this.getId() + '\'' +
               ", name='" + name + '\'' +
               ", startTimestamp=" + startTimestamp +
               ", modifiedTimestamp=" + modifiedTimestamp +
               ", status=" + status +
               ", deploymentPattern='" + deploymentPattern + '\'' +
               ", description='" + description + '\'' +
               ", productTestPlan=" + productTestPlan +
               ", testScenarios=" + testScenarios +
               ", deployerType=" + deployerType +
               ", deployment=" + deployment +
               ", enabled=" + enabled +
               ", testRepoDir='" + testRepoDir + '\'' +
               ", infraRepoDir='" + infraRepoDir + '\'' +
               ", infrastructureScript=" + infrastructureScript +
               ", deploymentScript=" + deploymentScript +
               '}';
    }

    /**
     * This defines the possible statuses of the TestPlan.
     *
     * @since 1.0.0
     */
    public enum Status {

        /**
         * TestPlan execution has planned.
         */
        TESTPLAN_PENDING("TESTPLAN_PENDING"),

        /**
         * TestPlan execution error.
         */
        TESTPLAN_ERROR("TESTPLAN_ERROR"),

        /**
         * Product deployment for the TestPlan execution is being prepared.
         */
        TESTPLAN_DEPLOYMENT_PREPARATION("TESTPLAN_DEPLOYMENT_PREPARATION"),

        /**
         * Product deployment for the TestPlan execution is ready to use.
         */
        TESTPLAN_DEPLOYMENT_READY("TESTPLAN_DEPLOYMENT_READY"),

        /**
         * There was an error when deploying the products for the TestPlan.
         */
        TESTPLAN_DEPLOYMENT_ERROR("TESTPLAN_DEPLOYMENT_ERROR"),

        /**
         * TestPlan execution has completed.
         */
        TESTPLAN_COMPLETED("TESTPLAN_COMPLETED");

        private final String status;

        /**
         * Sets the status of the test plan.
         *
         * @param status test plan status
         */
        Status(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return this.status;
        }
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
}
