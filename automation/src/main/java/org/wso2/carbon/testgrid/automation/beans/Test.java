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

package org.wso2.carbon.testgrid.automation.beans;

import org.wso2.carbon.testgrid.automation.TestAutomationException;
import org.wso2.carbon.testgrid.common.Deployment;

/**
 * This is the abstraction of Test class.
 *
 * @since 1.0.0
 */
public abstract class Test {

    private String testName;

    /**
     * Returns the test name.
     *
     * @return test name
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Sets the test name.
     *
     * @param testName test name
     */
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /**
     * Executes the test for the given test location and deployment.
     *
     * @param testLocation location of the tests
     * @param deployment   deployment to execute the tests on
     * @throws TestAutomationException thrown when error on executing tests
     */
    public abstract void execute(String testLocation, Deployment deployment) throws TestAutomationException;

}
