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


import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;

/**
 * This Interface has to be implemented by the InfrastructureProviders like AWS, GCC, OpenStack.
 */
public interface InfrastructureProvider {

    /**
     * This method returns the provider name (AWS/GCP/Open Stack etc).
     *
     * @return A String indicating the name of the provider.
     */
     String getProviderName();

    /**
     * This method creates the necessary infrastructure using the provided configuration.
     *
     * @param infrastructure An instance of a Infrastructure which includes the details of the infrastructure
     *                       that should be created.
     * @return Deployment -  Deployment object including the created host, ip details
     * @throws TestGridInfrastructureException
     */
    Deployment createInfrastructure(Infrastructure infrastructure) throws TestGridInfrastructureException;

    /**
     * This method executes commands needed to remove the infrastructure.
     *
     * @param deployment An instance of a Deployment which Infrastructure should be removed.
     * @return boolean status of the operation
     * @throws TestGridInfrastructureException
     */
    boolean removeInfrastructure(Deployment deployment) throws TestGridInfrastructureException;

}
