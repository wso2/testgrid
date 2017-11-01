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

package org.wso2.carbon.testgrid.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.core.exception.ScenarioExecutorException;
import org.wso2.carbon.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.carbon.testgrid.deployment.DeployerService;
import org.wso2.carbon.testgrid.deployment.TestGridDeployerException;
import org.wso2.carbon.testgrid.infrastructure.InfrastructureProviderService;
import org.wso2.carbon.testgrid.infrastructure.TestGridInfrastructureException;

/**
 * This class is mainly responsible for executing the provided TestPlan.
 */
public class TestPlanExecutor {

    private static final Log log = LogFactory.getLog(TestPlanExecutor.class);

    /**
     * This method executes a given TestPlan.
     *
     * @param  testPlan - An instance of TestPlan in which the tests should be executed.
     * @return Returns the status of the execution (success / fail)
     * @throws TestPlanExecutorException If something goes wrong while executing the TestPlan.
     */
    public TestPlan runTestPlan(TestPlan testPlan) throws TestPlanExecutorException {
        testPlan.setStatus(TestPlan.Status.INFRASTRUCTURE_PREPARATION);
        //Setup the infrastructure
        try {
            new InfrastructureProviderService().createTestEnvironment(testPlan);
        } catch (TestGridInfrastructureException e) {
            throw new TestPlanExecutorException("Exception occurred while running the infrastructure creation for " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'", e);
        }
        if (TestPlan.Status.INFRASTRUCTURE_READY.equals(testPlan.getStatus())) {
            //Trigger the deployment
            testPlan.setStatus(TestPlan.Status.DEPLOYMENT_PREPARATION);
            Deployment deployment = null;
            try {
                deployment = new DeployerService().deploy(testPlan);
            } catch (TestGridDeployerException e) {
                throw new TestPlanExecutorException("Exception occurred while running the deployment " +
                        "for deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" +
                        testPlan.getName() + "'", e);
            }
            if (TestPlan.Status.DEPLOYMENT_READY.equals(testPlan.getStatus())) {
                testPlan.setDeployment(deployment);
                testPlan.setStatus(TestPlan.Status.SCENARIO_EXECUTION);
                for (TestScenario testScenario : testPlan.getTestScenarios()) {
                    try {
                        new ScenarioExecutor().runScenario(testScenario, deployment);
                    } catch (ScenarioExecutorException e) {
                        log.error("Error occurred while executing the SolutionPattern '" +
                                testScenario.getSolutionPattern() + "' , in TestPlan '" +
                                testPlan.getName() + "'", e);
                    }
                }
                testPlan.setStatus(TestPlan.Status.SCENARIO_EXECUTION_COMPLETED);
            } else {
                throw new TestPlanExecutorException("Exception occurred while running the deployment " +
                        "for deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" +
                        testPlan.getName() + "'");
            }
        } else {
            throw new TestPlanExecutorException("Exception occurred while running the infrastructure creation for " +
                    "deployment pattern '" + testPlan.getDeploymentPattern() + "', in TestPlan '" + testPlan.getName()
                    + "'");
        }
        testPlan.setStatus(TestPlan.Status.EXECUTION_COMPLETED);
        return testPlan;
    }

    /**
     * This method aborts a running TestPlan.
     *
     * @param  testPlan - An instance of TestPlan in which the tests should be aborted.
     * @return Returns the status (success / fail)
     * @throws TestPlanExecutorException If something goes wrong while aborting the TestPlan.
     */
    public boolean abortTestPlan(TestPlan testPlan) throws TestPlanExecutorException {
        return false;
    }

    /**
     * This method returns the status of a running TestPlan.
     *
     * @param  testPlan - An instance of TestPlan in which the status should be monitored.
     * @return TestPlan.Status - Returns the status of the TestPlan
     * @throws TestPlanExecutorException If something goes wrong while checking the status of the TestPlan.
     */
    public TestPlan.Status getStatus(TestPlan testPlan) throws TestPlanExecutorException {
        return null;
    }
}
