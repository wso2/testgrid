/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.core.phase.DeployPhase;
import org.wso2.testgrid.core.phase.InfraPhase;
import org.wso2.testgrid.core.phase.TestPhase;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is responsible for executing the provided TestPlan.
 *
 * @since 1.0.0
 */
public class TestPlanExecutor {

    private static final int LINE_LENGTH = 72;
    private static final Logger logger = LoggerFactory.getLogger(TestPlanExecutor.class);
    private static final int MAX_NAME_LENGTH = 52;

    private  InfraPhase infraPhase;
    private DeployPhase deployPhase;
    private TestPhase testPhase;

    public TestPlanExecutor() {
        infraPhase = new InfraPhase();
        deployPhase = new DeployPhase();
        testPhase = new TestPhase();
    }
    public TestPlanExecutor(ScenarioExecutor scenarioExecutor, TestPlanUOW testPlanUOW,
                            TestScenarioUOW testScenarioUOW) {
        infraPhase = new InfraPhase();
        deployPhase = new DeployPhase();
        testPhase = new TestPhase();
        infraPhase.setTestPlanUOW(testPlanUOW);
        infraPhase.setTestScenarioUOW(testScenarioUOW);
        deployPhase.setTestPlanUOW(testPlanUOW);
        deployPhase.setTestScenarioUOW(testScenarioUOW);
        testPhase.setTestPlanUOW(testPlanUOW);
        testPhase.setTestScenarioUOW(testScenarioUOW);
    }

    /**
     * This method executes a given {@link TestPlan}.
     *
     * @param testPlan an instance of {@link TestPlan} in which the tests should be executed
     * @throws TestPlanExecutorException thrown when error on executing test plan
     */
    public boolean execute(TestPlan testPlan) throws TestPlanExecutorException, TestGridDAOException {
        long startTime = System.currentTimeMillis();

        testPhase.execute(deployPhase.execute(infraPhase.execute(testPlan)));

        // Print summary
        printSummary(testPlan, System.currentTimeMillis() - startTime);
        return testPlan.getStatus() == TestPlanStatus.SUCCESS;
    }

    /**
     * Prints a summary of the executed test plan.
     * Summary includes the list of scenarios that has been run, and their pass/fail status.
     *
     * @param testPlan  the test plan
     * @param totalTime time taken to run the test plan
     */
    void printSummary(TestPlan testPlan, long totalTime) {
        switch (testPlan.getStatus()) {
            case SUCCESS:
                logger.info("All tests passed...");
                break;
            case ERROR:
                switch(testPlan.getPhase()) {
                    case INFRA_PHASE_ERROR:
                        logger.info("Error occurred while provisioning infrastructure...");
                        break;
                    case DEPLOY_PHASE_ERROR:
                        logger.info("Error occurred while creating deployment...");
                        break;
                    case TEST_PHASE_INCOMPLETE:
                        logger.info("Skipped several tests/scenarios due to errors...");
                        break;
                    case TEST_PHASE_ERROR:
                        logger.info("Error occurred while executing tests...");
                        break;
                    default:
                            logger.info("Undefined error occurred: (TestPlanPhase: " + testPlan.getPhase() + ").");
                }
                break;
            case FAIL:
                printFailState(testPlan);
                break;
            case RUNNING:
            default:
                logger.error(StringUtil.concatStrings(
                        "Inconsistent state detected (", testPlan.getStatus(), "). Please report this to testgrid team "
                                + "at github.com/wso2/testgrid."));
        }

        printSeparator(LINE_LENGTH);
        logger.info(StringUtil.concatStrings("Test Plan Summary for ", testPlan.getInfraParameters()), ":");
        for (TestScenario testScenario : testPlan.getTestScenarios()) {
            StringBuilder buffer = new StringBuilder(128);

            buffer.append(testScenario.getName());
            buffer.append(' ');

            String padding = StringUtils.repeat(".", MAX_NAME_LENGTH - buffer.length());
            buffer.append(padding);
            buffer.append(' ');

            buffer.append(testScenario.getStatus());
            logger.info(buffer.toString());
        }

        printSeparator(LINE_LENGTH);
        logger.info("TEST RUN " + testPlan.getStatus());
        printSeparator(LINE_LENGTH);

        logger.info("Total Time: " + StringUtil.getHumanReadableTimeDiff(totalTime));
        logger.info("Finished at: " + new Date());
        printSeparator(LINE_LENGTH);
    }

    /**
     * Prints the logs for failure scenario.
     *
     * @param testPlan the test plan that has failures.
     */
    private static void printFailState(TestPlan testPlan) {
        logger.warn("There are test failures...");
        logger.info("Failed tests:");
        AtomicInteger testCaseCount = new AtomicInteger(0);
        AtomicInteger failedTestCaseCount = new AtomicInteger(0);
        testPlan.getTestScenarios().stream()
                .peek(ts -> {
                    testCaseCount.addAndGet(ts.getTestCases().size());
                    if (ts.getTestCases().size() == 0) {
                        testCaseCount.incrementAndGet();
                        failedTestCaseCount.incrementAndGet();
                    }
                })
                .filter(ts -> ts.getStatus() != Status.SUCCESS)
                .map(TestScenario::getTestCases)
                .flatMap(Collection::stream)
                .filter(tc -> Status.FAIL.equals(tc.getStatus()))
                .forEachOrdered(
                        tc -> {
                            failedTestCaseCount.incrementAndGet();
                            logger.info("  " + tc.getTestScenario().getName() + "::" + tc.getName() + ": " + tc
                                    .getFailureMessage());
                        });

        logger.info("");
        logger.info(
                StringUtil.concatStrings("Tests run: ", testCaseCount, ", Failures/Errors: ", failedTestCaseCount));
        logger.info("");
    }

    /**
     * Prints a series of dashes ('-') into the log.
     *
     * @param length no of characters to print
     */
    private static void printSeparator(int length) {
        logger.info(StringUtils.repeat("-", length));
    }

    /**
     * This enum defines the Operating system categories.
     *
     * @since 1.0.0
     */
    public enum OSCategory {

        UNIX("UNIX", ""),
        WINDOWS("WINDOWS", "");

        private final String osCategory;
        OSCategory(String osCategory, String logPath) {
            this.osCategory = osCategory;
        }

        @Override
        public String toString() {
            return this.osCategory;
        }
    }
}
