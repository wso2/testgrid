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

import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to retrieve test configuration values given by the user.
 *
 * @since 1.0.0
 */
public class TestConfig {

    private String productName;
    private String productVersion;
    private Product.Channel channel;
    private List<String> deploymentPatterns;
    private List<Map<String, Object>> infraParams = new ArrayList<>();
    private List<String> scenarios;
    private Infrastructure infrastructure;

    /**
     * Returns the product name.
     *
     * @return product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product name.
     *
     * @param productName product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns the product version.
     *
     * @return product version
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Sets the product version.
     *
     * @param productVersion product version
     */
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * Returns the channel of the product test plan.
     *
     * @return product test plan channel
     */
    public Product.Channel getChannel() {
        return channel;
    }

    /**
     * Sets the channel of the product test plan.
     *
     * @param channel product test plan channel
     */
    public void setChannel(Product.Channel channel) {
        this.channel = channel;
    }

    /**
     * Returns the deployment pattern names.
     * <p>
     * If the list of deployment patterns are not provided, all the deployment patterns in the repository will be
     * executed.
     *
     * @return deployment pattern names
     */
    public List<String> getDeploymentPatterns() {
        return deploymentPatterns;
    }

    /**
     * Sets the deployment pattern names.
     * <p>
     * If the list of deployment patterns are not provided, all the deployment patterns in the repository will be
     * executed.
     *
     * @param deploymentPatterns deployment pattern names
     */
    public void setDeploymentPatterns(List<String> deploymentPatterns) {
        this.deploymentPatterns = deploymentPatterns;
    }

    /**
     * Returns the infrastructure parameters.
     *
     * @return infrastructure parameters
     */
    public List<Map<String, Object>> getInfraParams() {
        return infraParams;
    }

    /**
     * Sets the infrastructure parameters.
     *
     * @param infraParams infrastructure parameters
     */
    public void setInfraParams(List<Map<String, Object>> infraParams) {
        this.infraParams = infraParams;
    }

    /**
     * Returns the list of scenarios to be executed.
     * <p>
     * If the list of scenarios are not provided, all the scenarios in the repository will be executed.
     *
     * @return list of scenarios to be executed
     */
    public List<String> getScenarios() {
        return scenarios;
    }

    /**
     * Sets list of deployments to be executed.
     * <p>
     * If the list of scenarios are not provided, all the scenarios in the repository will be executed.
     *
     * @param scenarios list of scenarios to be executed
     */
    public void setScenarios(List<String> scenarios) {
        this.scenarios = scenarios;
    }

    /**
     * Returns the infrastructure associated with this test configuration.
     *
     * @return infrastructure associated with this test configuration
     */
    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    /**
     * Sets the infrastructure associated with this test configuration.
     *
     * @param infrastructure infrastructure associated with this test configuration
     */
    public void setInfrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }
}
