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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.testgrid.deployment.deployers;

import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TimeOutBuilder;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.deployment.DeploymentValidator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * This class performs AWS related deployment tasks.
 *
 * @since 1.0.0
 */
public class AWSDeployer implements Deployer {

    private static final Logger logger = LoggerFactory.getLogger(AWSDeployer.class);
    private static final String DEPLOYER_NAME = "AWS_CF";
    private static final int TIMEOUT = 60;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;
    private static final TimeUnit POLL_UNIT = TimeUnit.SECONDS;
    private static final int POLL_INTERVAL = 15;

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    @Override
    public DeploymentCreationResult deploy(TestPlan testPlan,
            InfrastructureProvisionResult infrastructureProvisionResult)
            throws TestGridDeployerException {
        //wait for server startup
        DeploymentConfig.DeploymentPattern deploymentPatternConfig = testPlan.getDeploymentConfig()
                .getDeploymentPatterns().get(0);
        try {
            logger.info(
                    StringUtil.concatStrings("Waiting for server start-up.. : ", deploymentPatternConfig.getName()));

            DeploymentValidator validator = new DeploymentValidator();
            for (Host host : infrastructureProvisionResult.getHosts()) {
                try {
                    new URL(host.getIp());
                    logger.info("Waiting for server startup on URL : " + host.getIp());
                } catch (MalformedURLException e) {
                    logger.debug(StringUtil.concatStrings("Output Value : ", host.getIp(), " is Not a Valid URL, " +
                            "hence skipping to next value.."));
                    continue;
                }
                TimeOutBuilder deploymentTimeOut = new TimeOutBuilder(TIMEOUT, TIMEOUT_UNIT, POLL_INTERVAL, POLL_UNIT);
                validator.waitForDeployment(host.getIp(), deploymentTimeOut);
            }
        } catch (ConditionTimeoutException ex) {
            throw new TestGridDeployerException(StringUtil.concatStrings("Timeout occurred while waiting for pattern : "
                    , deploymentPatternConfig.getName(), "Timeout value : ", TIMEOUT, TIMEOUT_UNIT.toString()), ex);
        }

        DeploymentCreationResult deploymentCreationResult = new DeploymentCreationResult();
        deploymentCreationResult.setName(deploymentPatternConfig.getName());
        deploymentCreationResult.setHosts(infrastructureProvisionResult.getHosts());
        return deploymentCreationResult;
    }
}
