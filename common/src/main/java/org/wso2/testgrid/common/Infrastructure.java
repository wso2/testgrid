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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Defines a model object for a Infrastructure.
 *
 * @since 1.0.0
 */
public class Infrastructure implements Serializable {

    private static final long serialVersionUID = -1660815137752094462L;

    private String name;
    private ProviderType providerType;
    private InstanceType instanceType;
    private ClusterType clusterType;
    private InfraCombination infraCombination;
    private Map<String, String> securityProperties;
    private List<Script> scripts;
    private String region;
    private String imageId;

    /**
     * Returns the name of the infrastructure.
     *
     * @return infrastructure name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the infrastructure.
     *
     * @param name infrastructure name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the provider type of the infrastructure.
     *
     * @return infrastructure provider type
     */
    public ProviderType getProviderType() {
        return providerType;
    }

    /**
     * Sets the provider type of the infrastructure.
     *
     * @param providerType infrastructure provider type
     */
    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    /**
     * Returns the instance type of the infrastructure.
     *
     * @return the instance type of the infrastructure
     */
    public InstanceType getInstanceType() {
        return instanceType;
    }

    /**
     * Sets the instance type of the infrastructure.
     *
     * @param instanceType instance type of the infrastructure
     */
    public void setInstanceType(InstanceType instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Returns the cluster type of the infrastructure.
     *
     * @return cluster type of the infrastructure
     */
    public ClusterType getClusterType() {
        return clusterType;
    }

    /**
     * Sets the cluster type of the infrastructure.
     *
     * @param clusterType cluster type of the infrastructure
     */
    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    /**
     * Returns the infrastructure combination.
     *
     * @return infrastructure combination
     */
    public InfraCombination getInfraCombination() {
        return infraCombination;
    }

    /**
     * Sets the infrastructure combination.
     *
     * @param infraCombination infrastructure combination
     */
    public void setInfraCombination(InfraCombination infraCombination) {
        this.infraCombination = infraCombination;
    }

    /**
     * Returns the security properties associated with the infrastructure.
     *
     * @return security properties associated with the infrastructure
     */
    public Map<String, String> getSecurityProperties() {
        return securityProperties;
    }

    /**
     * Sets the security properties associated with the infrastructure.
     *
     * @param securityProperties security properties associated with the infrastructure
     */
    public void setSecurityProperties(Map<String, String> securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Returns the infrastructure scripts.
     *
     * @return infrastructure scripts
     */
    public List<Script> getScripts() {
        return scripts;
    }

    /**
     * Sets the infrastructure scripts.
     *
     * @param scripts infrastructure scripts
     */
    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    /**
     * Returns the region for the infrastructure.
     *
     * @return region for the infrastructure
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region for the infrastructure.
     *
     * @param region region for the infrastructure
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Returns the image id of the infrastructure.
     *
     * @return image id of the infrastructure
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * Sets the image id of the infrastructure.
     *
     * @param imageId image id of the infrastructure
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * Defines the infrastructure provider types.
     *
     * @since 1.0.0
     */
    public enum ProviderType {
        AWS("AWS"),
        OPENSTACK("OpenStack"),
        GCP("GCP");

        private final String providerType;

        /**
         * Sets the provider type for the infrastructure.
         *
         * @param providerType infrastructure provider type
         */
        ProviderType(String providerType) {
            this.providerType = providerType;
        }

        @Override
        public String toString() {
            return this.providerType;
        }
    }

    /**
     * Defines the cluster types.
     *
     * @since 1.0.0
     */
    public enum ClusterType {
        ECS("ECS"),
        K8S("Kubernetes"),
        None("None");

        private final String clusterType;

        /**
         * Sets the cluster type for the infrastructure.
         *
         * @param clusterType infrastructure cluster type
         */
        ClusterType(String clusterType) {
            this.clusterType = clusterType;
        }

        @Override
        public String toString() {
            return this.clusterType;
        }
    }

    /**
     * Defines the instance types.
     *
     * @since 1.0.0
     */
    public enum InstanceType {
        EC2("EC2"),
        DOCKER_CONTAINERS("Docker");

        private final String instanceType;

        /**
         * Sets the instance type for the infrastructure.
         *
         * @param instanceType infrastructure instance type
         */
        InstanceType(String instanceType) {
            this.instanceType = instanceType;
        }

        @Override
        public String toString() {
            return this.instanceType;
        }
    }
}
