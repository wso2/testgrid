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

package org.wso2.carbon.testgrid.infrastructure.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.InfrastructureProvider;
import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;

/**
 * This class creates the infrastructure for running tests on OpenStack.
 */
public class OpenStackProvider implements InfrastructureProvider {

    private static final Log log = LogFactory.getLog(OpenStackProvider.class);
    private final static String OPENSTACK_PROVIDER = "OPENSTACK";

    @Override
    public String getProviderName() {
        return OPENSTACK_PROVIDER;
    }

    @Override
    public boolean canHandle(Infrastructure infrastructure) {
        return true;
    }

    @Override
    public Deployment createInfrastructure(Infrastructure infrastructure, String infraRepoDir) throws TestGridInfrastructureException {
        return null;
    }

    @Override
    public boolean removeInfrastructure(Deployment deployment, String infraRepoDir) throws TestGridInfrastructureException {
        return false;
    }
}
