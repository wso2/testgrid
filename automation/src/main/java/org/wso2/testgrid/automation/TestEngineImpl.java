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
import org.wso2.testgrid.automation.core.TestManager;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.TestAutomationEngine;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.Utils;
import org.wso2.testgrid.common.exception.TestAutomationEngineException;

/**
 * This class is Responsible for initiating the core processes of TestGrid.
 */
public class TestEngineImpl implements TestAutomationEngine {

    private static final Log log = LogFactory.getLog(TestEngineImpl.class);

    public boolean runScenario(TestScenario scenario, String location, Deployment deployment)
            throws TestAutomationEngineException {
        log.info("Executing Tests for Solution Pattern : " + scenario.getName());
        TestManager testManager = new TestManager();
        try {
            testManager.init(Utils.getTestScenarioLocation(scenario, location), deployment, scenario);
            testManager.executeTests();
            scenario.setStatus(TestScenario.Status.TEST_SCENARIO_COMPLETED);
        } catch (TestAutomationException ex) {
            throw new TestAutomationEngineException("Error while initiating the TestManager", ex);
        }
        return true;
    }

    public boolean abortScenario(TestScenario scenario) throws TestAutomationEngineException {
        return false;
    }
}
