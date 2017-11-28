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

package org.wso2.testgrid.common;

import org.wso2.testgrid.common.util.EnvironmentUtil;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * This represents a model of the ProductTestPlan which includes all the necessary data to run the Test plans created
 * for a particular product. All the test-configs will be mapped to a TestPlan or list of TestPlans based on the
 * configured infrastructure, cluster types etc.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = ProductTestPlan.PRODUCT_TEST_PLAN_TABLE)
public class ProductTestPlan extends AbstractUUIDEntity implements Serializable {

    /**
     * Product test plan table name.
     */
    public static final String PRODUCT_TEST_PLAN_TABLE = "product_test_plan";

    /**
     * Column names of the table.
     */
    public static final String PRODUCT_NAME_COLUMN = "product_name";
    public static final String PRODUCT_VERSION_COLUMN = "product_version";
    public static final String START_TIMESTAMP_COLUMN = "start_timestamp";
    public static final String MODIFIED_TIMESTAMP_COLUMN = "modified_timestamp";
    public static final String STATUS_COLUMN = "status";
    public static final String INFRA_REPOSITORY_COLUMN = "infra_repository";
    public static final String DEPLOYMENT_REPOSITORY_COLUMN = "deployment_repository";
    public static final String SCENARIO_REPOSITORY_COLUMN = "scenario_repository";

    private static final long serialVersionUID = 5812347338918334430L;

    @Column(name = PRODUCT_NAME_COLUMN, nullable = false, length = 50)
    private String productName;

    @Column(name = PRODUCT_VERSION_COLUMN, nullable = false, length = 20)
    private String productVersion;

    @Column(name = START_TIMESTAMP_COLUMN, nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp startTimestamp;

    @Column(name = MODIFIED_TIMESTAMP_COLUMN, nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp modifiedTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = STATUS_COLUMN, nullable = false)
    private Status status;

    @Column(name = INFRA_REPOSITORY_COLUMN)
    private String infraRepository;

    @Column(name = DEPLOYMENT_REPOSITORY_COLUMN)
    private String deploymentRepository;

    @Column(name = SCENARIO_REPOSITORY_COLUMN)
    private String scenarioRepository;

    @Transient
    private String homeDir;

    @Transient
    private ConcurrentHashMap<String, Infrastructure> infrastructureMap = new ConcurrentHashMap<>();

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
     * Returns the start timestamp of the product test plan.
     *
     * @return start timestamp of the product test plan
     */
    public Timestamp getStartTimestamp() {
        return new Timestamp(startTimestamp.getTime());
    }

    /**
     * Sets the start timestamp of the product test plan.
     *
     * @param startTimestamp start timestamp of the product test plan
     */
    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = new Timestamp(startTimestamp.getTime());
    }

    /**
     * Returns the modified timestamp of the product test plan.
     *
     * @return modified timestamp of the product test plan
     */
    public Timestamp getModifiedTimestamp() {
        return new Timestamp(modifiedTimestamp.getTime());
    }

    /**
     * Sets the modified timestamp of the product test plan.
     *
     * @param modifiedTimestamp modified timestamp of the product test plan
     */
    public void setModifiedTimestamp(Timestamp modifiedTimestamp) {
        this.modifiedTimestamp = new Timestamp(modifiedTimestamp.getTime());
    }

    /**
     * Returns the status of the product test plan.
     *
     * @return status of the product test plan
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the product test plan.
     *
     * @param status status of the product test plan
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the infrastructure repository for the product test plan.
     *
     * @return infrastructure repository for the product test plan
     */
    public String getInfraRepository() {
        return infraRepository;
    }

    /**
     * Sets the infrastructure repository for the product test plan.
     *
     * @param infraRepository infrastructure repository for the product test plan
     */
    public void setInfraRepository(String infraRepository) {
        this.infraRepository = infraRepository;
    }

    /**
     * Returns the deployment repository of the product test plan.
     *
     * @return deployment repository of the product test plan
     */
    public String getDeploymentRepository() {
        return deploymentRepository;
    }

    /**
     * Sets the deployment repository of the product test plan.
     *
     * @param deploymentRepository deployment repository of the product test plan
     */
    public void setDeploymentRepository(String deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    /**
     * Returns the scenario repository for the product test plan.
     *
     * @return scenario repository for the product test plan
     */
    public String getScenarioRepository() {
        return scenarioRepository;
    }

    /**
     * Sets the scenario repository for the product test plan.
     *
     * @param scenarioRepository scenario repository for the product test plan
     */
    public void setScenarioRepository(String scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    /**
     * Returns the home directory of the product test plan.
     *
     * @return home directory of the product test plan
     */
    public String getHomeDir() {
        return EnvironmentUtil.getSystemVariableValue("TESTGRID_HOME");
    }

    /**
     * Sets the home directory of the product test plan.
     *
     * @param homeDir home directory of the product test plan
     */
    @Deprecated
    public void setHomeDir(String homeDir) {
        // No operation
    }

    /**
     * Returns the infrastructure map for the product test plan.
     *
     * @return infrastructure map for the product test plan
     */
    public ConcurrentHashMap<String, Infrastructure> getInfrastructureMap() {
        return infrastructureMap;
    }

    /**
     * Sets the infrastructure map for the product test plan.
     *
     * @param infrastructureMap infrastructure map for the product test plan
     */
    public void setInfrastructureMap(ConcurrentHashMap<String, Infrastructure> infrastructureMap) {
        this.infrastructureMap = infrastructureMap;
    }

    /**
     * Returns the infrastructure for the given infrastructure name.
     *
     * @param name infrastructure name
     * @return infrastructure for the given infrastructure name
     */
    public Infrastructure getInfrastructure(String name) {
        return this.infrastructureMap.get(name);
    }

    /**
     * Adds an infrastructure for the product test plan.
     *
     * @param infrastructure infrastructure to be added to the product test plan
     */
    public void addInfrastructure(Infrastructure infrastructure) {
        this.infrastructureMap.put(infrastructure.getName(), infrastructure);
    }

    /**
     * This defines the possible statuses of the ProductTestPlan.
     *
     * @since 1.0.0
     */
    public enum Status {

        /**
         * Planned to execute the ProductTestPlan.
         */
        PRODUCT_TEST_PLAN_PENDING("PRODUCT_TEST_PLAN_PENDING"),

        /**
         * Executing the ProductTestPlan.
         */
        PRODUCT_TEST_PLAN_RUNNING("PRODUCT_TEST_PLAN_RUNNING"),

        /**
         * Generating the test-report of the ProductTestPlan.
         */
        PRODUCT_TEST_PLAN_REPORT_GENERATION("PRODUCT_TEST_PLAN_REPORT_GENERATION"),

        /**
         * Execution completed.
         */
        PRODUCT_TEST_PLAN_COMPLETED("PRODUCT_TEST_PLAN_COMPLETED");

        private final String status;

        /**
         * Sets the status of the product test plan.
         *
         * @param status product test plan status
         */
        Status(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return this.status;
        }
    }
}
