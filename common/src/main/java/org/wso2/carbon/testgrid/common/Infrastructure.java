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
import java.util.Map;

/**
 *  Defines a model object for a Infrastructure.
 */
public class Infrastructure {

    private ProviderType providerType; //AWS, GCC, OPENSTACK
    private InstanceType instanceType; // EC2, DOCKER
    private ClusterType clusterType; // ECS, K8S
    private Database database;
    private List<Node> nodes;
    private Map<String, String> securityProperties;
    private OperatingSystem operatingSystem;

    public enum ProviderType {
        AWS ("AWS"),
        OPENSTACK ("OpenStack"),
        GCP ("GCP");

        private final String name;

        ProviderType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum ClusterType {
        ECS ("ECS"), K8S ("Kubernetes"), CLOUD_FORMATION ("Cloud Formation");

        private final String name;

        ClusterType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum InstanceType {
        EC2 ("EC2"), DOCKER_CONTAINERS ("Docker");

        private final String name;

        InstanceType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    public InstanceType getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(InstanceType instanceType) {
        this.instanceType = instanceType;
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public Map<String, String> getSecurityProperties() {
        return securityProperties;
    }

    public void setSecurityProperties(Map<String, String> securityProperties) {
        this.securityProperties = securityProperties;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}
