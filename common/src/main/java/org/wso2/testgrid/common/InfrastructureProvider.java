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

package org.wso2.testgrid.common;

import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;

/**
 * This Interface has to be implemented by the InfrastructureProviders like AWS, GCC, OpenStack.
 */
public interface InfrastructureProvider {

    /**
     * This method returns the provider name (AWS/GCP/Open Stack etc).
     *
     * @return a String indicating the name of the provider.
     */
    String getProviderName();

    /**
     * This method returns whether the provider can handle the requested infrastructure.
     *
     * @return a boolean indicating whether can handle or not.
     */
    boolean canHandle(InfrastructureConfig infrastructureConfig);

    /**
     * This method can be used to initialize the infrastructure provider.
     * Initialization may include usecases such as the initial log-in into a
     * cloud provider.
     */
    void init() throws TestGridInfrastructureException;

    /**
     * This method creates the necessary infrastructureConfig using the provided configuration.
     *
     * @param testPlan {@link TestPlan} with current test run specifications
     * @return Deployment Deployment object including the created host, ip details
     * @throws TestGridInfrastructureException thrown when error occurs in the infrastructure creation process.
     */
    InfrastructureProvisionResult provision(TestPlan testPlan) throws
            TestGridInfrastructureException;

    /**
     * This method executes commands needed to remove the infrastructure.
     *
     * @param infrastructureConfig an instance of a InfrastructureConfig in which Infrastructure should be removed.
     * @param infraRepoDir         location of the cloned repository related to infrastructure.
     * @return boolean status of the operation
     * @throws TestGridInfrastructureException thrown when error occurs in the infrastructure destroy process.
     */
    boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir) throws
            TestGridInfrastructureException;

}
