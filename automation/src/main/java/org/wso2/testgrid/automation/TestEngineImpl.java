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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.testgrid.automation.util.AutomationUtil;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.TestAutomationEngine;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.Utils;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestAutomationEngineException;
import org.wso2.testgrid.common.util.StringUtil;

import java.util.List;

/**
 * This class is responsible for executing a test scenario.
 *
 * @since 1.0.0
 */
public class TestEngineImpl implements TestAutomationEngine {

    private static final Log log = LogFactory.getLog(TestEngineImpl.class);

    @Override
    public boolean runScenario(TestScenario scenario, String location, Deployment deployment)
            throws TestAutomationEngineException {
        log.info("Executing Tests for Solution Pattern : " + scenario.getName());
        String testLocation = Utils.getTestScenarioLocation(scenario, location);
        List<Test> tests = getTests(scenario, testLocation);
        try {
            for (Test test : tests) {
                log.info(StringUtil.concatStrings("Executing ", test.getTestName(), " Test"));
                scenario = test.execute(testLocation, deployment);
                log.info("---------------------------------------");
            }
        } catch (TestAutomationException e) {
            throw new TestAutomationEngineException("Error occurred when executing tests.", e);
        } catch (CommandExecutionException e) {
            throw new TestAutomationEngineException("Error occurred when executing tests.", e);
        }
        scenario.setStatus(TestScenario.Status.TEST_SCENARIO_COMPLETED);
        return true;
    }

    @Override
    public boolean abortScenario(TestScenario scenario) throws TestAutomationEngineException {
        return false;
    }

    /**
     * Read and returns a list of {@link Test} instances for the given test scenario.
     *
     * @param testScenario test scenario to obtain tests from
     * @param testLocation location on which test files are
     * @return a list of {@link Test} instances for the given test scenario
     * @throws TestAutomationEngineException thrown when error on reading test files (such as .jmx for JMeter tests)
     */
    private List<Test> getTests(TestScenario testScenario, String testLocation) throws TestAutomationEngineException {
        try {
            return AutomationUtil.getTests(testLocation, testScenario);
        } catch (TestAutomationException e) {
            throw new TestAutomationEngineException("Error while reading tests for test scenario.", e);
        }
    }
}
