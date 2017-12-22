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

package org.wso2.testgrid.web.bean;

/**
 * Bean class of DeploymentPattern object used in APIs.
 */
public class DeploymentPattern {
    private String id;
    private String name;
    private String description;
    private String productId;
    private String testStatus;

    /**
     * Returns the id of the deployment-pattern.
     *
     * @return id of the deployment-pattern
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the deployment-pattern.
     *
     * @param id deployment-pattern id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the deployment-pattern.
     *
     * @return name of the deployment-pattern
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the deployment-pattern.
     *
     * @param name name of the deployment-pattern
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the associated productId of the deployment-pattern.
     *
     * @return id of the associated product
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the associated productId of the deployment-pattern.
     *
     * @param productId associated productId
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the last test testStatus of the deployment-pattern.
     *
     * @return testStatus final test status of the deployment-pattern
     */
    public String getTestStatus() {
        return testStatus;
    }

    /**
     * Sets the final test testStatus of the deployment-pattern.
     *
     * @param testStatus final test status of the deployment-pattern
     */
    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    /**
     * Returns the description of the deployment-pattern.
     *
     * @return description of the deployment-pattern
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the deployment-pattern.
     *
     * @param description description of the deployment-pattern
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
