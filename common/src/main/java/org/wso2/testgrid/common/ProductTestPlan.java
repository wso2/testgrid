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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
    public static final String PRODUCT_NAME_COLUMN = "productName";
    public static final String PRODUCT_VERSION_COLUMN = "productVersion";
    public static final String START_TIMESTAMP_COLUMN = "startTimestamp";
    public static final String MODIFIED_TIMESTAMP_COLUMN = "modifiedTimestamp";
    public static final String STATUS_COLUMN = "status";
    public static final String CHANNEL_COLUMN = "channel";

    private static final long serialVersionUID = 5812347338918334430L;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "product_version", nullable = false, length = 20)
    private String productVersion;

    @Column(name = "start_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp startTimestamp;

    @Column(name = "modified_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp modifiedTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    @OneToMany(mappedBy = "productTestPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestPlan> testPlans;

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
     * Returns the channel of the product test plan.
     *
     * @return product test plan channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Sets the channel of the product test plan.
     *
     * @param channel product test plan channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Returns the associated test plans list.
     *
     * @return associated test plans list
     */
    public List<TestPlan> getTestPlans() {
        return testPlans;
    }

    /**
     * Sets the associated test plans list.
     *
     * @param testPlans associated test plans list
     */
    public void setTestPlans(List<TestPlan> testPlans) {
        this.testPlans = testPlans;
    }

    @Override
    public String toString() {
        return "ProductTestPlan{" +
               "id='" + this.getId() + '\'' +
               ", productName='" + productName + '\'' +
               ", productVersion='" + productVersion + '\'' +
               ", startTimestamp=" + startTimestamp +
               ", modifiedTimestamp=" + modifiedTimestamp +
               ", status=" + status +
               ", channel=" + channel +
               '}';
    }

    /**
     * This defines the possible channels of the ProductTestPlan.
     *
     * @since 1.0.0
     */
    public enum Channel {

        /**
         * LTS channel.
         */
        LTS("LTS"),

        /**
         * Premium channel.
         */
        PREMIUM("PREMIUM");

        private final String channel;

        /**
         * Sets the channel of the product test plan.
         *
         * @param channel product test plan channel
         */
        Channel(String channel) {
            this.channel = channel;
        }

        @Override
        public String toString() {
            return this.channel;
        }
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
         * Error on executing product test plan.
         */
        PRODUCT_TEST_PLAN_ERROR("PRODUCT_TEST_PLAN_ERROR"),

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
