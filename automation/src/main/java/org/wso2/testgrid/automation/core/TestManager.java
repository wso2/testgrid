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

package org.wso2.testgrid.automation.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.beans.Test;
import org.wso2.testgrid.automation.file.common.TestGridTestReader;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.TestScenario;

import java.io.File;
import java.util.List;

/**
 * TestManager class manages test executions of all the test types in the test grid folder.
 */
public class TestManager {

    private static final Log log = LogFactory.getLog(TestManager.class);
    private List<Test> tests;
    private Deployment deployment;
    private String testLocation;

    /**
     * @param testLocation The location of tests in the file system as a String.
     * @param deployment   The deployment details of current pattern.
     * @throws TestAutomationException When there is an error creating the file structure.
     */
    public void init(String testLocation, Deployment deployment, TestScenario scenario) throws TestAutomationException {
        this.deployment = deployment;
        TestGridTestReader testGridTestReader = new TestGridTestReader();

        try {
            this.testLocation = testLocation;
            this.tests = testGridTestReader.getTests(this.testLocation,scenario);
        } catch (TestAutomationException e) {
            throw new TestAutomationException("Error while reading tests", e);
        }
    }

    /**
     * This method executes the Tests.
     *
     * @throws TestAutomationException When there is an error when executing the test
     */
    public void executeTests() throws TestAutomationException {

        if (this.tests != null) {
            for (Test test : this.tests) {
                log.info("Executing " + test.getTestName() + " Test");
                test.execute(this.testLocation, this.deployment);
                log.info("---------------------------------------");
            }
        }

    }

}
