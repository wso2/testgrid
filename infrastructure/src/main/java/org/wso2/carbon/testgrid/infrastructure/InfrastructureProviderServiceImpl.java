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
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.InfrastructureProvider;
import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;

/**
 * This class creates the infrastructure for running tests
 */
public class InfrastructureProviderServiceImpl implements InfrastructureProvider {

    private static final Log log = LogFactory.getLog(InfrastructureProviderServiceImpl.class);
    private final static String SHELL_SCRIPT_PROVIDER = "Shell";

    @Override
    public String getProviderName() {
        return SHELL_SCRIPT_PROVIDER;
    }

    @Override
    public boolean canHandle(Infrastructure infrastructure) {
        return true;
    }

    @Override
    public Deployment createInfrastructure(Infrastructure infrastructure, String infraRepoDir) throws TestGridInfrastructureException {
//        String testPlanLocation = infrastructure.getHome() +"/test-grid-is-resources/DeploymentPatterns/" + infrastructure.getDeploymentPattern();
//
//        System.out.println("Initializing terraform...");
//        log.info("Initializing terraform...");
//        Utils.executeCommand("terraform init " + testPlanLocation + "/OpenStack", null);
//
//        System.out.println("Creating the Kubernetes cluster...");
//        log.info("Creating the Kubernetes cluster...");
//        Utils.executeCommand("bash " + testPlanLocation + "/OpenStack/infra.sh", null);
//        infrastructure.setStatus(TestPlan.Status.INFRASTRUCTURE_READY);
        return null;
    }

    @Override
    public boolean removeInfrastructure(Deployment deployment, String infraRepoDir) throws TestGridInfrastructureException {
//        String testPlanLocation = deployment.getHome() +"/test-grid-is-resources/DeploymentPatterns/" + deployment.getDeploymentPattern();
//        System.out.println("Destroying test environment...");
//        if(Utils.executeCommand("sh " + testPlanLocation + "/OpenStack/cluster-destroy.sh", null)) {
//            return true;
//        }
        return false;
    }
}
