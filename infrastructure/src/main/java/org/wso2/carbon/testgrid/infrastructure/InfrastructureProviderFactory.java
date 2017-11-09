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

package org.wso2.carbon.testgrid.infrastructure;

import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.InfrastructureProvider;
import org.wso2.carbon.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.carbon.testgrid.common.exception.UnsupportedProviderException;

import java.util.ServiceLoader;

/**
 * This will return a InfrastructureProvider based on the required Infrastructure type.
 */
public class InfrastructureProviderFactory {

    static ServiceLoader<InfrastructureProvider> providers = ServiceLoader.load(InfrastructureProvider.class);

    /**
     * Returns a matching infrastructure provider to create the environment.
     *
     * @param infrastructure - an instance of the Infrastructure object
     * @return the status of the test plan
     */
    public static InfrastructureProvider getInfrastructureProvider(Infrastructure infrastructure) throws
            UnsupportedProviderException, InfrastructureProviderInitializationException {
        String providerName = infrastructure.getProviderType().name();

        for (InfrastructureProvider provider : providers) {

            if (provider.canHandle(infrastructure)) {
                try {
                    return provider.getClass().newInstance();
                } catch (InstantiationException e) {
                    throw new InfrastructureProviderInitializationException("Exception occurred while instantiating the" +
                            " InfrastructureProvider for requested type '" + providerName + "'", e);
                } catch (IllegalAccessException e) {
                    throw new InfrastructureProviderInitializationException("Exception occurred while instantiating the" +
                            " InfrastructureProvider for requested type '" + providerName + "'", e);
                }
            }
        }
        throw new UnsupportedProviderException("Unable to find a InfrastructureProvider for requested type '" +
                providerName + "'");
    }
}
