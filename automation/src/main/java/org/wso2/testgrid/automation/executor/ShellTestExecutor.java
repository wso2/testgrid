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

package org.wso2.testgrid.automation.executor;

import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.config.ScenarioConfig;

/**
 * Responsible for performing the tasks related to execution of single test scenario.
 *
 * This will eventually replace the {@link JMeterExecutor}.
 *
 * @since 1.0.0
 */
public class ShellTestExecutor extends JMeterExecutor {

    @Override
    public void init(String testLocation, String testName, ScenarioConfig scenarioConfig)
            throws TestAutomationException {
        super.init(testLocation, testName, scenarioConfig);
    }

    @Override
    public void execute(String script, DeploymentCreationResult deploymentCreationResult)
            throws TestAutomationException {
        super.execute(script, deploymentCreationResult);
    }

}
