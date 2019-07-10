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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
public class TestPlan extends AbstractUUIDEntity implements Serializable, Cloneable {
    private static final Logger logger = LoggerFactory.getLogger(TestPlan.class);

    /**
     * TestPlan table name.
     */
    public static final String TEST_PLAN_TABLE = "test_plan";

    /**
     * Column names of the table.
     */
    public static final String STATUS_COLUMN = "status";
    public static final String DEPLOYMENT_PATTERN_COLUMN = "deploymentPattern";
    public static final String TESTRUN_NUMBER_COLUMN = "testRunNumber";

    private static final long serialVersionUID = 9208083074380972876L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TestPlanStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase", length = 100)
    private TestPlanPhase phase;

    @Column(name = "infra_parameters")
    private String infraParameters;

    @Column(name = "test_run_number")
    private int testRunNumber;

    @Column(name = "log_url")
    private String logUrl;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = DeploymentPattern.class,
               fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "DEPLOYMENTPATTERN_id", referencedColumnName = ID_COLUMN)
    private DeploymentPattern deploymentPattern;

    @OneToMany(mappedBy = "testPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestScenario> testScenarios = new ArrayList<>();

    @Transient
    private DeployerType deployerType = DeployerType.SHELL;

    @Transient
    private InfrastructureConfig infrastructureConfig = new InfrastructureConfig();

    @Transient
    private DeploymentConfig deploymentConfig = new DeploymentConfig();

    @Transient
    private List<ScenarioConfig> scenarioConfigs = new ArrayList<>();

    @Transient
    private Properties infrastructureProperties = new Properties();

    @Transient
    private String jobName;

    @Transient
    private String infrastructureRepository;

    @Transient
    private String deploymentRepository;

    @Transient
    private String scenarioTestsRepository;

    @Transient
    private Properties jobProperties = new Properties();

    @Transient
    private String configChangeSetRepository;

    @Transient
    private String configChangeSetBranchName;

    @Transient
    private ResultFormat resultFormat;

    @Transient
    private String keyFileLocation;

    @Transient
    private InfrastructureProvisionResult infrastructureProvisionResult;

    @Transient
    private DeploymentCreationResult deploymentCreationResult;

    @Transient
    private String emailToList;

    @Transient
    private String workspace = Paths.get(TestGridUtil.getTestGridHomePath(), TestGridConstants.TESTGRID_JOB_DIR,
            "sample-").toString();

    @Column (name = "build_url")
    private String buildURL;

    /**
     * Returns the phase of the test-plan
     *
     */
    public TestPlanPhase getPhase() {
        return phase;
    }

    /**
     * Set the phase of the test-plan
     */
    public void setPhase(TestPlanPhase testPlanPhase) {
        this.phase = testPlanPhase;
    }

    /**
     * Returns the status of the infrastructure.
     *
     * @return infrastructure status
     */
    public TestPlanStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the infrastructure.
     *
     * @param status infrastructure status
     */
    public void setStatus(TestPlanStatus status) {
        this.status = status;
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
     * Returns the test run number.
     *
     * @return test run number
     */
    public int getTestRunNumber() {
        return testRunNumber;
    }

    /**
     * Sets the test run number.
     *
     * @param testRunNumber test run number
     */
    public void setTestRunNumber(int testRunNumber) {
        this.testRunNumber = testRunNumber;
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

    public InfrastructureConfig getInfrastructureConfig() {
        return infrastructureConfig;
    }

    public void setInfrastructureConfig(InfrastructureConfig infrastructureConfig) {
        this.infrastructureConfig = infrastructureConfig;
    }

    /**
     * Returns the deployment configuration in the test-plan.
     *
     * @return the deployment configuration of the test plan
     */
    public DeploymentConfig getDeploymentConfig() {
        return deploymentConfig;
    }

    /**
     * Sets the deployment configuration of the test plan.
     *
     * @param deploymentConfig the deployment information of the test plan
     */
    public void setDeploymentConfig(DeploymentConfig deploymentConfig) {
        this.deploymentConfig = deploymentConfig;
    }

    public List<ScenarioConfig> getScenarioConfigs() {
        return scenarioConfigs;
    }

    public void setScenarioConfigs(List<ScenarioConfig> scenarioConfig) {
        this.scenarioConfigs = scenarioConfig;
    }

    /**
     * Returns the path of the test plans' test artifacts.
     *
     * @return the path of the test plans' test artifacts.
     */
    public String getScenarioTestsRepository() {
        return scenarioTestsRepository;
    }

    /**
     * Sets the path of the test plans' test artifacts.
     *
     * @param scenarioTestsRepository the path of the test plans' test artifacts.
     */
    public void setScenarioTestsRepository(String scenarioTestsRepository) {
        this.scenarioTestsRepository = scenarioTestsRepository;
    }

    /**
     * Return the path of config change set artifacts.
     *
     * @return the path of config change set artifacts.
     */
    public String getConfigChangeSetRepository() {
        return configChangeSetRepository;
    }

    /**
     * Sets the path of the config change set artifacts.
     *
     * @param configChangeSetRepository the path of config change set artifacts.
     */
    public void setConfigChangeSetRepository(String configChangeSetRepository) {
        this.configChangeSetRepository = configChangeSetRepository;
    }

    /**
     * Get config change set repository branch name
     *
     * @return
     */
    public String getConfigChangeSetBranchName() {
        return configChangeSetBranchName;
    }

    /**
     * Set config change set repository branch name
     *
     * @param configChangeSetBranchName
     */
    public void setConfigChangeSetBranchName(String configChangeSetBranchName) {
        this.configChangeSetBranchName = configChangeSetBranchName;
    }

    /**
     * Returns the path of the test plans' infrastructure artifacts.
     *
     * @return the path of the test plans' infrastructure artifacts
     */
    public String getInfrastructureRepository() {
        return infrastructureRepository;
    }

    /**
     * Sets the path of the test plans' infrastructure artifacts.
     *
     * @param infrastructureRepository the path of the test plans' infrastructure artifacts
     */
    public void setInfrastructureRepository(String infrastructureRepository) {
        this.infrastructureRepository = infrastructureRepository;
    }

    /**
     * Returns the path of the deployment repository.
     *
     * @return the path of the test plans' infrastructure artifacts
     */
    public String getDeploymentRepository() {
        return deploymentRepository;
    }

    /**
     * Sets the path of the deployment repository, which contains deploy.sh.
     *
     * @param deploymentRepository the path of the deployment repository
     */
    public void setDeploymentRepository(String deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    /**
     * Get the list of build properties associated with this test plan.
     * The build properties are currently contain properties received via
     * {@link org.wso2.testgrid.common.config.JobConfigFile#getProperties()}.
     *
     * @return build proprties
     */
    public Properties getJobProperties() {
        return jobProperties;
    }

    /**
     * See {@link #getJobProperties()}
     *
     * @param jobProperties build properties
     */
    public void setJobProperties(Properties jobProperties) {
        this.jobProperties = jobProperties;
    }

    /**
     * Returns the ResultFormatter object fot the specific TestPlan
     *
     * @return ResultFormatter object of TestPlan
     */
    public ResultFormat getResultFormat() {
        return resultFormat;
    }

    /**
     * Set the ResultFormatter object for the TestPlan
     *
     * @param resultFormat ResultFormatter object to be set
     */
    public void setResultFormat(ResultFormat resultFormat) {
        this.resultFormat = resultFormat;
    }

    /**
     * Get the location of the key file used to access the instances.
     *
     * @return location of the key file
     */
    public String getKeyFileLocation() {
        return keyFileLocation;
    }

    /**
     * Sets the location of the key file.
     *
     * @param keyFileLocation location of the key file
     */
    public void setKeyFileLocation(String keyFileLocation) {
        this.keyFileLocation = keyFileLocation;
    }

    /**
     * Returns the URL to view WSO2 server logs.
     *
     * @return Log location URL
     */
    public String getLogUrl() {
        return logUrl;
    }

    /**
     * Returns the URL to view WSO2 server logs.
     *
     * @param logUrl Log location URL
     */
    public void setLogUrl(String logUrl) {
        this.logUrl = logUrl;
    }

    @Override
    public String toString() {
        String id = this.getId() != null ? this.getId() : "";
        return StringUtil.concatStrings("TestPlan{",
                "id='", id, "\'",
                ", status='", status, "\'",
                ", infraParams='", infraParameters, "\'",
                ", testRunNumber='", testRunNumber, "\'",
                ", deploymentPattern='", deploymentPattern, "\'",
                '}');
    }

    @Override
    public TestPlan clone() {
        try {
            TestPlan testPlan = (TestPlan) super.clone();
            testPlan.setConfigChangeSetRepository(configChangeSetRepository);
            testPlan.setConfigChangeSetBranchName(configChangeSetBranchName);
            testPlan.setDeployerType(deployerType);
            testPlan.setDeploymentConfig(deploymentConfig);
            testPlan.setDeploymentPattern(deploymentPattern);
            testPlan.setDeploymentRepository(deploymentRepository);
            testPlan.setInfraParameters(infraParameters);
            testPlan.setInfrastructureRepository(infrastructureRepository);
            testPlan.setInfrastructureConfig(infrastructureConfig);
            testPlan.setScenarioConfigs(scenarioConfigs);
            testPlan.setStatus(status);
            testPlan.setScenarioTestsRepository(scenarioTestsRepository);
            testPlan.setTestRunNumber(testRunNumber);
            testPlan.setTestScenarios(testScenarios);

            return testPlan;
        } catch (CloneNotSupportedException e) {
            throw new TestGridError("Since the super class of this object is java.lang.Object that supports cloning, "
                    + "this failure condition should never happen unless a serious system error occurred.", e);
        }

    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Return the workspace of the TestPlan.
     * If the workspace is not set it will be derived as below;
     *      1. Derived using the job-name
     *      2. If job-name is not set, derive using product-name
     *      3. If both (1) and (2) is not possible, consider job directory as "product-<random-number>"
     *      (The random directory will be generated only one-time and will be used as its workspace from there onwards.)
     */
    public String getWorkspace() {
        if (StringUtil.isStringNullOrEmpty(workspace)) {
            String jobDir = getJobName();
            if (StringUtil.isStringNullOrEmpty(jobDir)) {
                if (getDeploymentPattern() != null && getDeploymentPattern().getProduct() != null) {
                    jobDir = getDeploymentPattern().getProduct().getName();
                }
                if (StringUtil.isStringNullOrEmpty(jobDir)) {
                    jobDir = "product-" + StringUtil.generateRandomString(5);
                }
            }
            this.setWorkspace(Paths.get(TestGridUtil.getTestGridHomePath(), TestGridConstants.TESTGRID_JOB_DIR,
                    jobDir).toString());
            logger.warn("Test-plan " + this.toString() + " does not stick to a workspace directory. " +
                    "Hence the directory '" + workspace + "' is set as workspace and will be used from here onwards.");
        }
            return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getEmailToList() {
        return emailToList;
    }

    public void setEmailToList(String emailToList) {
        this.emailToList = emailToList;
    }

    public void setBuildURL(String buildURL) {
        this.buildURL = buildURL;
    }

    public String getBuildURL() {
        return buildURL;
    }

    public InfrastructureProvisionResult getInfrastructureProvisionResult() {
        return infrastructureProvisionResult;
    }

    public void setInfrastructureProvisionResult(InfrastructureProvisionResult infrastructureProvisionResult) {
        this.infrastructureProvisionResult = infrastructureProvisionResult;
    }

    public DeploymentCreationResult getDeploymentCreationResult() {
        return deploymentCreationResult;
    }

    public void setDeploymentCreationResult(DeploymentCreationResult deploymentCreationResult) {
        this.deploymentCreationResult = deploymentCreationResult;
    }

    public Properties getInfrastructureProperties() {
        return infrastructureProperties;
    }

    public void setInfrastructureProperties(Properties infrastructureProperties) {
        this.infrastructureProperties = infrastructureProperties;
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
        CHEF("CHEF"),

        /**
         * Defines the Kubernetes based deployment
         */
        KUBERNETES("KUBERNETES"),

        /**
         * Defines the Helm based deployment
         */
        HELM("HELM"),

        /**
         * Defines the Shell based deployment
         */
        SHELL("SHELL");

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
