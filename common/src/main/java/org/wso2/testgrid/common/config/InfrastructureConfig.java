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
import org.wso2.testgrid.common.TestGridError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Defines a model object for a InfrastructureConfig.
 *
 * @see DeploymentConfig
 *
 * @since 1.0.0
 */
public class InfrastructureConfig implements Serializable, Cloneable {

    private static final long serialVersionUID = -1660815137752094462L;

    private IACProvider iacProvider;
    private InfrastructureProvider infrastructureProvider;
    private ContainerOrchestrationEngine containerOrchestrationEngine;
    private Properties parameters = new Properties();
    private List<Provisioner> provisioners;
    private List<String> excludes;
    private List<String> includes;

    public IACProvider getIacProvider() {
        return iacProvider;
    }

    public void setIacProvider(IACProvider iacProvider) {
        this.iacProvider = iacProvider;
    }

    public InfrastructureProvider getInfrastructureProvider() {
        return infrastructureProvider;
    }

    public void setInfrastructureProvider(
            InfrastructureProvider infrastructureProvider) {
        this.infrastructureProvider = infrastructureProvider;
    }

    public ContainerOrchestrationEngine getContainerOrchestrationEngine() {
        return containerOrchestrationEngine;
    }

    public void setContainerOrchestrationEngine(
            ContainerOrchestrationEngine containerOrchestrationEngine) {
        this.containerOrchestrationEngine = containerOrchestrationEngine;
    }

    /**
     * Contains a list of properties that show the infrastructure combination.
     */
    public Properties getParameters() {
        return parameters;
    }

    public void setParameters(Properties parameters) {
        this.parameters = parameters;
    }

    public List<Provisioner> getProvisioners() {
        return ListUtils.emptyIfNull(provisioners);
    }

    public Provisioner getFirstProvisioner() {
        if (provisioners == null || provisioners.isEmpty()) {
            return null;
        }
        return provisioners.get(0);
    }

    public void setProvisioners(List<Provisioner> provisioners) {
        this.provisioners = provisioners;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    /**
     * Defines the infrastructure-as-Code provider types.
     * This information is useful in case we need to pre-process
     * the infrastructure scripts before invocation.
     *
     * @since 1.0.0
     */
    public enum IACProvider {
        CLOUDFORMATION("CloudFormation"),
        TERRAFORM("Terraform"),
        KUBERNETES("KUBERNETES"),
        None("None");
        private final String iacProvider;

        IACProvider(String iacProvider) {
            this.iacProvider = iacProvider;
        }

        @Override
        public String toString() {
            return this.iacProvider;
        }
    }

    /**
     * Defines the infrastructure provider types.
     * This configuration is required in order for
     * testgrid to figure out what information are
     * needed to access the given cloud-provider.
     *
     * For example, AWS requires AWS credentials.
     *
     * @since 1.0.0
     */
    public enum InfrastructureProvider {
        AWS("AWS"),
        OPENSTACK("OpenStack"),
        GCP("GCP"),
        GKE("GKE"),
        SHELL("SHELL"),
        LOCAL("LOCAL");

        private final String providerType;

        InfrastructureProvider(String providerType) {
            this.providerType = providerType;
        }

        @Override
        public String toString() {
            return this.providerType;
        }
    }

    /**
     * Defines the ContainerOrchestrationEngine types.
     *
     * @since 1.0.0
     */
    public enum ContainerOrchestrationEngine {
        ECS("ECS"),
        K8S("Kubernetes"),
        None("None");

        private final String clusterType;

        ContainerOrchestrationEngine(String clusterType) {
            this.clusterType = clusterType;
        }

        @Override
        public String toString() {
            return this.clusterType;
        }
    }

    /**
     * Describe an infrastructure provisoner located within an infrastructure repository.
     * A provisioner is responsible for talking to relevant infrastructure providers to
     * provision an infrastructure.
     *
     * The provisioner has the same behavior as the {@link DeploymentConfig.DeploymentPattern}.
     *
     */
    public static class Provisioner extends DeploymentConfig.DeploymentPattern implements Cloneable {
        private static final long serialVersionUID = -3937792864579403430L;

        @Override
        public Provisioner clone() {
            try {
                Provisioner provisioner =  (Provisioner) super.clone();
                List<Script> scripts = new ArrayList<>();
                for (Script script : provisioner.getScripts()) {
                    scripts.add(script.clone());
                }
                provisioner.setScripts(scripts);
                return provisioner;
            } catch (CloneNotSupportedException e) {
                throw new TestGridError("Error occurred while cloning Provisioner object.", e);
            }
        }
    }

    @Override
    public InfrastructureConfig clone() {
        try {
            InfrastructureConfig infrastructureConfig = (InfrastructureConfig) super.clone();
            infrastructureConfig.setProvisioners(provisioners);
            infrastructureConfig.setParameters(parameters);
            infrastructureConfig.setContainerOrchestrationEngine(containerOrchestrationEngine);
            infrastructureConfig.setIacProvider(iacProvider);
            infrastructureConfig.setInfrastructureProvider(infrastructureProvider);
            infrastructureConfig.setIncludes(includes);

            return infrastructureConfig;
        } catch (CloneNotSupportedException e) {
            throw new TestGridError("Since the super class of this object is java.lang.Object that supports " +
                    "cloning this failure condition should never happen unless a serious system error occurred.", e);
        }
    }
}
