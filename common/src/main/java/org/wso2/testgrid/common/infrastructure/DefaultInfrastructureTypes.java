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
 *
 */

package org.wso2.testgrid.common.infrastructure;

/**
 * This constants class lists the default infrastructure types
 * that is frequently used.
 *
 * Any infrastructure provider specific type (say, AMI_ID) will
 * need to go to its own constants class, or possibly to the
 * {@link org.wso2.testgrid.common.InfrastructureProvider} implementation.
 *
 */
public class DefaultInfrastructureTypes {

    public static final String OPERATING_SYSTEM = "operating_system";

    /**
     * deprecated in favor of {@link DefaultInfrastructureTypes#DB_ENGINE} and
     * {@link DefaultInfrastructureTypes#DB_ENGINE_VERSION}
     */
    public static final String DATABASE = "database";

    public static final String DB_ENGINE = "DB_ENGINE";
    public static final String DB_ENGINE_VERSION = "DB_ENGINE_VERSION";
    public static final String JDK = "JDK";

    private DefaultInfrastructureTypes() {
    }
}
