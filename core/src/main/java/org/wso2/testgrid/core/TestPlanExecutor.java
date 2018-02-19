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
import org.wso2.testgrid.core.exception.ScenarioExecutorException;
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

    /**
     * This method executes a given {@link TestPlan}.
     *
     * @param testPlan an instance of {@link TestPlan} in which the tests should be executed
     * @throws TestPlanExecutorException thrown when error on executing test plan
     */
    public void execute(TestPlan testPlan, InfrastructureConfig infrastructureConfig)
            throws TestPlanExecutorException {
        InfrastructureProvisionResult infrastructureProvisionResult = provisionInfrastructure(infrastructureConfig,
                testPlan);

        // Create and set deployment.
        DeploymentCreationResult deploymentCreationResult = createDeployment(infrastructureConfig, testPlan,
                infrastructureProvisionResult);

        // Run test scenarios.
        for (TestScenario testScenario : testPlan.getTestScenarios()) {
            try {
                ScenarioExecutor scenarioExecutor = new ScenarioExecutor();
                scenarioExecutor.execute(testScenario, deploymentCreationResult, testPlan);
            } catch (ScenarioExecutorException e) {
                setTestPlanStatus(testPlan, Status.FAIL);
                logger.error(StringUtil.concatStrings("Error occurred while executing the SolutionPattern '",
                        testScenario.getName(), "' , in TestPlan"), e);
            }
        }

        // Test plan completed.
        if (isFailedTestScenariosExist(testPlan)) {
            setTestPlanStatus(testPlan, Status.FAIL);
        } else {
            setTestPlanStatus(testPlan, Status.SUCCESS);
        }
        destroyInfrastructure(infrastructureConfig, testPlan);
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
            return DeployerFactory.getDeployerService(testPlan).deploy(testPlan, infrastructureProvisionResult);
        } catch (TestGridDeployerException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            destroyInfrastructure(infrastructureConfig, testPlan);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Exception occurred while running the deployment for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        } catch (DeployerInitializationException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            destroyInfrastructure(infrastructureConfig, testPlan);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Unable to locate a Deployer Service implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan '"), e);
        } catch (UnsupportedDeployerException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            destroyInfrastructure(infrastructureConfig, testPlan);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error occurred while running deployment for deployment pattern '",
                            testPlan.getDeploymentPattern(), "' in TestPlan"), e);
        }
    }

    /**
     * Sets up infrastructure for the given {@link InfrastructureConfig}.
     *
     * @param infrastructureConfig infrastructure to set up
     * @param testPlan             test plan
     * @throws TestPlanExecutorException thrown when error on setting up infrastructure
     */
    private InfrastructureProvisionResult provisionInfrastructure(InfrastructureConfig infrastructureConfig,
            TestPlan testPlan)
            throws TestPlanExecutorException {
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
            InfrastructureProvisionResult provisionResult = infrastructureProvider
                    .provision(testPlan);

            provisionResult.setName(infrastructureConfig.getProvisioners().get(0).getName());
            provisionResult.setDeploymentScriptsDir(Paths.get(testPlan.getDeploymentRepoDir()).toString());
            return provisionResult;
        } catch (TestGridInfrastructureException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error on infrastructure creation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            setTestPlanStatus(testPlan, Status.FAIL);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        }
    }

    /**
     * Destroys the given {@link InfrastructureConfig}.
     *
     * @param infrastructureConfig infrastructure to delete
     * @param testPlan             test plan
     * @throws TestPlanExecutorException thrown when error on destroying infrastructure
     */
    private void destroyInfrastructure(InfrastructureConfig infrastructureConfig, TestPlan testPlan)
            throws TestPlanExecutorException {
        try {
            //TODO: This condition always evaluates to false as I see. Need to validate.
            if (infrastructureConfig == null || testPlan.getDeploymentConfig() == null) {
                throw new TestPlanExecutorException(StringUtil
                        .concatStrings("Unable to locate infrastructure descriptor for deployment pattern '",
                                testPlan.getDeploymentPattern(), "', in TestPlan"));
            }
            InfrastructureProviderFactory.getInfrastructureProvider(infrastructureConfig)
                    .release(infrastructureConfig, testPlan.getInfraRepoDir());
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
     * @param status   status of the test plan
     * @throws TestPlanExecutorException thrown when error on persisting the test plan
     */
    private void setTestPlanStatus(TestPlan testPlan, Status status) throws TestPlanExecutorException {
        try {
            testPlan.setStatus(status);
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            throw new TestPlanExecutorException("Error occurred while persisting the test plan.");
        }
    }

    /**
     * Returns whether failed test scenarios exists.
     *
     * @param testPlan test plan to check whether failed test scenarios exist
     * @return {@code true} if failed test scenarios exist, {@code false} otherwise
     * @throws TestPlanExecutorException thrown when error on getting status of test scenarios
     */
    private boolean isFailedTestScenariosExist(TestPlan testPlan) throws TestPlanExecutorException {
        try {
            TestScenarioUOW testScenarioUOW = new TestScenarioUOW();
            return testScenarioUOW.isFailedTestScenariosExist(testPlan);
        } catch (TestGridDAOException e) {
            throw new TestPlanExecutorException("Error on retrieving scenario status for the test plan.");
        }
    }
}
