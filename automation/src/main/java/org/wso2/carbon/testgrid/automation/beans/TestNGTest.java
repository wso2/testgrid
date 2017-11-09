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

import org.wso2.carbon.testgrid.automation.exceptions.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.executers.common.TestExecuter;
import org.wso2.carbon.testgrid.automation.executers.common.TestExecuterFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;

import java.util.List;

/**
 * This class is responsible for Executing the TestNG tests.
 */
public class TestNGTest extends Test {

    private List<String> testNGJars;
    private TestExecuter testExecuter = TestExecuterFactory.getTestExecutor(TestGridConstants.TEST_TYPE_TESTNG);

    private List<String> getTestNGJars() {
        return testNGJars;
    }

    /**
     * Sets the test jars for test execution
     *
     * @param testNGJars .jar files containing tests to set
     */
    public void setTestNGJars(List<String> testNGJars) {
        this.testNGJars = testNGJars;
    }

    /**
     * @param testLocation the testNG tests location as a String.
     * @param deployment   Deployment mapping information.
     * @throws TestGridExecuteException When there is an execution error.
     */
    @Override
    public void execute(String testLocation, Deployment deployment) throws TestGridExecuteException {
        testExecuter.init(testLocation, getTestName());
        for (String script : this.getTestNGJars()) {
            testExecuter.execute(script, deployment);
        }
    }
}
