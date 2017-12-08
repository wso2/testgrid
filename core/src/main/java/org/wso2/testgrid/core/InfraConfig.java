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
package org.wso2.testgrid.core;

import org.apache.commons.lang3.SerializationUtils;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.testgrid.common.Database;
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.OperatingSystem;
import org.wso2.testgrid.common.Script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * This class is used to retrieve infrastructure configuration values.
 *
 * @since 1.0.0
 */
@Configuration(namespace = "wso2.testgrid.infrastructure",
               description = "TestGrid Infrastructure Configuration Parameters")
public class InfraConfig {

    private static final String PLACEHOLDER_OPERATING_SYSTEM_NAME = "operatingsystems.name";
    private static final String PLACEHOLDER_OPERATING_SYSTEM_VERSION = "operatingsystems.version";
    private static final String PLACEHOLDER_DATABASE_ENGINE = "databases.engine";
    private static final String PLACEHOLDER_DATABASE_VERSION = "databases.version";
    private static final String PLACEHOLDER_JDK = "jdks.jdk";

    @Element(description = "defines the name of this infrastructure", required = true)
    private String name;
    @Element(description = "defines the infrastructure provider type (i.e. AWS, OpenStack)", required = true)
    private Infrastructure.ProviderType providerType;
    @Element(description = "defines the required instance type (i.e. EC2, Docker)", required = true)
    private Infrastructure.InstanceType instanceType;
    @Element(description = "defines the required cluster type (i.e. ECS, Kubernetes)", required = true)
    private Infrastructure.ClusterType clusterType;
    @Element(description = "defines the required operating systems")
    private List<OperatingSystem> operatingSystems = Collections.emptyList();
    @Element(description = "defines the required databases")
    private List<Database> databases = Collections.emptyList();
    @Element(description = "defines the required jdks")
    private List<InfraCombination.JDK> jdks = Collections.emptyList();
    @Element(description = "defines a set of infra combinations to be excluded")
    private List<InfraCombination> excludeInfraCombinations = Collections.emptyList();
    @Element(description = "defines a set of infra combinations to be included")
    private List<InfraCombination> includeInfraCombinations = Collections.emptyList();
    @Element(description = "holds the required properties for security related stuff")
    private Map<String, String> securityProperties;
    @Element(description = "holds the list of customized scripts if provided")
    private List<Script> scripts = Collections.emptyList();
    @Element(description = "defines the region in which the infrastructure should be created")
    private String region;
    @Element(description = "defines the image to be used when setting up the instances")
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
    public Infrastructure.ProviderType getProviderType() {
        return providerType;
    }

    /**
     * Sets the provider type of the infrastructure.
     *
     * @param providerType infrastructure provider type
     */
    public void setProviderType(Infrastructure.ProviderType providerType) {
        this.providerType = providerType;
    }

    /**
     * Returns the instance type of the infrastructure.
     *
     * @return the instance type of the infrastructure
     */
    public Infrastructure.InstanceType getInstanceType() {
        return instanceType;
    }

    /**
     * Sets the instance type of the infrastructure.
     *
     * @param instanceType instance type of the infrastructure
     */
    public void setInstanceType(Infrastructure.InstanceType instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Returns the cluster type of the infrastructure.
     *
     * @return cluster type of the infrastructure
     */
    public Infrastructure.ClusterType getClusterType() {
        return clusterType;
    }

    /**
     * Sets the cluster type of the infrastructure.
     *
     * @param clusterType cluster type of the infrastructure
     */
    public void setClusterType(Infrastructure.ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    /**
     * Returns the operating systems for infrastructures.
     *
     * @return operating systems for infrastructures
     */
    public List<OperatingSystem> getOperatingSystems() {
        return operatingSystems;
    }

    /**
     * Sets the operating systems for infrastructures.
     *
     * @param operatingSystems operating systems for infrastructures
     */
    public void setOperatingSystems(List<OperatingSystem> operatingSystems) {
        this.operatingSystems = operatingSystems;
    }

    /**
     * Returns the databases for infrastructures.
     *
     * @return databases for infrastructures
     */
    public List<Database> getDatabases() {
        return databases;
    }

    /**
     * Sets databases for infrastructures.
     *
     * @param databases databases for infrastructures
     */
    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    /**
     * Returns the JDKs for infrastructures.
     *
     * @return the JDKs for infrastructures
     */
    public List<InfraCombination.JDK> getJdks() {
        return jdks;
    }

    /**
     * Sets the JDKs for infrastructures.
     *
     * @param jdks the JDKs for infrastructures
     */
    public void setJdks(List<InfraCombination.JDK> jdks) {
        this.jdks = jdks;
    }

    /**
     * Returns a list of {@link InfraCombination} to be excluded.
     *
     * @return {@link InfraCombination} to be excluded
     */
    public List<InfraCombination> getExcludeInfraCombinations() {
        return excludeInfraCombinations;
    }

    /**
     * Sets a list of {@link InfraCombination} to be excluded.
     *
     * @param excludeInfraCombinations {@link InfraCombination} to be excluded
     */
    public void setExcludeInfraCombinations(List<InfraCombination> excludeInfraCombinations) {
        this.excludeInfraCombinations = excludeInfraCombinations;
    }

    /**
     * Returns a list of {@link InfraCombination} to be included.
     *
     * @return {@link InfraCombination} to be included
     */
    public List<InfraCombination> getIncludeInfraCombinations() {
        return includeInfraCombinations;
    }

    /**
     * Sets a list of {@link InfraCombination} to be included.
     *
     * @param includeInfraCombinations {@link InfraCombination} to be included
     */
    public void setIncludeInfraCombinations(List<InfraCombination> includeInfraCombinations) {
        this.includeInfraCombinations = includeInfraCombinations;
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
     * Returns the associated {@link Infrastructure}s to this infra configuration.
     *
     * @return associated {@link Infrastructure}s to this infra configuration
     */
    public List<Infrastructure> getInfrastructures() {
        List<Infrastructure> infrastructures = new ArrayList<>();

        // 1st priority for include infra combinations
        for (InfraCombination infraCombination : includeInfraCombinations) {
            Infrastructure infrastructure = createInfrastructure(infraCombination);
            infrastructures.add(infrastructure);
        }

        // Considering infra combinations
        for (OperatingSystem operatingSystem : operatingSystems) {
            for (Database database : databases) {
                for (InfraCombination.JDK jdk : jdks) {
                    InfraCombination infraCombination = createInfraCombination(operatingSystem, database, jdk);

                    // If the infra combination is in the exclude list then ignore
                    // If the infra combination is in the include list then ignore because it is already considered
                    if (excludeInfraCombinations.contains(infraCombination) ||
                        includeInfraCombinations.contains(infraCombination)) {
                        continue;
                    }
                    Infrastructure infrastructure = createInfrastructure(infraCombination);
                    infrastructures.add(infrastructure);
                }
            }
        }
        return infrastructures;
    }

    /**
     * Creates and returns an instance of {@link InfraCombination} for the given params.
     *
     * @param operatingSystem {@link OperatingSystem} associated
     * @param database        {@link Database} associated
     * @param jdk             {@link org.wso2.testgrid.common.InfraCombination.JDK} associated
     * @return an instance of {@link InfraCombination} for the given params
     */
    private InfraCombination createInfraCombination(OperatingSystem operatingSystem, Database database,
                                                    InfraCombination.JDK jdk) {
        InfraCombination infraCombination = new InfraCombination();
        infraCombination.setOperatingSystem(operatingSystem);
        infraCombination.setDatabase(database);
        infraCombination.setJdk(jdk);
        return infraCombination;
    }

    /**
     * Creates an instance of {@link Infrastructure} for the given {@link InfraCombination}.
     *
     * @param infraCombination {@link InfraCombination} to create the {@link Infrastructure}
     * @return instance of {@link Infrastructure} for the given {@link InfraCombination}
     */
    private Infrastructure createInfrastructure(InfraCombination infraCombination) {
        // Parse script parameters
        List<Script> parsedScripts = parseScriptParameters(scripts, infraCombination);

        // Create infrastructure
        Infrastructure infrastructure = new Infrastructure();
        infrastructure.setName(name);
        infrastructure.setProviderType(providerType);
        infrastructure.setInstanceType(instanceType);
        infrastructure.setClusterType(clusterType);
        infrastructure.setInfraCombination(infraCombination);
        infrastructure.setSecurityProperties(securityProperties);
        infrastructure.setScripts(parsedScripts);
        infrastructure.setRegion(region);
        infrastructure.setImageId(imageId);

        return infrastructure;
    }

    /**
     * Sets the appropriate value for script parameters, if the script parameter is a place holder.
     *
     * @param scripts          list of {@link Script} instances to parse
     * @param infraCombination infrastructure values to be considered for replacing with the placeholder
     * @return parsed list of script instances {@link Script}
     */
    private List<Script> parseScriptParameters(List<Script> scripts, InfraCombination infraCombination) {
        List<Script> parsedScripts = new ArrayList<>();
        for (Script script : scripts) {
            // Make a copy of script - do not alter the original instance
            Script parsedScript = SerializationUtils.clone(script);
            Properties parsedScriptProperties = new Properties();
            Properties scriptProperties = parsedScript.getScriptParameters();
            for (String scriptPropertyKey : scriptProperties.stringPropertyNames()) {
                parsedScriptProperties.put(scriptPropertyKey,
                        parseValue(scriptProperties.getProperty(scriptPropertyKey), infraCombination));
            }
            parsedScript.setScriptParameters(parsedScriptProperties);
            parsedScripts.add(parsedScript);
        }
        return parsedScripts;
    }

    /**
     * Sets the appropriate value for a config element, if the config element is a place holder.
     * <p>
     * If the config element value is one of {@value PLACEHOLDER_OPERATING_SYSTEM_NAME},
     * {@value PLACEHOLDER_OPERATING_SYSTEM_VERSION}, {@value PLACEHOLDER_DATABASE_ENGINE},
     * {@value PLACEHOLDER_DATABASE_VERSION}, {@value PLACEHOLDER_JDK}
     * <p>
     * then the appropriate value for the placeholder will be returned.
     * <p>
     * Please note that the case of the string is not considered.
     *
     * @param value            config element value to parse
     * @param infraCombination infrastructure values to be considered for replacing with the placeholder
     * @return config value it self or the appropriate value for the place holder
     */
    private String parseValue(String value, InfraCombination infraCombination) {
        switch (value.toLowerCase(Locale.ENGLISH)) {
            case PLACEHOLDER_OPERATING_SYSTEM_NAME:
                return infraCombination.getOperatingSystem().getName();
            case PLACEHOLDER_OPERATING_SYSTEM_VERSION:
                return infraCombination.getOperatingSystem().getVersion();
            case PLACEHOLDER_DATABASE_ENGINE:
                return infraCombination.getDatabase().getEngine().toString();
            case PLACEHOLDER_DATABASE_VERSION:
                return infraCombination.getDatabase().getVersion();
            case PLACEHOLDER_JDK:
                return infraCombination.getJdk().toString();
            default:
                return value;
        }
    }
}
