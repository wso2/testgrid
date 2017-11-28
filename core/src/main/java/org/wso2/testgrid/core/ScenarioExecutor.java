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

import org.wso2.testgrid.automation.TestEngineImpl;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.TestAutomationEngineException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.core.exception.ScenarioExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;

/**
 * This class is mainly responsible for executing a TestScenario. This will invoke the TestAutomationEngine for
 * executing the tests available for a particular solution pattern.
 *
 * @since 1.0.0
 */
public class ScenarioExecutor {

    /**
     * This method executes a given TestScenario.
     *
     * @param testScenario an instance of TestScenario in which the tests should be executed.
     * @param deployment   an instance of Deployment in which the tests should be executed against.
     * @param testPlan     test plan associated with the test scenario
     * @return the modified TestScenario with status
     * @throws ScenarioExecutorException If something goes wrong while executing the TestScenario.
     */
    public TestScenario runScenario(TestScenario testScenario, Deployment deployment, TestPlan testPlan)
            throws ScenarioExecutorException {
        try {
            String homeDir = testPlan.getTestRepoDir();
            testScenario = setStatusAndPersistTestScenario(testScenario, TestScenario.Status.TEST_SCENARIO_RUNNING);
            new TestEngineImpl().runScenario(testScenario, homeDir, deployment);
            testScenario =
                    setStatusAndPersistTestScenario(testScenario, TestScenario.Status.TEST_SCENARIO_COMPLETED);
        } catch (TestAutomationEngineException e) {
            testScenario = setStatusAndPersistTestScenario(testScenario, TestScenario.Status.TEST_SCENARIO_ERROR);
            throw new ScenarioExecutorException(StringUtil
                    .concatStrings("Exception occurred while running the Tests for Solution Pattern '",
                            testScenario.getName(), "'"));
        }
        return testScenario;
    }

    /**
     * This method aborts a running TestScenario.
     *
     * @param testScenario an instance of TestScenario in which the tests should be aborted.
     * @return the status of the operation
     * @throws ScenarioExecutorException If something goes wrong while aborting the TestScenario.
     */
    public boolean abortScenario(TestScenario testScenario) throws ScenarioExecutorException {
        return false;
    }

    /**
     * Sets the given status and persists the {@link TestScenario} instance.
     *
     * @param testScenario test scenario to be persisted
     * @param status       status of the test scenario
     * @return persisted {@link TestScenario} instance
     * @throws ScenarioExecutorException thrown when error on persisting the {@link TestScenario} instance
     */
    private TestScenario setStatusAndPersistTestScenario(TestScenario testScenario, TestScenario.Status status)
            throws ScenarioExecutorException {
        try {
            TestScenarioUOW testScenarioUOW = new TestScenarioUOW();
            TestScenario persisted = testScenarioUOW.persistTestScenario(testScenario, status);
            if (persisted != null) {
                persisted.setTestEngine(testScenario.getTestEngine());
                persisted.setEnabled(testScenario.isEnabled());

            }
            return persisted;
        } catch (TestGridDAOException e) {
            throw new ScenarioExecutorException(StringUtil
                    .concatStrings("Error occurred when persisting test scenario - ", testScenario.toString()), e);
        }
    }
}
