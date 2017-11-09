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
package org.wso2.carbon.testgrid.reporting.model;

import org.wso2.carbon.testgrid.reporting.ReportingException;
import org.wso2.carbon.testgrid.reporting.result.TestResultable;

/**
 * Bean class to capture the information required in test results to generate the report for the test plan.
 *
 * @since 1.0.0
 */
public class TestResultReport<T extends TestResultable> {

    private static final String NO_FAILURE = "-";

    private final boolean isTestSuccess;
    private final String isTestSuccessString;
    private final String timestamp;
    private final String testCaseName;
    private final String failureMessage;

    /**
     * Constructs an instance of a test result report.
     *
     * @param testResult test result of the test scenario
     * @throws ReportingException thrown when error occurs in getting the formatted time-stamp
     */
    public TestResultReport(T testResult) throws ReportingException {
        this.isTestSuccess = testResult.isTestSuccess();
        this.isTestSuccessString = testResult.isTestSuccess()
                                   ? TestResultMessages.SUCCESS.toString()
                                   : TestResultMessages.FAILURE.toString();
        this.timestamp = testResult.getFormattedTimestamp();
        this.testCaseName = testResult.getTestCase();
        this.failureMessage = testResult.getFailureMessage() != null
                              ? testResult.getFailureMessage()
                              : NO_FAILURE;
    }

    /**
     * Returns whether the test is a success or a failure.
     *
     * @return {@code true} if the test execution was a success, {@code false} otherwise
     */
    public boolean isTestSuccess() {
        return isTestSuccess;
    }

    /**
     * Returns the test success or the test failure message.
     *
     * @return test success or the test failure message
     */
    public String getIsTestSuccessString() {
        return isTestSuccessString;
    }

    /**
     * Returns the time stamp of the test case.
     *
     * @return time stamp of the test case
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the test case name.
     *
     * @return test case name
     */
    public String getTestCaseName() {
        return testCaseName;
    }

    /**
     * Returns the failure message of the test case.
     *
     * @return failure message of the test case
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Enum to represent test success or test failure message.
     *
     * @since 1.0.0
     */
    private enum TestResultMessages {

        SUCCESS("SUCCESS"),
        FAILURE("FAILURE");

        private final String message;

        /**
         * Sets the test success or test failure message.
         *
         * @param message test result message
         */
        TestResultMessages(final String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
