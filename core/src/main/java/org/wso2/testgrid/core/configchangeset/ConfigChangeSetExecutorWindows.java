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

package org.wso2.testgrid.core.configchangeset;

import org.wso2.testgrid.common.ConfigChangeSet;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.TestPlan;

/**
 * This class execute commands for applying config change set on WINDOWS machines.
 * Todo this class should be implement according to the Windows environment
 *
 */
public class ConfigChangeSetExecutorWindows extends ConfigChangeSetExecutor {

    /**
     * Apply config change set script before run test scenarios
     *
     * @param testPlan the test plan
     * @param configChangeSet   config change set
     * @param deploymentCreationResult
     * @return execution passed or failed
     */
    @Override
    public boolean applyConfigChangeSet(TestPlan testPlan, ConfigChangeSet configChangeSet,
                                        DeploymentCreationResult deploymentCreationResult) {
        return false;
    }

    /**
     * Revert config change set script after run test scenarios
     *
     * @param testPlan the test plan
     * @param configChangeSet the config change set
     * @param deploymentCreationResult deployment creation result
     * @return execution passed or failed
     */
    public boolean revertConfigChangeSet(TestPlan testPlan, ConfigChangeSet configChangeSet,
                                         DeploymentCreationResult deploymentCreationResult) {
        return false;
    }

    /**
     * Initialize agent before running config change set
     *
     * @param testPlan  The test plan
     * @return          True if execution success. Else, false
     */
    @Override
    public boolean initConfigChangeSet(TestPlan testPlan) {
        return false;
    }

    /**
     * Revert back changes did in initConfigChangeSet
     *
     * @param testPlan  The test plan
     * @return          True if execution success. Else, false
     */
    @Override
    public boolean deInitConfigChangeSet(TestPlan testPlan) {
        return false;
    }
}
