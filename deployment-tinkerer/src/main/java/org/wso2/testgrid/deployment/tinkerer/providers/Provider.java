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

package org.wso2.testgrid.deployment.tinkerer.providers;

import java.util.Optional;
/**
 * Interface for Cloud provider specific functionality.
 *
 * <p>This interface is providing generic methods related to different cloud
 * vendors like aws, azure, gce.
 *
 * @since 1.0.0
 */
public interface Provider {

    /**
     * Returns the name of the instance specified by region and the instance id.
     *
     * @param region     - Region of the stack.
     * @param instanceId - Id of the instance.
     * @return The name of the instance.
     */
    Optional<String> getInstanceName(String region, String instanceId);

    /**
     * Returns the instance username for the OS
     *
     * @param region region of the stack.
     * @param instanceId Id of the instance
     * @return An Optional instance of the username
     */
    Optional<String> getInstanceUserName(String region, String instanceId);
}
