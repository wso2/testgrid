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
package org.wso2.testgrid.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.Port;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This holds the utility methods used by the deployment component.
 */
public class DeploymentUtil {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentUtil.class);

    /**
     * Reads the deployment.json file and constructs the deployment object.
     *
     * @deprecated We no longer use the deployment.json file. Hence, the entire logic in here is deprecated.
     * @param workspace location String of the test plan
     * @return the deployment information ObjectMapper
     * @throws TestGridDeployerException If reading the deployment.json file fails
     */
    @Deprecated
    public static DeploymentCreationResult getDeploymentCreationResult(String workspace)
            throws TestGridDeployerException {
        DeploymentCreationResult deploymentCreationResult = new DeploymentCreationResult();
        List<Host> hosts = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(Paths.get(workspace, DeployerConstants.DEPLOYMENT_FILE).toString());
        try {
            if (!file.exists()) {
                logger.debug("The deployment.json file was not found at: " + file.getPath() + ". This is where the "
                        + "deployment creation outputs are stored. Continuing without the deployment outputs.");
                return deploymentCreationResult;
            }

            List<Host> hostList = mapper.readValue(file, DeploymentCreationResult.class).getHosts();
            /* JMeter test files has the values for the host and ports as two properties. In order to replace
             * the values, the serverHost and serverPort has to be set as two different hosts.
             */
            for (Host host : hostList) {
                Host serverHost = new Host();
                serverHost.setIp(host.getIp());
                serverHost.setLabel("serverHost");
                for (Port port : host.getPorts()) {
                    Host serverPort = new Host();
                    serverPort.setIp(String.valueOf(port.getPortNumber()));
                    serverPort.setLabel("serverPort");
                    hosts.add(serverPort);
                }
                hosts.add(serverHost);
            }
            deploymentCreationResult.setHosts(hosts);
            return deploymentCreationResult;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new TestGridDeployerException(StringUtil.concatStrings(
                    "Error occurred while reading ", file.getAbsolutePath()), e);
        }
    }
}
