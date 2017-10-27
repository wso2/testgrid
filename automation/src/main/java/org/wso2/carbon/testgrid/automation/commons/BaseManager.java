/*
*Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.testgrid.automation.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.wso2.carbon.testgrid.automation.FrameworkConstants;
import org.wso2.carbon.testgrid.automation.beans.Deployment;
import org.wso2.carbon.testgrid.automation.util.GitRepositoryUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class handles initiates fundamental platform required for distributed deployment.
 */
public class BaseManager {

    protected Log log = LogFactory.getLog(BaseManager.class);

    public BaseManager() throws IOException, GitAPIException, InterruptedException {

        String resourceLocation = System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION);

        HashMap<String, Deployment> deploymentHashMap = DeploymentConfigurationReader.readConfiguration()
                .getDeploymentHashMap();
        List<Deployment> deploymentList = new ArrayList<>(deploymentHashMap.values());

        for (Deployment deployment : deploymentList) {
            try {
                log.info(
                        "clone git repo =>" + deployment.getRepository() + "  to =>" + resourceLocation + File.separator
                                + "Artifacts" + File.separator + deployment.getName());
                GitRepositoryUtil.gitCloneRepository(deployment.getRepository(),
                        resourceLocation + File.separator + "Artifacts" + File.separator + deployment.getName());
            } catch (GitAPIException e) {
                log.error("Git clone failed.", e);
            }
        }

    }

}

