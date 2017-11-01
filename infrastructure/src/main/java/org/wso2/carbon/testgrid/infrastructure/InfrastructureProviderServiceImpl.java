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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.TestPlan;

import org.wso2.carbon.testgrid.utils.Util;

/**
 * This class creates the infrastructure for running tests
 */
public class InfrastructureProviderServiceImpl implements InfrastructureProviderService {

    private static final Log log = LogFactory.getLog(InfrastructureProviderServiceImpl.class);

    @Override
    public boolean createTestEnvironment(TestPlan testPlan) throws TestGridInfrastructureException {
        String testPlanLocation = testPlan.getTestScenarios().get(0).getScenarioLocation();

        System.out.println("Initializing terraform...");
        log.info("Initializing terraform...");
        Util.executeCommand("terraform init " + testPlanLocation + "/Scripts/OpenStack", null);

        System.out.println("Creating the Kubernetes cluster...");
        log.info("Creating the Kubernetes cluster...");
        Util.executeCommand("bash " + testPlanLocation + "/Scripts/OpenStack/infra.sh", null);

        return true;
    }

    @Override
    public boolean removeTestEnvironment(TestPlan testPlan) throws TestGridInfrastructureException {
        String testPlanLocation = testPlan.getTestScenarios().get(0).getScenarioLocation();
        System.out.println("Destroying test environment...");
        if(Util.executeCommand("sh " + testPlanLocation + "/Scripts/OpenStack/cluster-destroy.sh", null)) {
            return true;
        }
        return false;
    }
}
