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
 *  Defines a model object for a Infrastructure.
 */
public class Infrastructure {

    private String operatingSystem;
    private String osVersion;
    private String databaseEngine;
    private String databaseVersion;
    private InfrastructureProviderType providerType; //AWS, GCC, OPENSTACK
    private String instanceType; // EC2, DOCKER
    private String clusterType; // ECS, K8S
    private List<Node> nodes;

    public enum InfrastructureProviderType {
        AWS ("AWS"),
        OPENSTACK ("OpenStack"),
        GCC ("GCC");

        private final String name;

        InfrastructureProviderType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getDatabaseEngine() {
        return databaseEngine;
    }

    public void setDatabaseEngine(String databaseEngine) {
        this.databaseEngine = databaseEngine;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public InfrastructureProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(InfrastructureProviderType providerType) {
        this.providerType = providerType;
    }
}
