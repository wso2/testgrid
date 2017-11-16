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

package org.wso2.carbon.testgrid.automation.executor;

import org.wso2.carbon.testgrid.automation.TestAutomationException;
import org.wso2.carbon.testgrid.common.Deployment;

/**
 * Interface for Test executors.
 *
 * @since 1.0.0
 */
public interface TestExecutor {

    /**
     * Executes a test based on the given script and the deployment.
     *
     * @param script     test script
     * @param deployment deployment to run the test script on
     * @throws TestAutomationException thrown when error on executing the given test script
     */
    void execute(String script, Deployment deployment) throws TestAutomationException;

    /**
     * Initialises the test executor.
     * <p>
     * Performs pre-operations required for test execution
     *
     * @param testsLocation location of the test scripts
     * @param testName      test name
     * @throws TestAutomationException thrown when error on initialising the test executor
     */
    void init(String testsLocation, String testName) throws TestAutomationException;
}
