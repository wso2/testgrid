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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.web.bean;

import java.sql.Timestamp;

/**
 * Bean class for Aggregated product productStatus reponses.
 *
 * @since 1.0.0
 */
public class ProductStatus {

    private String productId;
    private String productName;
    private String reportLink;
    private String productStatus;
    private String executeLink;
    private String configLink;
    private Timestamp lastSuccessTimestamp;
    private Timestamp lastFailureTimestamp;

    /**
     * Returns the productId of the product.
     *
     * @return productId of the product
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the productId of the product.
     *
     * @param productId product-productId
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the productName of product.
     *
     * @return product productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the productName of product.
     *
     * @param productName productName to set to product
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     *
     * Returns the Reportlink for the product.
     *
     * @return the report link
     */
    public String getReportLink() {
        return reportLink;
    }

    /**
     * Sets the report link for a product.
     *
     * @param reportLink report link to set
     */
    public void setReportLink(String reportLink) {
        this.reportLink = reportLink;
    }

    /**
     *
     * Returns the productStatus of product.
     *
     * @return the productStatus of product
     */
    public String getProductStatus() {
        return productStatus;
    }

    /**
     * Sets the productStatus of the product.
     *
     * @param productStatus the productStatus of product
     */
    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    /**
     * Returns the executable link for the product that contains all the jobs.
     *
     * @return executable URL for the product
     */
    public String getExecuteLink() {
        return executeLink;
    }

    /**
     * Set the executable link for the product.
     *
     * @param executeLink URL for the job execution of product
     */
    public void setExecuteLink(String executeLink) {
        this.executeLink = executeLink;
    }

    /**
     * Returns the Configuration link for the product jobs in Jenkins
     *
     * @return the URL for configuration console
     */
    public String getConfigLink() {
        return configLink;
    }

    /**
     * Set the Jenkins configuration link for the product.
     *
     * @param configLink URL for configuration console
     */
    public void setConfigLink(String configLink) {
        this.configLink = configLink;
    }

    /**
     * Returns the last success timestamp of the product build.
     *
     * @return timestamp
     */
    public Timestamp getLastSuccessTimestamp() {
        return lastSuccessTimestamp == null ? null : new Timestamp(lastSuccessTimestamp.getTime());
    }

    /**
     * Sets the last success timestamp of the product build.
     *
     * @param lastSuccessTimestamp timestamp
     */
    public void setLastSuccessTimestamp(Timestamp lastSuccessTimestamp) {
        this.lastSuccessTimestamp = lastSuccessTimestamp == null ? null : new Timestamp(lastSuccessTimestamp.getTime());
    }

    /**
     * Returns the last failure timestamp of the product build.
     *
     * @return timestamp
     */
    public Timestamp getLastFailureTimestamp() {
        return lastFailureTimestamp == null ? null : new Timestamp(lastFailureTimestamp.getTime());
    }

    /**
     * Sets the last failure timestamp of the product build.
     *
     * @param lastFailureTimestamp timestamp
     */
    public void setLastFailureTimestamp(Timestamp lastFailureTimestamp) {
        this.lastFailureTimestamp = lastFailureTimestamp == null ? null : new Timestamp(lastFailureTimestamp.getTime());
    }
}
