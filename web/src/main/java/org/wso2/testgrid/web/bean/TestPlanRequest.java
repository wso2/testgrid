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
 */

package org.wso2.testgrid.web.bean;


/**
 * Bean class of TestPlanRequest object used in APIs.
 */
public class TestPlanRequest {
    private String productName;

    /**
     * Three repositories to contain URLs and inputs (command-line arguments) of each infrastructure, deployment
     * and test-scenario repos.
     */
    private Repository infrastructure;
    private Repository deployment;
    private Repository scenarios;

    /**
     * Returns the name of the product.
     *
     * @return product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the name of the product.
     *
     * @param productName name of the product which is testing.
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns the repository object which include infrastructure details.
     *
     * @return repository of infrastructure details.
     */
    public Repository getInfrastructure() {
        return infrastructure;
    }

    /**
     * Sets the infrastructure repository content.
     *
     * @param infrastructure repository of infrastructure details.
     */
    public void setInfrastructure(Repository infrastructure) {
        this.infrastructure = infrastructure;
    }

    /**
     * Returns the repository object which include deployment details.
     *
     * @return repository of deployment details.
     */
    public Repository getDeployment() {
        return deployment;
    }

    /**
     * Sets the deployment repository content.
     *
     * @param deployment repository of deployment details.
     */
    public void setDeployment(Repository deployment) {
        this.deployment = deployment;
    }

    /**
     * Returns the repository object which include test scenario details.
     *
     * @return repository of test scenarios.
     */
    public Repository getScenarios() {
        return scenarios;
    }

    /**
     * Sets the scenario repository content.
     *
     * @param scenarios repository of test scenarios.
     */
    public void setScenarios(Repository scenarios) {
        this.scenarios = scenarios;
    }
}
