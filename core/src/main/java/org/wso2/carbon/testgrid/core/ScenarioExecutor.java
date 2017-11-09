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
import org.wso2.carbon.testgrid.automation.TestEngineImpl;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.common.exception.TestAutomationEngineException;
import org.wso2.carbon.testgrid.core.exception.ScenarioExecutorException;

/**
 * This class is mainly responsible for executing a TestScenario. This will invoke the TestAutomationEngine for executing the
 * Tests available for a particular solution pattern.
 */
public class ScenarioExecutor {

    private static final Log log = LogFactory.getLog(ScenarioExecutor.class);

    /**
     * This method executes a given TestScenario.
     *
     * @param  testScenario - An instance of TestScenario in which the tests should be executed.
     * @param  deployment - An instance of Deployment in which the tests should be executed against.
     * @param  homeDir - The location of cloned TestPlan.
     * @return Returns the modified TestScenario with status
     * @throws ScenarioExecutorException If something goes wrong while executing the TestScenario.
     */
    public TestScenario runScenario(TestScenario testScenario, Deployment deployment, String homeDir)
            throws ScenarioExecutorException {
        try {
            testScenario.setStatus(TestScenario.Status.RUNNING);
            new TestEngineImpl().runScenario(testScenario, homeDir, deployment);
            testScenario.setStatus(TestScenario.Status.COMPLETED);
        } catch (TestAutomationEngineException e) {
            testScenario.setStatus(TestScenario.Status.ERROR);
            throw new ScenarioExecutorException("Exception occurred while running the Tests for Solution Pattern '" +
                    testScenario.getSolutionPattern() + "'");
        }
        return testScenario;
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
    public TestScenario.Status getStatus(TestScenario testScenario) throws ScenarioExecutorException {
        return null;
    }
}
