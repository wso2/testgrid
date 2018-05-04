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
    private TestPlan lastBuild;
    private TestPlan lastfailed;
    private String executeLink;
    private String configLink;

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
     * Returns the last build TestPlan of the product
     *
     * @return {@link TestPlan} for the last build
     */
    public TestPlan getLastBuild() {
        return lastBuild;
    }

    /**
     *
     * Sets the last build for the product.
     *
     * @param lastBuild the last build {@link TestPlan}
     */
    public void setLastBuild(TestPlan lastBuild) {
        this.lastBuild = lastBuild;
    }

    /**
     *
     * Returns the last failed TestPlan of the product.
     *
     * @return {@link TestPlan} for last failed build
     */
    public TestPlan getLastfailed() {
        return lastfailed;
    }

    /**
     *Sets the last failed build for the product
     *
     * @param lastfailed {@link TestPlan} for last failed build
     */
    public void setLastfailed(TestPlan lastfailed) {
        this.lastfailed = lastfailed;
    }

    /**
     *Returns the executable link for the product that contains all the jobs.
     *
     * @return executable URL for the product
     */
    public String getExecuteLink() {
        return executeLink;
    }

    /**
     *Set the executable link for the product.
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
     *Set the Jenkins configuration link for the product.
     *
     * @param configLink URL for configuration console
     */
    public void setConfigLink(String configLink) {
        this.configLink = configLink;
    }
}
