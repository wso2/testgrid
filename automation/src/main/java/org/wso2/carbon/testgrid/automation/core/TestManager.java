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

package org.wso2.carbon.testgrid.automation.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.beans.Test;
import org.wso2.carbon.testgrid.automation.exceptions.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.exceptions.TestManagerException;
import org.wso2.carbon.testgrid.automation.exceptions.TestReaderException;
import org.wso2.carbon.testgrid.automation.file.common.TestGridTestReader;
import org.wso2.carbon.testgrid.common.Deployment;

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
     * @throws TestManagerException When there is an error creating the file structure.
     */
    public void init(String testLocation, Deployment deployment) throws TestManagerException {
        this.deployment = deployment;
        TestGridTestReader testGridTestReader = new TestGridTestReader();

        try {
            this.testLocation = testLocation + File.separator + "Tests";
            this.tests = testGridTestReader.getTests(this.testLocation);

        } catch (TestReaderException e) {
            String msg = "Error while reading tests";
            log.error(msg, e);
            throw new TestManagerException(msg, e);
        }
    }

    /**
     * This method executes the Tests.
     *
     * @throws TestGridExecuteException When there is an error when executing the test
     */
    public void executeTests() throws TestGridExecuteException {

        if (this.tests != null) {
            for (Test test : this.tests) {
                log.info("Executing " + test.getTestName() + " Test");
                test.execute(this.testLocation, this.deployment);
                log.info("---------------------------------------");
            }
        }

    }

}
