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
 * Bean class of TestPlan object used in APIs.
 */
public class TestPlan {
    private String id;
    private String name;
    private String deploymentPattern;
    private String description;
    private String startTimestamp;
    private String modifiedTimestamp;
    private String status;
    private String productTestPlanId;
    private String infraResultId;
    private String operatingSystem;
    private String database;
    private String jdk;

    /**
     * Returns the id of the test plan.
     *
     * @return test plan id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the test plan.
     *
     * @param id test plan id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the test plan.
     *
     * @return test plan name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the test plan.
     *
     * @param name test plan name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the deployment pattern of the test plan.
     *
     * @return test plan deployment pattern
     */
    public String getDeploymentPattern() {
        return deploymentPattern;
    }

    /**
     * Sets the deployment pattern of the test plan.
     *
     * @param deploymentPattern test plan deployment pattern
     */
    public void setDeploymentPattern(String deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    /**
     * Returns the description of the test plan.
     *
     * @return test plan description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description pattern of the test plan.
     *
     * @param description test plan description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the start timestamp of the test plan.
     *
     * @return test plan timestamp
     */
    public String getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Sets the start timestamp of the test plan.
     *
     * @param startTimestamp test plan start timestamp
     */
    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    /**
     * Returns the status of the test plan.
     *
     * @return test plan status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the test plan.
     *
     * @param status test plan status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the id of the product test plan.
     *
     * @return product test plan id
     */
    public String getProductTestPlanId() {
        return productTestPlanId;
    }

    /**
     * Sets the id of the product test plan.
     *
     * @param productTestPlanId product test plan id
     */
    public void setProductTestPlanId(String productTestPlanId) {
        this.productTestPlanId = productTestPlanId;
    }

    /**
     * Returns the id of the infra result.
     *
     * @return infra result test plan id
     */
    public String getInfraResultId() {
        return infraResultId;
    }

    /**
     * Sets the id of the infra result.
     *
     * @param infraResultId infra result id
     */
    public void setInfraResultId(String infraResultId) {
        this.infraResultId = infraResultId;
    }

    /**
     * Returns the modified timestamp of the test plan.
     *
     * @return product test plan end timestamp
     */
    public String getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    /**
     * Sets the modified timestamp of the test plan.
     *
     * @param modifiedTimestamp test plan modified timestamp
     */
    public void setModifiedTimestamp(String modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    /**
     * Returns the operating system of the test plan.
     *
     * @return product test plan operating system
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Sets the operating system of the test plan.
     *
     * @param operatingSystem test plan operating system
     */
    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    /**
     * Returns the database of the test plan.
     *
     * @return product test plan database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Sets the database of the test plan.
     *
     * @param database test plan database
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Returns the JDK of the test plan.
     *
     * @return product test plan JDK
     */
    public String getJdk() {
        return jdk;
    }

    /**
     * Sets the jdk of the test plan.
     *
     * @param jdk test plan jdk
     */
    public void setJdk(String jdk) {
        this.jdk = jdk;
    }
}
