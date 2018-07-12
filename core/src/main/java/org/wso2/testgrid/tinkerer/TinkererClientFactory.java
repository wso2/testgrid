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
package org.wso2.testgrid.tinkerer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.core.TestPlanExecutor;
import java.util.Optional;

/**
 *
 * This Factory class creates the appropriate Deployment tinkerer client for the Operating system
 * category.
 *
 * @since 1.0.0
 */
public class TinkererClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(TinkererClientFactory.class);

    /**
     * This method returns the correct Deployment tinkerer client for the {@link TestPlanExecutor.OSCategory}
     * type.
     *
     * @param osCategory os category of the instance where tinkerer agent is running
     * @return the subclass of {@link TinkererClient}
     */
    public static Optional<TinkererClient> getExecuter(TestPlanExecutor.OSCategory osCategory) {
        if (osCategory.equals(TestPlanExecutor.OSCategory.UNIX)) {
            logger.info("OSCategory is UNIX");
            return Optional.of(new UnixClient());
        } else if (osCategory.equals(TestPlanExecutor.OSCategory.WINDOWS)) {
            logger.info("OSCategory is WINDOWS");
            return Optional.of(new WindowsClient());
        } else {
            return Optional.empty();
        }
    }
}
