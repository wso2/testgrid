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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.testgrid.web.bean;

/**
 * Bean class for Aggregated product status reponses.
 *
 * @since 1.0.0
 */
public class ProductStatus {

    private String id;
    private String name;
    private String reportLink;
    private String status;
    private TestPlan lastBuild;
    private TestPlan lastfailed;
    private String executeLink;
    private String configLink;

    /**
     * Returns the id of the product.
     *
     * @return id of the product
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the product.
     *
     * @param id product-id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of product.
     *
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of product.
     *
     * @param name name to set to product
     */
    public void setName(String name) {
        this.name = name;
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
     * Returns the status of product.
     *
     * @return the status of product
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the product.
     *
     * @param status the status of product
     */
    public void setStatus(String status) {
        this.status = status;
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
