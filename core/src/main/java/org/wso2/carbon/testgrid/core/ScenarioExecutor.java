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

import org.wso2.carbon.testgrid.automation.TestEngine;
import org.wso2.carbon.testgrid.automation.TestEngineException;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.deployment.DeployerService;
import org.wso2.carbon.testgrid.deployment.TestGridDeployerException;
import org.wso2.carbon.testgrid.infrastructure.InfrastructureProviderService;
import org.wso2.carbon.testgrid.infrastructure.TestGridInfrastructureException;
import org.wso2.carbon.testgrid.reporting.TestReportEngine;
import org.wso2.carbon.testgrid.reporting.TestReportingException;

/**
 * This class is mainly responsible for executing the provided TestScenarios.
 */
public class ScenarioExecutor {

    /**
     * This method executes a given TestScenario.
     *
     * @param  testScenario - An instance of TestScenario in which the tests should be executed.
     * @return Returns the status of the operation
     * @throws ScenarioExecutorException If something goes wrong while executing the TestScenario.
     */
    public boolean runScenario(TestScenario testScenario) throws ScenarioExecutorException {
        try {
            //Setup infrastructure
            Deployment deployment = new InfrastructureProviderService().createTestEnvironment(testScenario);
            //Trigger deployment
            boolean status = new DeployerService().deploy(deployment);
            if (status) {
                //Run Tests
                try {
                    new TestEngine().runScenario(testScenario);
                } catch (TestEngineException e) {
                    e.printStackTrace();
                }
                //Generate Reports
                try {
                    new TestReportEngine().generateReport(testScenario);
                } catch (TestReportingException e) {
                    e.printStackTrace();
                }
            }
        } catch (TestGridInfrastructureException e) {
            e.printStackTrace();
        } catch (TestGridDeployerException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * This method aborts a running TestScenario.
     *
     * @param  testScenario - An instance of TestScenario in which the tests should be aborted.
     * @return Returns the status of the operation
     * @throws ScenarioExecutorException If something goes wrong while aborting the TestScenario.
     */
    public boolean abortScenario(TestScenario testScenario) throws ScenarioExecutorException {
        return false;
    }

    /**
     * This method returns the status of a running TestScenario.
     *
     * @param  testScenario - An instance of TestScenario in which the status should be monitored.
     * @return Returns the status of the TestScenario
     * @throws ScenarioExecutorException If something goes wrong while checking the status of the TestScenario.
     */
    public TestScenario.TestScenarioStatus getStatus(TestScenario testScenario) throws ScenarioExecutorException {
        return null;
    }
}
