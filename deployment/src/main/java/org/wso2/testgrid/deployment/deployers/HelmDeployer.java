/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.testgrid.deployment.deployers;

import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.deployment.util.ResourceFileUtil;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * This class performs Kubernetes related deployment tasks using Helm. This class is used to deploy
 * the Helm deployer script which is used to deploy the deployments and services
 * in the Kubernetes engine using Helm charts.
 *
 * @since 1.0.0
 */
public class HelmDeployer implements Deployer {

    private static final String DEPLOYER_NAME = TestPlan.DeployerType.HELM.toString();

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    /**
     * This class is used to invoke the script to deploy the deployments in Kubernetes Engine using helm
     *
     * @param testPlan                      current testPlan configurations
     * @param infrastructureProvisionResult infrastructure provisioning output
     * @return deploymentCreationResult   result of the deployment
     * @throws TestGridDeployerException  Throws error if Execution of deploy script fails
     */
    @Override
    public DeploymentCreationResult deploy(TestPlan testPlan,
                                           InfrastructureProvisionResult infrastructureProvisionResult,
                                           Script script)
            throws TestGridDeployerException {
        ResourceFileUtil.createTempDashBoard(testPlan);
        ResourceFileUtil.createResources(testPlan, "HELM");

        String deployRepositoryLocation = Paths.get(testPlan.getDeploymentRepository()).toString();

        return ShellDeployerFactory.deploy(testPlan,
                infrastructureProvisionResult,
                Paths.get(deployRepositoryLocation, TestGridConstants.HELM_DEPLOY_SCRIPT));
    }
}
