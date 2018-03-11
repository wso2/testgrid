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
import org.wso2.testgrid.automation.Test;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.reader.TestReader;
import org.wso2.testgrid.automation.reader.TestReaderFactory;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.core.exception.ScenarioExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestCaseUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is mainly responsible for executing a TestScenario. This will invoke the TestAutomationEngine for
 * executing the tests available for a particular solution pattern.
 *
 * @since 1.0.0
 */
public class ScenarioExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioExecutor.class);
    private final TestScenarioUOW testScenarioUOW;
    private final TestCaseUOW testCaseUOW;

    public ScenarioExecutor() {
        testScenarioUOW = new TestScenarioUOW();
        testCaseUOW = new TestCaseUOW();
    }

    public ScenarioExecutor(TestScenarioUOW testScenarioUOW, TestCaseUOW testCaseUOW) {
        this.testScenarioUOW = testScenarioUOW;
        this.testCaseUOW = testCaseUOW;
    }

    /**
     * This method executes a given TestScenario.
     *
     * @param testScenario             an instance of TestScenario in which the tests should be executed.
     * @param deploymentCreationResult the deployment creation output.
     * @param testPlan                 test plan associated with the test scenario.
     * @throws ScenarioExecutorException If something goes wrong while executing the TestScenario.
     */
    public void execute(TestScenario testScenario, DeploymentCreationResult deploymentCreationResult, TestPlan testPlan)
            throws ScenarioExecutorException {
        try {
            // Run test scenario.
            String homeDir = testPlan.getScenarioTestsRepository();
            testScenario.setTestPlan(testPlan);
            testScenario.setStatus(Status.RUNNING);
            testScenario = persistTestScenario(testScenario);

            logger.info("Executing Tests for Solution Pattern : " + testScenario.getName());
            String testLocation = Paths.get(homeDir, testScenario.getName()).toAbsolutePath().toString();
            List<Test> tests = getTests(testScenario, testLocation);

            if (tests.isEmpty()) {
                logger.warn("Couldn't find any tests for the scenario " + testScenario + " At location "
                        + testLocation);
            }

            for (Test test : tests) {
                logger.info(StringUtil.concatStrings("Executing ", test.getTestName(), " Test"));
                test.execute(testLocation, deploymentCreationResult);
                logger.info("---------------------------------------");
            }

            // Test scenario completed.
            setScenarioStatus(testScenario);
            persistTestScenario(testScenario);
        } catch (TestAutomationException e) {
            testScenario.setStatus(Status.FAIL);
            persistTestScenario(testScenario);
            throw new ScenarioExecutorException(StringUtil
                    .concatStrings("Exception occurred while running the Tests for Solution Pattern '",
                            testScenario.getName(), "'"), e);
        } catch (TestGridDAOException e) {
            throw new ScenarioExecutorException(StringUtil
                    .concatStrings("Exception occurred while checking for failed test cases for scenario'",
                            testScenario.getName(), "'", e));
        }
    }

    /**
     * Persists the {@link TestScenario} instance.
     *
     * @param testScenario test scenario to be persisted
     * @return persisted {@link TestScenario} instance
     * @throws ScenarioExecutorException thrown when error on persisting the {@link TestScenario} instance
     */
    private TestScenario persistTestScenario(TestScenario testScenario) throws ScenarioExecutorException {
        try {
            return testScenarioUOW.persistTestScenario(testScenario);
        } catch (TestGridDAOException e) {
            throw new ScenarioExecutorException(StringUtil
                    .concatStrings("Error occurred when persisting test scenario - ", testScenario), e);
        }
    }

    /**
     * This method goes through every type of test in the folder structure and returns a list of tests
     * with common test interface.
     *
     * @param testScenario test scenario to obtain tests from
     * @param testLocation location on which test files are
     * @return a list of {@link Test} instances
     * @throws ScenarioExecutorException thrown when an error on reading tests
     */
    private List<Test> getTests(TestScenario testScenario, String testLocation) throws ScenarioExecutorException {
        try {
            Path testLocationPath = Paths.get(testLocation);
            List<Test> testList = new ArrayList<>();

            if (Files.exists(testLocationPath)) {
                File file = new File(Paths.get(testLocationPath.toString()).toString());
                String[] testDirectories = file.list((current, name) -> new File(current, name).isDirectory());
                testDirectories = testDirectories == null ? new String[0] : testDirectories;

                for (String testDirectory : testDirectories) {
                    Optional<TestReader> testReader = TestReaderFactory.getTestReader(testDirectory);
                    if (testReader.isPresent()) {
                        List<Test> tests = testReader.get().readTests(testLocation, testScenario);
                        testList.addAll(tests);
                    }
                }
            } else {
                throw new ScenarioExecutorException(
                        "Scenario directory doesn't exist for the path : " + testLocationPath + " Scenario name : "
                                + testScenario.getName());
            }
            return testList;
        } catch (TestAutomationException e) {
            throw new ScenarioExecutorException("Error while reading tests for test scenario.", e);
        }
    }

    /**
     * Checks for any failed test cases and sets final status of the scenario accordingly.
     *
     * @param testScenario test scenario
     * @throws TestGridDAOException thrown when error fetching test cases
     */
    private void setScenarioStatus(TestScenario testScenario) throws TestGridDAOException {
        if (!testCaseUOW.isExistsFailedTests(testScenario)) {
            testScenario.setStatus(Status.FAIL);
        } else {
            testScenario.setStatus(Status.SUCCESS);
        }
    }
}
