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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.testgrid.common.config;

import org.apache.commons.collections4.ListUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * This describes a deployment repository.
 *
 * A deployment repository contain a set of deployment patterns.
 * Testgrid need to know what's the deployer technology used, and
 * how to execute those deploy scripts into a pre-provisioned infrastructure.
 *
 * Hence, this class represents the DeploymentConfig element of the testgrid.yaml.
 * It is one of the top three elements we have:
 *
 * <li>
 *     <ul>1. {@link InfrastructureConfig}</ul>
 *     <ul>2. {@link DeploymentConfig}</ul>
 *     <ul>3. {@link ScenarioConfig}</ul>
 * </li>
 *
 */
public class DeploymentConfig implements Serializable {

    private static final long serialVersionUID = -3264052940825641013L;

    private List<DeploymentPattern> deploymentPatterns;

    public DeploymentConfig() {
        this(Collections.emptyList());
    }

    public DeploymentConfig(List<DeploymentPattern> deploymentPatterns) {
        this.deploymentPatterns = deploymentPatterns;
    }

    public List<DeploymentPattern> getDeploymentPatterns() {
        return ListUtils.emptyIfNull(deploymentPatterns);
    }

    public DeploymentPattern getFirstDeploymentPattern() {
        if (deploymentPatterns == null || deploymentPatterns.isEmpty()) {
            return null;
        }
        return deploymentPatterns.get(0);
    }

    public void setDeploymentPatterns(
            List<DeploymentPattern> deploymentPatterns) {
        this.deploymentPatterns = deploymentPatterns;
    }

    /**
     * Describes a given deployment pattern within a deployment repository.
     * A deployment pattern config should describe its name, where it is located,
     * and what scripts are there that can be executed.
     */
    public static class DeploymentPattern implements Serializable {
        private static final long serialVersionUID = 5484623288608884369L;

        private String name;
        private String description;
        private String dir;
        private String remoteRepository;
        private String remoteBranch = "master";
        private List<Script> scripts;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * The git repo URL of the remote git repository.
         * This parameter is only used within Jenkins pipeline (for cloning purpose).
         * It does not have any relevance within the testgrid core.
         *
         * Same applies to {@link #getRemoteBranch()} as well.
         */
        public String getRemoteRepository() {
            return remoteRepository;
        }

        public void setRemoteRepository(String remoteRepository) {
            this.remoteRepository = remoteRepository;
        }

        /**
         * The git branch name of the remote git repository.
         *
         */
        public String getRemoteBranch() {
            return remoteBranch;
        }

        public void setRemoteBranch(String remoteBranch) {
            this.remoteBranch = remoteBranch;
        }

        /**
         * Returns the relative location where the deployment pattern is located
         * wrt to the Deployment repo directory.
         *
         * @return the deployment pattern directory
         */
        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public List<Script> getScripts() {
            return ListUtils.emptyIfNull(scripts);
        }

        public void setScripts(List<Script> scripts) {
            this.scripts = scripts;
        }

        @Override
        public String toString() {
            return "DeploymentPattern{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}
