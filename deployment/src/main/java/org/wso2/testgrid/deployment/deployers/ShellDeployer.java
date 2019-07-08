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


import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import java.nio.file.Paths;


/**
 * This class performs Shell related deployment tasks.
 *
 * @since 1.0.0
 */
public class ShellDeployer implements Deployer {

    private static final String DEPLOYER_NAME = "SHELL";

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    @Override
    public DeploymentCreationResult deploy(TestPlan testPlan,
            InfrastructureProvisionResult infrastructureProvisionResult, Script script)
            throws TestGridDeployerException {

        String deployScriptLocation = Paths.get(testPlan.getDeploymentRepository()).toString();

        DeploymentCreationResult deploymentCreationResult = ShellDeployerFactory.deploy(testPlan,
                infrastructureProvisionResult, Paths.get(deployScriptLocation, script.getFile()));
        return deploymentCreationResult;
    }

    /**
     * This method returns the script matching the correct script phase.
     *
     * @param deploymentConfig {@link DeploymentConfig} object with current deployment configurations
     * @param scriptPhase      {@link Script.Phase} enum value for required script
     * @return the matching script from deployment configuration
     * @throws TestGridDeployerException if there is no matching script for phase defined
     */

}
