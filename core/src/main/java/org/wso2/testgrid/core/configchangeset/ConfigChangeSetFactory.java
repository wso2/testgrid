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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.core.configchangeset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.core.TestPlanExecutor;

import java.util.Optional;

/**
 * This factory create appropriate config change set executor class for given OS.
 *
 * @since 1.0.0
 */
public class ConfigChangeSetFactory {

    private static final Logger logger = LoggerFactory.getLogger(ConfigChangeSetFactory.class);

    /**
     * Generate Config change set executor for given OS
     * @param osCategory type of agent OS as WINDOWS or UNIX
     * @return Object to run config change set commands
     */
    public static Optional<ConfigChangeSetExecutor> getExecutor(TestPlanExecutor.OSCategory osCategory) {
        if (osCategory.equals(TestPlanExecutor.OSCategory.UNIX)) {
            logger.debug("OSCatagory is UNIX");
            return Optional.of(new ConfigChangeSetExecutorUnix());
        } else if (osCategory.equals(TestPlanExecutor.OSCategory.WINDOWS)) {
            logger.debug("OSCatagory is WINDOWS");
            return Optional.of(new ConfigChangeSetExecutorWindows());
        } else {
            return Optional.empty();
        }
    }
}
