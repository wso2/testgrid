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

package org.wso2.carbon.testgrid.core;

import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.TestScenarioStatus;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;
import org.wso2.carbon.testgrid.common.exception.TestGridException;

/**
 * This defines the contract of TestGridMgtService in which will serve as the main entry point to the TestGrid
 * Framework.
 */
public interface TestGridMgtService {

    /**
     * This method adds a TestPlan to the TestGrid framework.
     *
     * @param  testConfiguration - An instance of TestConfiguration in which holds the configuration of the
     *                           ScenarioTests.
     * @return Returns the status of the operation
     * @throws TestGridException If something goes wrong while adding the TestPlan.
     */
    TestPlan addTestPlan(TestConfiguration testConfiguration) throws TestGridException;

    /**
     * This method triggers the execution of a TestPlan.
     *
     * @param  testPlan - An instance of TestPlan in which should be executed.
     * @return Returns the status of the operation
     * @throws TestGridException If something goes wrong while executing the TestPlan.
     */
    boolean executeTestPlan(TestPlan testPlan) throws TestGridException;

    /**
     * This method aborts the execution of a TestPlan.
     *
     * @param  testPlan - An instance of TestPlan in which should be aborted.
     * @return Returns the status of the operation
     * @throws TestGridException If something goes wrong while aborting the execution of the TestPlan.
     */
    boolean abortTestPlan(TestPlan testPlan) throws TestGridException;

    /**
     * This method fetches the status of a TestPlan.
     *
     * @param  testPlan - An instance of TestPlan in which should be monitored.
     * @return Returns the status of the TestPlan
     * @throws TestGridException If something goes wrong while checking the status of the TestPlan.
     */
    TestScenarioStatus getStatus(TestPlan testPlan) throws TestGridException;

}
