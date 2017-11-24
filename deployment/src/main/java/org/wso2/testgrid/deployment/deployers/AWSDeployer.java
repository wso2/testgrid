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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.testgrid.common.DeployerService;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.deployment.DeploymentValidator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * This class performs AWS related deployment tasks.
 *
 * @since 1.0.0
 */
public class AWSDeployer implements DeployerService {

    private static final Log log = LogFactory.getLog(AWSDeployer.class);
    private static final String DEPLOYER_NAME = "AWS_CF";

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    @Override
    public Deployment deploy(Deployment deployment) throws TestGridDeployerException {
        //wait for server startup
        log.info("Deploying the pattern..");
        DeploymentValidator validator = new DeploymentValidator();
        log.info("Waiting for server startup..");
        for (Host host : deployment.getHosts()) {
            try {
                new URL(host.getIp());
            } catch (MalformedURLException e) {
                continue;
            }
            validator.waitForDeployment(host.getIp(), 20, TimeUnit.MINUTES, 15, TimeUnit.SECONDS);
        }
        return deployment;
    }
}
