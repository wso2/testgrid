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
package org.wso2.testgrid.reporting.model;

import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.reporting.ReportingException;

import java.sql.Timestamp;

/**
 * Class to capture the information required to generate the test case view.
 *
 * @since 1.0.0
 */
public class TestCaseView {

    private final String testName;
    private final Timestamp startTimestamp;
    private final Timestamp modifiedTimestamp;
    private final TestCase.Status status;
    private final String logLocation;
    private final String failureMessage;
    private final boolean isTestSuccess;
    private final boolean isTestCaseFailed;
    private final boolean isTestPending;
    private final boolean isTestExecuting;

    /**
     * Constructs an instance of a test result view.
     *
     * @param testCase test case of the test scenario
     * @throws ReportingException thrown when error occurs in getting the formatted time-stamp
     */
    public TestCaseView(TestCase testCase) throws ReportingException {
        // Test case statuses
        this.isTestSuccess = testCase.getStatus().equals(TestCase.Status.TESTCASE_COMPLETED);
        this.isTestCaseFailed = testCase.getStatus().equals(TestCase.Status.TESTCASE_ERROR);
        this.isTestPending = testCase.getStatus().equals(TestCase.Status.TESTCASE_PENDING);
        this.isTestExecuting = testCase.getStatus().equals(TestCase.Status.TESTCASE_EXECUTION);

        this.testName = testCase.getName();
        this.startTimestamp = new Timestamp(testCase.getStartTimestamp().getTime());
        this.modifiedTimestamp = new Timestamp(testCase.getModifiedTimestamp().getTime());
        this.status = testCase.getStatus();
        this.logLocation = testCase.getLogLocation();
        this.failureMessage = testCase.getFailureMessage();
    }

    /**
     * Returns the test name.
     *
     * @return test name
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Returns the start time of the test.
     *
     * @return start time of the test
     */
    public Timestamp getStartTimestamp() {
        return new Timestamp(startTimestamp.getTime());
    }

    /**
     * Returns the modified time of the test.
     *
     * @return modified time of the test
     */
    public Timestamp getModifiedTimestamp() {
        return new Timestamp(modifiedTimestamp.getTime());
    }

    /**
     * Returns the test case status.
     *
     * @return test case status
     */
    public TestCase.Status getStatus() {
        return status;
    }

    /**
     * Returns the log location of the test.
     *
     * @return log location of the test
     */
    public String getLogLocation() {
        return logLocation;
    }

    /**
     * Returns the failure message of the test.
     *
     * @return failure message of the test
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Returns whether the test case is successful.
     *
     * @return returns {@code true} if test case is successful, {@code false} otherwise
     */
    public boolean isTestSuccess() {
        return isTestSuccess;
    }

    /**
     * Returns whether the test case is failed.
     *
     * @return returns {@code true} if test case is failed, {@code false} otherwise
     */
    public boolean isTestCaseFailed() {
        return isTestCaseFailed;
    }

    /**
     * Returns whether the test case is pending.
     *
     * @return returns {@code true} if test case is pending, {@code false} otherwise
     */
    public boolean isTestPending() {
        return isTestPending;
    }

    /**
     * Returns whether the test case is executing.
     *
     * @return returns {@code true} if test case is executing, {@code false} otherwise
     */
    public boolean isTestExecuting() {
        return isTestExecuting;
    }
}
