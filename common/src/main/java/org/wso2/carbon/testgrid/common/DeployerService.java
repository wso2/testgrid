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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.testgrid.common;

import org.wso2.carbon.testgrid.common.exception.TestGridDeployerException;

/**
 * Interface for the deployment of the artifacts.
 */
public interface DeployerService {

    /**
     * This method returns the provider name (AWS/GCP/Open Stack etc).
     *
     * @return A String indicating the name of the provider.
     */
    String getDeployerName();

    /**
     * Runs deploy.sh script and deploys artifacts in the test cluster
     *
     * @param testPlan Current test plan
     * @return Deployment
     * @throws TestGridDeployerException
     */
    Deployment deploy(TestPlan testPlan) throws TestGridDeployerException;

    /**
     *
     * @param testPlan Current test plan
     * @return Deployment
     * @throws TestGridDeployerException
     */
    boolean unDeploy(TestPlan testPlan) throws TestGridDeployerException;
}
