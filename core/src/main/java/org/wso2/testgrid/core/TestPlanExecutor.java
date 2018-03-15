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

package org.wso2.testgrid.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.exception.DeployerInitializationException;
import org.wso2.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.UnsupportedDeployerException;
import org.wso2.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;

import java.nio.file.Paths;

/**
 * This class is responsible for executing the provided TestPlan.
 *
 * @since 1.0.0
 */
public class TestPlanExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestPlanExecutor.class);
    private TestScenarioUOW testScenarioUOW;
    private TestPlanUOW testPlanUOW;
    private ScenarioExecutor scenarioExecutor;

    public TestPlanExecutor() {
        testPlanUOW = new TestPlanUOW();
        scenarioExecutor = new ScenarioExecutor();
        testScenarioUOW = new TestScenarioUOW();
    }

    public TestPlanExecutor(ScenarioExecutor scenarioExecutor, TestPlanUOW testPlanUOW,
            TestScenarioUOW testScenarioUOW) {
        this.scenarioExecutor = scenarioExecutor;
        this.testPlanUOW = testPlanUOW;
        this.testScenarioUOW = testScenarioUOW;
    }

    /**
     * This method executes a given {@link TestPlan}.
     *
     * @param testPlan an instance of {@link TestPlan} in which the tests should be executed
     * @throws TestPlanExecutorException thrown when error on executing test plan
     */
    public void execute(TestPlan testPlan, InfrastructureConfig infrastructureConfig)
            throws TestPlanExecutorException {
        // Provision infrastructure
        InfrastructureProvisionResult infrastructureProvisionResult = provisionInfrastructure(infrastructureConfig,
                testPlan);

        // Create and set deployment.
        DeploymentCreationResult deploymentCreationResult = createDeployment(infrastructureConfig, testPlan,
                infrastructureProvisionResult);

        // Run test scenarios.
        runScenarioTests(testPlan, deploymentCreationResult);

        // Test plan completed.
        setTestPlanStatus(testPlan);

        //cleanup
        releaseInfrastructure(testPlan, infrastructureProvisionResult, deploymentCreationResult);
    }

    /**
     * Run all the scenarios mentioned in the testgrid.yaml.
     *
     * @param testPlan the test plan
     * @param deploymentCreationResult the result of the previous build step
     */
    private void runScenarioTests(TestPlan testPlan, DeploymentCreationResult deploymentCreationResult) {
        for (TestScenario testScenario : testPlan.getTestScenarios()) {
            try {
                scenarioExecutor.execute(testScenario, deploymentCreationResult, testPlan);
            } catch (Exception e) {
                // we need to continue the rest of the tests irrespective of the exception thrown here.
                setTestPlanStatus(testPlan, Status.FAIL);
                logger.error(StringUtil.concatStrings("Error occurred while executing the SolutionPattern '",
                        testScenario.getName(), "' , in TestPlan"), e);
            }
        }
    }

    /**
     * Creates the deployment in the provisioned infrastructure.
     *
     * @param infrastructureConfig          infrastructure to create the deployment
     * @param testPlan                      test plan
     * @param infrastructureProvisionResult the output of the infrastructure provisioning scripts
     * @return created {@link DeploymentCreationResult}
     * @throws TestPlanExecutorException thrown when error on creating deployment
     */
    private DeploymentCreationResult createDeployment(InfrastructureConfig infrastructureConfig, TestPlan testPlan,
            InfrastructureProvisionResult infrastructureProvisionResult)
            throws TestPlanExecutorException {
        try {
            if (!infrastructureProvisionResult.isSuccess()) {
                DeploymentCreationResult deploymentCreationResult = new DeploymentCreationResult();
                deploymentCreationResult.setSuccess(false);
                return deploymentCreationResult;
            }

            Deployer deployerService = DeployerFactory.getDeployerService(testPlan);
            return deployerService.deploy(testPlan, infrastructureProvisionResult);
        } catch (TestGridDeployerException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("Exception occurred while running the deployment for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan");
            logger.error(msg, e);
        } catch (DeployerInitializationException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("Unable to locate a Deployer Service implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan '");
            logger.error(msg, e);
        } catch (UnsupportedDeployerException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("Error occurred while running deployment for deployment pattern '",
                            testPlan.getDeploymentPattern(), "' in TestPlan");
            logger.error(msg, e);
        } catch (Exception e) {
            // deployment creation should not interrupt other tasks.
            setTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil.concatStrings("Unhandled error occurred hile running deployment for deployment "
                    + "pattern '", testPlan.getDeploymentConfig(), "' in TestPlan");
            logger.error(msg, e);
        }

        DeploymentCreationResult deploymentCreationResult = new DeploymentCreationResult();
        deploymentCreationResult.setSuccess(false);
        return deploymentCreationResult;
    }

    /**
     * Sets up infrastructure for the given {@link InfrastructureConfig}.
     *
     * @param infrastructureConfig infrastructure to set up
     * @param testPlan             test plan
     */
    private InfrastructureProvisionResult provisionInfrastructure(InfrastructureConfig infrastructureConfig,
            TestPlan testPlan) {
        try {
            if (infrastructureConfig == null) {
                setTestPlanStatus(testPlan, Status.FAIL);
                throw new TestPlanExecutorException(StringUtil
                        .concatStrings("Unable to locate infrastructure descriptor for deployment pattern '",
                                testPlan.getDeploymentPattern(), "', in TestPlan"));
            }
            InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                    .getInfrastructureProvider(infrastructureConfig);
            infrastructureProvider.init();
            InfrastructureProvisionResult provisionResult = infrastructureProvider.provision(testPlan);

            provisionResult.setName(infrastructureConfig.getProvisioners().get(0).getName());
            //TODO: remove. deploymentScriptsDir is deprecated now in favor of DeploymentConfig.
            provisionResult.setDeploymentScriptsDir(Paths.get(testPlan.getDeploymentRepository()).toString());
            return provisionResult;
        } catch (TestGridInfrastructureException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("Error on infrastructure creation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan");
            logger.error(msg, e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan");
            logger.error(msg, e);
        } catch (Exception e) {
            // Catching the Exception here since we need to catch and gracefully handle all exceptions.
            setTestPlanStatus(testPlan, Status.FAIL);
            logger.error("Unknown exception while provisioning the infrastructure: " + e.getMessage(), e);
        }

        InfrastructureProvisionResult infrastructureProvisionResult = new InfrastructureProvisionResult();
        infrastructureProvisionResult.setSuccess(false);
        return infrastructureProvisionResult;
    }

    /**
     * Destroys the given {@link InfrastructureConfig}.
     *
     * @param testPlan                      test plan
     * @param infrastructureProvisionResult the infrastructure provisioning result
     * @param deploymentCreationResult      the deployment creation result
     * @throws TestPlanExecutorException thrown when error on destroying
     *                                   infrastructure
     */
    private void releaseInfrastructure(TestPlan testPlan,
            InfrastructureProvisionResult infrastructureProvisionResult,
            DeploymentCreationResult deploymentCreationResult)
            throws TestPlanExecutorException {
        try {
            InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
            if (!infrastructureProvisionResult.isSuccess() || !deploymentCreationResult.isSuccess()) {
                logger.error("Execution of previous steps failed. Trying to release the possibly provisioned "
                        + "infrastructure");
            }
            InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                    .getInfrastructureProvider(infrastructureConfig);
            infrastructureProvider.release(infrastructureConfig, testPlan.getInfrastructureRepository());
        } catch (TestGridInfrastructureException e) {
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error on infrastructure removal for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        }
    }

    /**
     * Persists the test plan with the status.
     *
     * @param testPlan TestPlan object to persist
     */
    private void setTestPlanStatus(TestPlan testPlan) {
        try {
            Status status = isFailedTestScenariosExist(testPlan) ? Status.FAIL : Status.SUCCESS;
            setTestPlanStatus(testPlan, status);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }

    /**
     * Persists the test plan with the status.
     *
     * @param testPlan TestPlan object to persist
     * @param status   the status to set
     */
    private void setTestPlanStatus(TestPlan testPlan, Status status) {
        try {
            testPlan.setStatus(status);
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }

    /**
     * Returns whether failed test scenarios exists.
     *
     * @param testPlan test plan to check whether failed test scenarios exist
     * @return {@code true} if failed test scenarios exist, {@code false} otherwise
     * @throws TestGridDAOException thrown when error on getting status of test scenarios
     */
    private boolean isFailedTestScenariosExist(TestPlan testPlan) throws TestGridDAOException {
        return testScenarioUOW.isFailedTestScenariosExist(testPlan);
    }
}
