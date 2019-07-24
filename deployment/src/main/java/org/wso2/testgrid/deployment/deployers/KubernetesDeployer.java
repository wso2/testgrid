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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridDeployerException;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class performs Kubernetes related deployment tasks. This class is used to deploy
 * the kubernetes deployer script which is used to deploy the deployments and services
 * in the kubernetes engine.
 *
 * @since 1.0.0
 */
public class KubernetesDeployer implements Deployer {

    private static final String DEPLOYER_NAME = TestPlan.DeployerType.KUBERNETES.toString();
    private static final Logger logger = LoggerFactory.getLogger(KubernetesDeployer.class);

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    /**
     * This class is used to invoke the script to deploy the deployments in Kubernetes Engine
     *
     * @param testPlan current testPlan configurations
     * @param infrastructureProvisionResult infrastructure provisioning output
     * @return
     * @throws TestGridDeployerException
     */
    @Override
    public DeploymentCreationResult deploy(TestPlan testPlan,
                                           InfrastructureProvisionResult infrastructureProvisionResult,
                                           Script script)
            throws TestGridDeployerException {

        String deployRepositoryLocation = Paths.get(testPlan.getDeploymentRepository()).toString();

        InputStream resourceFileStream = getClass().getClassLoader()
                .getResourceAsStream(TestGridConstants.KUBERNETES_DEPLOY_SCRIPT);

        InputStream helperFileStream = getClass().getClassLoader()
                .getResourceAsStream(TestGridConstants.KUBERNETES_GROOVY_HELPER);

        try {
            Files.copy(resourceFileStream, Paths.get(testPlan.getDeploymentRepository(),
                    TestGridConstants.KUBERNETES_DEPLOY_SCRIPT));
        } catch (IOException e) {
            logger.error("IO error occurred while reading " +
                    TestGridConstants.KUBERNETES_DEPLOY_SCRIPT, e);
        }

        try{
            Files.copy(helperFileStream , Paths.get(testPlan.getDeploymentRepository(),
                    TestGridConstants.KUBERNETES_GROOVY_HELPER));
        }catch (IOException e){
            logger.error("IO error occurred while reading " +
                    TestGridConstants.KUBERNETES_GROOVY_HELPER, e);
        }

        DeploymentCreationResult deploymentCreationResult = ShellDeployerFactory.deploy(testPlan,
                infrastructureProvisionResult,
                Paths.get(deployRepositoryLocation, TestGridConstants.KUBERNETES_DEPLOY_SCRIPT));
        return deploymentCreationResult;
    }
}
