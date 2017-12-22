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
 * Bean class of Product object used in APIs.
 */
public class ProductTestSummary {

    private String productName;
    private String productVersion;
    private String endStatus;

    /**
     * Returns the product name of the product test plan.
     *
     * @return product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product name of the product test plan.
     *
     * @param productName product test plan product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns the product version of the product test plan.
     *
     * @return product version
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Sets the product version of the product test plan.
     *
     * @param productVersion product test plan product version
     */
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * Returns the end status of the product test plan.
     *
     * @return product test plan end status
     */
    public String getEndStatus() {
        return endStatus;
    }

    /**
     * Sets the end status of the product test plan.
     *
     * @param endStatus product test plan end status
     */
    public void setEndStatus(String endStatus) {
        this.endStatus = endStatus;
    }
}
