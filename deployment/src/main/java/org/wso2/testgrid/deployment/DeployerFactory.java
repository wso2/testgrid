/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.testgrid.deployment;

import org.wso2.testgrid.common.DeployerService;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.DeployerInitializationException;
import org.wso2.testgrid.common.exception.UnsupportedDeployerException;

import java.util.ServiceLoader;

/**
 * Returns a DeployerService for the requested deployer type.
 */
public class DeployerFactory {
    private static ServiceLoader<DeployerService> providers = ServiceLoader.load(DeployerService.class);

    /**
     * Return a matching Deployer for deploying artifacts.
     *
     * @param testPlan an instance of testPlan object
     * @return an instance of the requested Deployer
     * @throws DeployerInitializationException if instantiation of the requested Deployer fails
     * @throws UnsupportedDeployerException if an unsupported deployerType is provided
     */
    public static DeployerService getDeployerService(TestPlan testPlan)
            throws DeployerInitializationException, UnsupportedDeployerException {
        String deployerType = testPlan.getDeployerType().toString();

        for (DeployerService deployerService : providers) {
            if (deployerService.getDeployerName().equals(deployerType)) {
                try {
                    return deployerService.getClass().newInstance();
                } catch (InstantiationException e) {
                        throw new DeployerInitializationException("Exception occurred while instantiating the" +
                                " DeployerFactory for requested type '" + deployerType + "'", e);
                } catch (IllegalAccessException e) {
                    throw new DeployerInitializationException("Exception occurred while instantiating the" +
                            " DeployerFactory for requested type '" + deployerType + "'", e);
                }
            }
        }
        throw new UnsupportedDeployerException("Unable to find a Deployer for requested type '" +
                deployerType + "'");
    }
}
