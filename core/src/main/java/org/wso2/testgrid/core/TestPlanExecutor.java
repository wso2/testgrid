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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.InfraResult;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.constants.TestGridConstants;
import org.wso2.testgrid.common.exception.DeployerInitializationException;
import org.wso2.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.UnsupportedDeployerException;
import org.wso2.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.testgrid.core.exception.ScenarioExecutorException;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;

import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;

/**
 * This class is mainly responsible for executing the provided TestPlan.
 */
public class TestPlanExecutor {

    private static final Log log = LogFactory.getLog(TestPlanExecutor.class);

    private TestPlan setupInfrastructure(Infrastructure infrastructure, TestPlan testPlan) throws
            TestPlanExecutorException {
        //Setup the infrastructure
        try {

            if (infrastructure != null) {
                Deployment deployment = InfrastructureProviderFactory.getInfrastructureProvider(infrastructure)
                        .createInfrastructure(infrastructure, testPlan.getInfraRepoDir());
                deployment.setName(infrastructure.getName());
                deployment.setDeploymentScriptsDir(Paths.get(testPlan.getInfraRepoDir(),
                        TestGridConstants.DEPLOYMENT_DIR, infrastructure.getName(),
                        infrastructure.getProviderType().name()).toString());
                testPlan.setDeployment(deployment);
                InfraResult infraResult = testPlan.getInfraResult();
                infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_READY);
                testPlan.setInfraResult(infraResult);
            } else {
                InfraResult infraResult = testPlan.getInfraResult();
                infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_ERROR);
                testPlan.setInfraResult(infraResult);
                throw new TestPlanExecutorException("Unable to locate infrastructure descriptor for " +
                        "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" +
                        testPlan.getName() + "'");
            }
        } catch (TestGridInfrastructureException e) {
            InfraResult infraResult = testPlan.getInfraResult();
            infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_ERROR);
            testPlan.setInfraResult(infraResult);
            throw new TestPlanExecutorException("Exception occurred while running the infrastructure creation for " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'", e);
        } catch (InfrastructureProviderInitializationException e) {
            InfraResult infraResult = testPlan.getInfraResult();
            infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_ERROR);
            testPlan.setInfraResult(infraResult);
            throw new TestPlanExecutorException("Unable to locate an Infrastructure Provider implementation for  " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'", e);
        } catch (UnsupportedProviderException e) {
            InfraResult infraResult = testPlan.getInfraResult();
            infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_ERROR);
            testPlan.setInfraResult(infraResult);
            throw new TestPlanExecutorException("Exception occurred while running the infrastructure creation for " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'", e);
        } finally {
            handlePersistance(testPlan);
        }

        return testPlan;
    }

    private TestPlan destroyInfrastructure(Infrastructure infrastructure, TestPlan testPlan) throws
            TestPlanExecutorException {
        //Remove the infrastructure
        try {

            if (infrastructure != null && testPlan.getDeployment() != null) {
                InfrastructureProviderFactory.getInfrastructureProvider(infrastructure)
                        .removeInfrastructure(infrastructure, testPlan.getInfraRepoDir());
            } else {
                InfraResult infraResult = testPlan.getInfraResult();
                infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_DESTROY_ERROR);
                testPlan.setInfraResult(infraResult);
                throw new TestPlanExecutorException("Unable to locate infrastructure descriptor for " +
                        "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" +
                        testPlan.getName() + "'");
            }
        } catch (TestGridInfrastructureException e) {
            InfraResult infraResult = testPlan.getInfraResult();
            infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_DESTROY_ERROR);
            testPlan.setInfraResult(infraResult);
            throw new TestPlanExecutorException("Exception occurred while running the infrastructure creation for " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'", e);
        } catch (InfrastructureProviderInitializationException e) {
            InfraResult infraResult = testPlan.getInfraResult();
            infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_DESTROY_ERROR);
            testPlan.setInfraResult(infraResult);
            throw new TestPlanExecutorException("Unable to locate an Infrastructure Provider implementation for  " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'", e);
        } catch (UnsupportedProviderException e) {
            InfraResult infraResult = testPlan.getInfraResult();
            infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_DESTROY_ERROR);
            testPlan.setInfraResult(infraResult);
            throw new TestPlanExecutorException("Exception occurred while running the infrastructure creation for " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'", e);
        } finally {
            handlePersistance(testPlan);
        }
        return testPlan;
    }

    /**
     * This method executes a given TestPlan.
     *
     * @param testPlan an instance of TestPlan in which the tests should be executed.
     * @return the status of the execution (success / fail)
     * @throws TestPlanExecutorException If something goes wrong while executing the TestPlan.
     */
    public TestPlan runTestPlan(TestPlan testPlan, Infrastructure infrastructure) throws TestPlanExecutorException {
        TestPlanUOW testPlanUOW = new TestPlanUOW();
        testPlan = setupInfrastructure(infrastructure, testPlan);
        testPlan.setStatus(TestPlan.Status.TESTPLAN_DEPLOYMENT_PREPARATION);
        try {
            testPlan = testPlanUOW.persistSingleTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            destroyInfrastructure(infrastructure, testPlan);
            throw new TestPlanExecutorException("Error occured while Persisting TestPlan ", e);
        }
        if (InfraResult.Status.INFRASTRUCTURE_READY.equals(testPlan.getInfraResult().getStatus())) {
            Deployment deployment;
            try {
                deployment = DeployerFactory.getDeployerService(testPlan).deploy(testPlan.getDeployment());
                testPlan.setStatus(TestPlan.Status.TESTPLAN_DEPLOYMENT_READY);
                testPlan = handlePersistance(testPlan);

            } catch (TestGridDeployerException e) {
                testPlan.setStatus(TestPlan.Status.TESTPLAN_DEPLOYMENT_ERROR);
                this.destroyInfrastructure(infrastructure, testPlan);
                testPlan = handlePersistance(testPlan);
                throw new TestPlanExecutorException("Exception occurred while running the deployment " +
                        "for deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" +
                        testPlan.getName() + "'", e);
            } catch (DeployerInitializationException e) {
                testPlan.setStatus(TestPlan.Status.TESTPLAN_DEPLOYMENT_ERROR);
                this.destroyInfrastructure(infrastructure, testPlan);
                testPlan = handlePersistance(testPlan);
                throw new TestPlanExecutorException("Unable to locate a Deployer Service implementation for  " +
                        "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" +
                        testPlan.getName() + "'", e);
            } catch (UnsupportedDeployerException e) {
                testPlan.setStatus(TestPlan.Status.TESTPLAN_DEPLOYMENT_ERROR);
                this.destroyInfrastructure(infrastructure, testPlan);
                testPlan = handlePersistance(testPlan);
                throw new TestPlanExecutorException("Error occurred while running deployment for "
                        + "deployment pattern '" + testPlan.getDeploymentPattern() + "' in TestPlan '"
                        + testPlan.getName() + "'", e);
            }

            if (TestPlan.Status.TESTPLAN_DEPLOYMENT_READY.equals(testPlan.getStatus())) {
                testPlan.setDeployment(deployment);
                testPlan = handlePersistance(testPlan);
                for (TestScenario testScenario : testPlan.getTestScenarios()) {
                    try {
                        new ScenarioExecutor().runScenario(testScenario, deployment, testPlan);
                    } catch (ScenarioExecutorException e) {
                        log.error("Error occurred while executing the SolutionPattern '" +
                                testScenario.getName() + "' , in TestPlan '" +
                                testPlan.getName() + "'", e);
                    }
                }
                testPlan.setStatus(TestPlan.Status.TESTPLAN_COMPLETED);
                testPlan = handlePersistance(testPlan);
            } else {
                this.destroyInfrastructure(infrastructure, testPlan);
                throw new TestPlanExecutorException("Exception occurred while running the deployment " +
                        "for deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" +
                        testPlan.getName() + "'");
            }
        } else {
            InfraResult infraResult = testPlan.getInfraResult();
            infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_ERROR);
            testPlan.setInfraResult(infraResult);
            testPlan = handlePersistance(testPlan);
            throw new TestPlanExecutorException("Exception occurred while running the infrastructure creation for " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'");
        }
        //Destroy the infrastructure
        testPlan = destroyInfrastructure(infrastructure, testPlan);
        testPlan.setModifiedTimestamp(new Timestamp(new Date().getTime()));
        testPlan.setStatus(TestPlan.Status.TESTPLAN_COMPLETED);
        return testPlan;
    }

    /**
     * This method handles persistence outside the try-catch ladder to avoid nested try-catch blocks.
     *
     * @param testPlan TestPlan object to persist.
     * @throws TestPlanExecutorException When there is an error persisting the object.
     */
    private TestPlan handlePersistance(TestPlan testPlan) throws TestPlanExecutorException {
        TestPlanUOW testPlanUOW = new TestPlanUOW();
        try {
            return testPlanUOW.persistSingleTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            throw new TestPlanExecutorException("Error occurred while persisting the test plan");
        }
    }

    /**
     * This method aborts a running TestPlan.
     *
     * @param testPlan an instance of TestPlan in which the tests should be aborted.
     * @return the status (success / fail)
     * @throws TestPlanExecutorException If something goes wrong while aborting the TestPlan.
     */
    public boolean abortTestPlan(TestPlan testPlan, Infrastructure infrastructure) throws TestPlanExecutorException {
        this.destroyInfrastructure(infrastructure, testPlan);
        return false;
    }

    /**
     * This method returns the status of a running TestPlan.
     *
     * @param testPlan an instance of TestPlan in which the status should be monitored.
     * @return TestPlan.Status the status of the TestPlan
     * @throws TestPlanExecutorException If something goes wrong while checking the status of the TestPlan.
     */
    public TestPlan.Status getStatus(TestPlan testPlan) throws TestPlanExecutorException {
        return testPlan.getStatus();
    }
}
