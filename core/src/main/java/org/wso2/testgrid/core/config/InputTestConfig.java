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
package org.wso2.testgrid.core.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to retrieve test configuration values given by the user.
 *
 * @since 1.0.0
 */
@Configuration(namespace = "wso2.testgrid.test", description = "TestGrid Test Configuration Parameters")
public class InputTestConfig {

    @Element(description = "Defines the product config.")
    private ProductConfig product;

    @Element(required = true, description = "Defines the deployment pattern.")
    private DeploymentPatternConfig deploymentPattern;

    @Element(required = true, description = "Defines the infrastructure parameters.")
    private List<Map<String, Object>> infraParams = new ArrayList<>();

    @Element(required = true, description = "Defines the scenario.")
    private ScenarioConfig scenario;

    /**
     * Returns the product config.
     *
     * @return product config
     */
    public ProductConfig getProduct() {
        return product;
    }

    /**
     * Sets the product config.
     *
     * @param product product config
     */
    public void setProduct(ProductConfig product) {
        this.product = product;
    }

    /**
     * Returns the deployment configuration.
     *
     * @return deployment configuration
     */
    public DeploymentPatternConfig getDeploymentPatternConfig() {
        return deploymentPattern;
    }

    /**
     * Sets the deployment configuration.
     *
     * @param deploymentPattern deployment configuration
     */
    public void setDeploymentPatternConfig(DeploymentPatternConfig deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    /**
     * Returns the infrastructure configuration.
     *
     * @return infrastructure configuration
     */
    public List<Map<String, Object>> getInfraParams() {
        return infraParams;
    }

    /**
     * Sets the infrastructure configuration.
     *
     * @param infraParams infrastructure configuration
     */
    public void setInfraParams(List<Map<String, Object>> infraParams) {
        this.infraParams = infraParams;
    }

    /**
     * Returns the scenario configuration.
     *
     * @return scenario configuration
     */
    public ScenarioConfig getScenarioConfig() {
        return scenario;
    }

    /**
     * Sets the scenario configuration.
     *
     * @param scenario scenario configuration
     */
    public void setScenarioConfig(ScenarioConfig scenario) {
        this.scenario = scenario;
    }
}
