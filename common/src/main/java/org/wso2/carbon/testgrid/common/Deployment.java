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

import java.util.List;

/**
 *  Defines a model object for a created deployment.
 */
public class Deployment {

    private String name;
    private List<Host> hosts;
    private String deploymentScriptsDir;

    /**
     * Returns the name of the deployment.
     *
     * @return name of the deployment
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the deployment.
     *
     * @param name - Name of the deployment
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the list of hosts in the deployment.
     *
     * @return List of hosts in the deployment
     */
    public List<Host> getHosts() {
        return hosts;
    }

    /**
     * Sets the list of hosts in the deployment.
     *
     * @param hosts - list of hosts in the deployment
     */
    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    /**
     * Returns the location of the deployment scripts.
     *
     * @return the location of the deployment scripts
     */
    public String getDeploymentScriptsDir() {
        return deploymentScriptsDir;
    }

    /**
     * Sets the location of the deployment scripts.
     *
     * @param deploymentScriptsDir - location of the deployment scripts
     */
    public void setDeploymentScriptsDir(String deploymentScriptsDir) {
        this.deploymentScriptsDir = deploymentScriptsDir;
    }
}
