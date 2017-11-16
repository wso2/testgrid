/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.testgrid.deployment.deployers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.DeployerService;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Utils;
import org.wso2.carbon.testgrid.common.exception.CommandExecutionException;
import org.wso2.carbon.testgrid.common.exception.TestGridDeployerException;
import org.wso2.carbon.testgrid.deployment.DeployerConstants;
import org.wso2.carbon.testgrid.deployment.DeploymentUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Performs deployment of artifacts using Puppet for running tests.
 */
public class PuppetDeployer implements DeployerService {
    private static final Log log = LogFactory.getLog(PuppetDeployer.class);
    private static final String DEPLOYER_NAME = "puppet";

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    @Override
    public Deployment deploy(Deployment deployment) throws TestGridDeployerException {
        String testPlanLocation = deployment.getDeploymentScriptsDir();

        //Set read,write and execute permissions to files related to deployment
        try {
            Utils.executeCommand("chmod -R 777 " + testPlanLocation, null);
        } catch (CommandExecutionException e) {
            throw new TestGridDeployerException("Error occurred while executing the filesystem permission command.", e);
        }
        System.setProperty("user.dir", Paths.get(testPlanLocation, DeployerConstants.PRODUCT_IS_DIR).toString());
        File file = new File(System.getProperty("user.dir"));

        log.info("Deploying kubernetes artifacts...");
        //Execute deploy.sh scripts with required arguments to deploy artifacts
        try {
            if (Utils.executeCommand("./deploy.sh "
                    + getKubernetesMaster(Paths.get(testPlanLocation, DeployerConstants.K8S_PROPERTIES_FILE).toString())
                    + " " + DeployerConstants.WSO2_PRIVATE_DOCKER_URL + " "
                    + DeployerConstants.USERNAME + " "
                    + DeployerConstants.PASSWORD + " "
                    + DeployerConstants.DOCKER_EMAIL, file)) {
                return DeploymentUtil.getDeploymentInfo(testPlanLocation);
            } else {
                throw new TestGridDeployerException("Error occurred while deploying artifacts");
            }
        } catch (CommandExecutionException e) {
            throw new TestGridDeployerException("Error occurred while executing the deploy script.", e);
        }
    }

    /**
     * Retrieves the value of KUBERNETES_MASTER property.
     *
     * @param location location of k8s.properties file
     * @return String value of KUBERNETES_MASTER property
     */
    private String getKubernetesMaster(String location) {
        Properties prop = new Properties();

        try (InputStream inputStream = new FileInputStream(location)) {
            prop.load(inputStream);
        } catch (IOException e) {
            String msg = "Error occurred while getting KUBERNETES_MASTER environment variable";
            log.error(msg, e);
        }
        return prop.getProperty("KUBERNETES_MASTER");
    }
}
