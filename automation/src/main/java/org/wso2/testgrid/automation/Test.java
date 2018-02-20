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

package org.wso2.testgrid.automation;

import org.wso2.testgrid.automation.executor.TestExecutor;
import org.wso2.testgrid.automation.executor.TestExecutorFactory;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;

import java.nio.file.Paths;
import java.util.List;

/**
 * This class is responsible for activities related to a test unit.
 *
 * @since 1.0.0
 */
public class Test {

    private final String testName;
    private final TestExecutor testExecutor;
    private final List<String> scripts;
    private TestScenario testScenario;
    private String preScenarioScript;
    private String postScenarioScript;

    /**
     * Constructs an instance of {@link Test} for the given params.
     *
     * @param testName     test name
     * @param testType     type of test (ex: JMeter, TestNG, Selenium etc.)
     * @param scripts      test scripts associated with the test
     * @param testScenario test scenario associated with the test
     * @throws TestAutomationException thrown when the the test executor is not found for the given test type
     */
    public Test(String testName, TestEngine testType, List<String> scripts, TestScenario testScenario)
            throws TestAutomationException {
        this.testName = testName;
        this.testExecutor = TestExecutorFactory.getTestExecutor(testType);
        this.scripts = scripts;
        this.testScenario = testScenario;
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
     * Returns the test executor for this test.
     *
     * @return test executor for this test
     */
    public TestExecutor getTestExecutor() {
        return testExecutor;
    }

    /**
     * Returns the list of scripts associated with this test.
     *
     * @return list of scripts associated with this test
     */
    public List<String> getScripts() {
        return scripts;
    }

    /**
     * Returns the test script associated with this test.
     *
     * @return test script associated with this test.
     */
    public TestScenario getTestScenario() {
        return testScenario;
    }

    /**
     * Returns the Pre scenario script.
     *
     * @return location of the script.
     */
    public String getPreScenarioScript() {
        return preScenarioScript;
    }

    /**
     * Sets the Pre scenario script.
     *
     * @param preScenarioScript location of the script.
     */
    public void setPreScenarioScript(String preScenarioScript) {
        this.preScenarioScript = preScenarioScript;
    }

    /**
     * Returns the Post scenario script.
     *
     * @return location of the script.
     */
    public String getPostScenarioScript() {
        return postScenarioScript;
    }

    /**
     * Sets the Post scenario script.
     *
     * @param postScenarioScript location of the script.
     */
    public void setPostScenarioScript(String postScenarioScript) {
        this.postScenarioScript = postScenarioScript;
    }

    /**
     * Executes the test for the given test location and deployment.
     *
     * @param testLocation location of the tests
     * @param input        Output of the previous (ie. deployment) build step.
     * @throws TestAutomationException thrown when error on executing tests
     */
    public TestScenario execute(String testLocation, DeploymentCreationResult input)
            throws TestAutomationException {
        if (!StringUtil.isStringNullOrEmpty(preScenarioScript)) {
            String result = testExecutor.executeEnvironmentScript(Paths.get(preScenarioScript), input);
            if (result.contains("OK")) {
                testScenario.setIsPreScriptSuccessful(true);
            }
        }
        testExecutor.init(testLocation, getTestName(), testScenario);
        for (String script : scripts) {
            testExecutor.execute(script, input);
        }
        if (!StringUtil.isStringNullOrEmpty(postScenarioScript)) {
            String result = testExecutor.executeEnvironmentScript(Paths.get(postScenarioScript), input);
            if (result.contains("OK")) {
                testScenario.setIsPostScriptSuccessful(true);
            }
        }
        return this.testScenario;
    }
}
