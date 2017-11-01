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

package org.wso2.carbon.testgrid.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * This defines a model object for a Test Plan in a test configuration for a particular product.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPlan {

    private String name;
    private boolean enabled;
    private String deploymentPattern;
    private String infrastructureType;
    private String clusterType;
    private String deployerType;
    private String instanceType;
    private String os;
    private String databaseEngine;
    private String testRepository;
    private String deploymentRepository;
    private String productName;
    private String productVersion;

    private List<SolutionPattern> solutionPatterns;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDeploymentPattern() {
        return deploymentPattern;
    }

    public void setDeploymentPattern(String deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    public String getInfrastructureType() {
        return infrastructureType;
    }

    public void setInfrastructureType(String infrastructureType) {
        this.infrastructureType = infrastructureType;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String getDeployerType() {
        return deployerType;
    }

    public void setDeployerType(String deployerType) {
        this.deployerType = deployerType;
    }

    public List<SolutionPattern> getSolutionPatterns() {
        return solutionPatterns;
    }

    public void setSolutionPatterns(List<SolutionPattern> solutionPatterns) {
        this.solutionPatterns = solutionPatterns;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getDatabaseEngine() {
        return databaseEngine;
    }

    public void setDatabaseEngine(String databaseEngine) {
        this.databaseEngine = databaseEngine;
    }

    public String getTestRepository() {
        return testRepository;
    }

    public void setTestRepository(String testRepository) {
        this.testRepository = testRepository;
    }

    public String getDeploymentRepository() {
        return deploymentRepository;
    }

    public void setDeploymentRepository(String deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }
}
