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
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
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
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;

import java.nio.file.Paths;

/**
 * This class is responsible for executing the provided TestPlan.
 *
 * @since 1.0.0
 */
public class TestPlanExecutor {

    private static final String DEPLOYMENT_DIR = "DeploymentPatterns";
    private static final Logger logger = LoggerFactory.getLogger(TestPlanExecutor.class);

    /**
     * This method executes a given {@link TestPlan}.
     *
     * @param testPlan an instance of {@link TestPlan} in which the tests should be executed
     * @throws TestPlanExecutorException thrown when error on executing test plan
     */
    public void runTestPlan(TestPlan testPlan, Infrastructure infrastructure) throws TestPlanExecutorException {
        // Deployment preparation.
        testPlan = setupInfrastructure(infrastructure, testPlan);
        testPlan = persistTestPlan(testPlan);

        // Run test plan.
        testPlan.setStatus(Status.RUNNING);
        testPlan = persistTestPlan(testPlan);

        // Run test scenarios.
        for (TestScenario testScenario : testPlan.getTestScenarios()) {
            try {
                ScenarioExecutor scenarioExecutor = new ScenarioExecutor();
                scenarioExecutor.runScenario(testScenario, testPlan.getDeployment(), testPlan);
            } catch (ScenarioExecutorException e) {
                testPlan.setStatus(Status.FAIL);
                testPlan = persistTestPlan(testPlan);
                logger.error(StringUtil.concatStrings("Error occurred while executing the SolutionPattern '",
                        testScenario.getName(), "' , in TestPlan"), e);
            }
        }

        // Test plan completed.
        testPlan.setStatus(Status.SUCCESS);
        testPlan = persistTestPlan(testPlan);

        // Destroy the infrastructure.
        destroyInfrastructure(infrastructure, testPlan);
    }

    /**
     * Creates an instance of {@link Deployment} for the given {@link Infrastructure}.
     *
     * @param infrastructure infrastructure to create the deployment
     * @param testPlan       test plan
     * @return created {@link Deployment}
     * @throws TestPlanExecutorException thrown when error on creating deployment
     */
    private Deployment createDeployment(Infrastructure infrastructure, TestPlan testPlan)
            throws TestPlanExecutorException {
        try {
            Deployment deployment = DeployerFactory.getDeployerService(testPlan).deploy(testPlan.getDeployment());
//            testPlan.setStatus(TestPlan.Status.TESTPLAN_DEPLOYMENT_READY);
            persistTestPlan(testPlan);
            return deployment;
        } catch (TestGridDeployerException e) {
//            testPlan.setStatus(TestPlan.Status.TESTPLAN_DEPLOYMENT_ERROR);
            testPlan = persistTestPlan(testPlan);
            destroyInfrastructure(infrastructure, testPlan);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Exception occurred while running the deployment for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        } catch (DeployerInitializationException e) {
//            testPlan.setStatus(TestPlan.Status.TESTPLAN_DEPLOYMENT_ERROR);
            testPlan = persistTestPlan(testPlan);
            destroyInfrastructure(infrastructure, testPlan);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Unable to locate a Deployer Service implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan '"), e);
        } catch (UnsupportedDeployerException e) {
            testPlan.setStatus(Status.FAIL);
            testPlan = persistTestPlan(testPlan);
            destroyInfrastructure(infrastructure, testPlan);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error occurred while running deployment for deployment pattern '",
                            testPlan.getDeploymentPattern(), "' in TestPlan"), e);
        }
    }

    /**
     * Sets up infrastructure for the given {@link Infrastructure}.
     *
     * @param infrastructure infrastructure to set up
     * @param testPlan       test plan
     * @return test plan with the infra result
     * @throws TestPlanExecutorException thrown when error on setting up infrastructure
     */
    private TestPlan setupInfrastructure(Infrastructure infrastructure, TestPlan testPlan)
            throws TestPlanExecutorException {
        try {
            if (infrastructure == null) {
                testPlan.setStatus(Status.FAIL);
                throw new TestPlanExecutorException(StringUtil
                        .concatStrings("Unable to locate infrastructure descriptor for deployment pattern '",
                                testPlan.getDeploymentPattern(), "', in TestPlan"));
            }
            Deployment deployment = InfrastructureProviderFactory.getInfrastructureProvider(infrastructure)
                    .createInfrastructure(infrastructure, testPlan.getInfraRepoDir());
            deployment.setName(infrastructure.getName());
            deployment.setDeploymentScriptsDir(Paths.get(testPlan.getInfraRepoDir(), DEPLOYMENT_DIR,
                    infrastructure.getName(), infrastructure.getProviderType().name()).toString());
            testPlan.setDeployment(deployment);
//            testPlan.getInfraResult().setStatus(InfraResult.Status.INFRASTRUCTURE_READY);
        } catch (TestGridInfrastructureException e) {
//            testPlan.getInfraResult().setStatus(InfraResult.Status.INFRASTRUCTURE_ERROR);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error on infrastructure creation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            testPlan.setStatus(Status.FAIL);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        } finally {
            persistTestPlan(testPlan);
        }
        return testPlan;
    }

    /**
     * Destroys the given {@link Infrastructure}.
     *
     * @param infrastructure infrastructure to delete
     * @param testPlan       test plan
     * @throws TestPlanExecutorException thrown when error on destroying infrastructure
     */
    private void destroyInfrastructure(Infrastructure infrastructure, TestPlan testPlan) throws
            TestPlanExecutorException {
        try {
            if (infrastructure == null || testPlan.getDeployment() == null) {
                testPlan.setStatus(Status.FAIL);
                persistTestPlan(testPlan);
                throw new TestPlanExecutorException(StringUtil
                        .concatStrings("Unable to locate infrastructure descriptor for deployment pattern '",
                                testPlan.getDeploymentPattern(), "', in TestPlan"));
            }
            InfrastructureProviderFactory.getInfrastructureProvider(infrastructure)
                    .removeInfrastructure(infrastructure, testPlan.getInfraRepoDir());
        } catch (TestGridInfrastructureException e) {
            testPlan.setStatus(Status.FAIL);
            persistTestPlan(testPlan);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error on infrastructure removal for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            testPlan.setStatus(Status.FAIL);
            persistTestPlan(testPlan);
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        }
    }

    /**
     * This method handles persistence outside the try-catch ladder to avoid nested try-catch blocks.
     *
     * @param testPlan TestPlan object to persist.
     * @throws TestPlanExecutorException When there is an error persisting the object.
     */
    private TestPlan persistTestPlan(TestPlan testPlan) throws TestPlanExecutorException {
        try {
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            return testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            throw new TestPlanExecutorException("Error occurred while persisting the test plan.");
        }
    }
}
