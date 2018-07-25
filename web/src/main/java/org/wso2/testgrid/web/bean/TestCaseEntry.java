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
package org.wso2.testgrid.web.bean;

/**
 * Bean class for managing information related to test case.
 *
 * @since 1.0.0
 */
public class TestCaseEntry {

    private final String testCase;
    private final String failureMessage;
    private final boolean isTestSuccess;

    /**
     * Constructs an instance of {@link TestCaseEntry}
     *
     * @param testCase       test case name
     * @param failureMessage test case failure message
     * @param isTestSuccess  whether the test case is successful or not
     */
    public TestCaseEntry(String testCase, String failureMessage, boolean isTestSuccess) {
        this.testCase = testCase;
        this.failureMessage = failureMessage;
        this.isTestSuccess = isTestSuccess;
    }

    /**
     * Returns the test case name.
     *
     * @return test case name
     */
    public String getTestCase() {
        return testCase;
    }

    /**
     * Returns the test failure message.
     *
     * @return test failure message
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Returns whether the test case is successful or not.
     *
     * @return {@code true} if the test case is success, {@code false} otherwise
     */
    public boolean isTestSuccess() {
        return isTestSuccess;
    }

    @Override
    public String toString() {
        return "TestCaseEntry{" +
               "testCase='" + testCase + '\'' +
               ", failureMessage='" + failureMessage + '\'' +
               ", isTestSuccess='" + isTestSuccess + '\'' +
               '}';
    }
}
