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

package org.wso2.testgrid.common;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Defines a model object for a Infrastructure.
 */
@Configuration(namespace = "wso2.testgrid.infrastructure",
               description = "TestGrid Infrastructure Configuration Parameters")
public class Infrastructure implements Serializable {

    private static final long serialVersionUID = -1660815137752094462L;

    @Element(description = "defines the infrastructure provider type (i.e. AWS, OpenStack)")
    private ProviderType providerType; //AWS, GCC, OPENSTACK
    @Element(description = "defines the required instance type (i.e. EC2, Docker)")
    private InstanceType instanceType; // EC2, DOCKER
    @Element(description = "defines the required cluster type (i.e. ECS, Kubernetes)")
    private ClusterType clusterType; // ECS, K8S
    @Element(description = "defines the database configuration")
    private Database database;
    @Element(description = "defines the database configuration")
    private JDK jdk;
    @Element(description = "holds the required properties for security related stuff")
    private Map<String, String> securityProperties;
    @Element(description = "holds the list of customized scripts if provided")
    private List<Script> scripts;
    @Element(description = "defines the os configuration")
    private OperatingSystem operatingSystem;
    @Element(description = "defines the name of this infrastructure")
    private String name;
    @Element(description = "defines the region in which the infrastructure should be created")
    private String region;
    @Element(description = "holds the additional properties for the infrastructure")
    private Map<String, String> infraArguments;

    /**
     * Defines the infrastructure provider types.
     */
    public enum ProviderType {
        AWS("AWS"),
        OPENSTACK("OpenStack"),
        GCP("GCP");

        private final String name;

        ProviderType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    /**
     * Defines the cluster types.
     */
    public enum ClusterType {
        ECS("ECS"), K8S("Kubernetes"), None("None");

        private final String name;

        ClusterType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    /**
     * Defines the instance types.
     */
    public enum InstanceType {
        EC2("EC2"), DOCKER_CONTAINERS("Docker");

        private final String name;

        InstanceType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    /**
     * Defines the supported jdks.
     */
    public enum JDK {
        JDK7("JDK7"), JDK8("JDK8");

        private final String name;

        JDK(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
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

    public List<Script> getScripts() {
        return scripts;
    }

    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Map<String, String> getInfraArguments() {
        return infraArguments;
    }

    public void setInfraArguments(Map<String, String> infraArguments) {
        this.infraArguments = infraArguments;
    }
}
