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
package org.wso2.carbon.testgrid.reporting.result;

import org.wso2.carbon.testgrid.reporting.ReportingException;

/**
 * Interface to capture the result of a test scenario.
 *
 * @since 1.0.0
 */
public interface TestResultable {

    /**
     * Returns whether the test is a success or a failure.
     *
     * @return whether the test is a success or a failure
     */
    boolean isTestSuccess();

    /**
     * Returns the time stamp of the test case.
     *
     * @return time stamp of the test case
     */
    String getTimestamp();

    /**
     * Returns the formatted time stamp of the actual time stamp.
     *
     * @return formatted time stamp
     * @throws ReportingException thrown when error on formatting date
     */
    String getFormattedTimestamp() throws ReportingException;

    /**
     * Returns the test case name.
     *
     * @return test case name
     */
    String getTestCase();

    /**
     * Returns the failure message of the test case.
     *
     * @return failure message of the test case
     */
    String getFailureMessage();
}
