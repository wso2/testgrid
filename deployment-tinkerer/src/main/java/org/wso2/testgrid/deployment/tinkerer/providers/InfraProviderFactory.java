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
package org.wso2.testgrid.deployment.tinkerer.providers;

import java.util.Optional;

/**
 * This is the factory implementation for the {@link Provider} interface.
 * the appropriate provider subclass will be created depending on the provider
 * String provided.
 *
 * @since 1.0.0
 */
public class InfraProviderFactory {

    private static final String AWS_PROVIDER = "aws";

    /**
     * Returns an Optional Provider implementation. the Optional will be empty if the
     * provider is not identified.
     *
     * @param provider the cloud provider identifier
     * @return Optional of the Provider implementation
     */
    public static Optional<Provider> getInfrastructureProvider(String provider) {

        if (AWS_PROVIDER.equalsIgnoreCase(provider)) {
            return Optional.of(new AWSProvider());
        } else {
            return Optional.empty();
        }
    }
}
