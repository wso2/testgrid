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

package org.wso2.carbon.testgrid.common;

import org.wso2.carbon.testgrid.common.exception.TestAutomationEngineException;

/**
 * This interface defines the contract of the TestAutomationEngine where the automation tests will be executed.
 */
public interface TestAutomationEngine {

    /**
     *
     * @param scenario the Test scenario that should be executed.
     * @param location the cloned location of the test-plan.
     * @param deployment the Deployment info tests should be executed against.
     * @return true if all the processes finished or false in an erroneous situation.
     * @throws TestAutomationEngineException when there is an error in the process.
     */
     boolean runScenario(TestScenario scenario, String location, Deployment deployment) throws
             TestAutomationEngineException;

    /**
     *
     * @param scenario the Test scenario that should be aborted.
     * @return the status of the operation.
     * @throws TestAutomationEngineException when there is an error in the process.
     */
     boolean abortScenario(TestScenario scenario) throws TestAutomationEngineException;
}
