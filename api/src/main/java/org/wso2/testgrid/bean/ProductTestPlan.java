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

package org.wso2.testgrid.bean;

/**
 * Bean class of ProductTestPlan object used in APIs.
 */
public class ProductTestPlan {
    private String id;
    private String startTimestamp;
    private String endTimestamp;
    private String status;
    private String productName;
    private String productVersion;
    private String reportLocation;

    /**
     * Returns the id of the product test plan.
     *
     * @return product test plan id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the product test plan.
     *
     * @param id product test plan id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the start timestamp of the product test plan.
     *
     * @return product test plan timestamp
     */
    public String getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Sets the start timestamp of the product test plan.
     *
     * @param startTimestamp product test plan start timestamp
     */
    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    /**
     * Returns the end timestamp of the product test plan.
     *
     * @return product test plan end timestamp
     */
    public String getEndTimestamp() {
        return endTimestamp;
    }

    /**
     * Sets the end timestamp of the product test plan.
     *
     * @param endTimestamp product test plan end timestamp
     */
    public void setEndTimestamp(String endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    /**
     * Returns the status of the product test plan.
     *
     * @return product test plan status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the product test plan.
     *
     * @param status product test plan status
     */
    public void setStatus(String status) {
        this.status = status;
    }

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
     * Returns the report location of the product test plan.
     *
     * @return product test plan report location
     */
    public String getReportLocation() {
        return reportLocation;
    }

    /**
     * Sets the report location of the product test plan.
     *
     * @param reportLocation product test plan report location
     */
    public void setReportLocation(String reportLocation) {
        this.reportLocation = reportLocation;
    }
}
