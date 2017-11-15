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
package org.wso2.carbon.testgrid.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.exception.TestGridDeployerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class DeploymentUtil {

    /**
     * Reads the deployment.json file and constructs the deployment object.
     *
     * @param testPlanLocation location String of the test plan
     * @return the deployment information ObjectMapper
     * @throws TestGridDeployerException If reading the deployment.json file fails
     */
    public static Deployment getDeploymentInfo(String testPlanLocation) throws TestGridDeployerException {

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(Paths.get(testPlanLocation, DeployerConstants.PRODUCT_IS_DIR, DeployerConstants.DEPLOYMENT_FILE).toString());
        try {
            return mapper.readValue(file, Deployment.class);
        } catch (IOException e) {
            throw new TestGridDeployerException("Error occurred while reading the "
                    + DeployerConstants.DEPLOYMENT_FILE + " file", e);
        }
    }
}
