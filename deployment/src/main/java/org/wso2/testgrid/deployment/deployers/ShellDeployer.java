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
package org.wso2.testgrid.deployment.deployers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.ShellExecutor;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * This class performs Shell related deployment tasks.
 *
 * @since 1.0.0
 */
public class ShellDeployer implements Deployer {

    private static final Logger logger = LoggerFactory.getLogger(ShellDeployer.class);
    private static final String DEPLOYER_NAME = "SHELL";
    private static final String DEPLOY_SCRIPT_NAME = "deploy.sh";

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    @Override
    public DeploymentCreationResult deploy(TestPlan testPlan,
            InfrastructureProvisionResult infrastructureProvisionResult) throws TestGridDeployerException {
        DeploymentConfig.DeploymentPatternConfig deploymentPatternConfig = testPlan.getDeploymentConfig()
                .getDeploymentPatterns().get(0);
        logger.info("Performing the Deployment " + deploymentPatternConfig.getName());
        try {
            ShellExecutor shellExecutor = new ShellExecutor(Paths.get(TestGridUtil.getTestGridHomePath()));
            // TODO: DEPLOY_SCRIPT_NAME is hard-coded. We should first find the script name in the DeploymentConfig
            if (!shellExecutor
                    .executeCommand("bash " + Paths
                            .get(infrastructureProvisionResult.getDeploymentScriptsDir(), DEPLOY_SCRIPT_NAME))) {
                throw new TestGridDeployerException("Error occurred while executing the deploy script");
            }
        } catch (CommandExecutionException e) {
            throw new TestGridDeployerException(e);
        } catch (IOException e) {
            throw new TestGridDeployerException("Error occurred while retrieving the Testgrid_home ", e);
        }

        DeploymentCreationResult result = new DeploymentCreationResult();
        result.setName(deploymentPatternConfig.getName());
        result.setHosts(infrastructureProvisionResult.getHosts());
        result.setDeploymentScriptsDir(infrastructureProvisionResult.getDeploymentScriptsDir());

        return result;
    }
}
