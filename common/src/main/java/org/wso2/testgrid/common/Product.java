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

import org.wso2.testgrid.common.util.StringUtil;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * This represents a model of the Product which includes the name, version and the channel.
 * All the test-configs will be mapped to a product based on the configuration.
 *
 * A single product will have multiple deployment patterns
 *
 * @since 1.0.0
 */
@Entity
@Table(name = Product.PRODUCT_TABLE)
public class Product extends AbstractUUIDEntity implements Serializable {

    /**
     * Product test plan table name.
     */
    public static final String PRODUCT_TABLE = "product";

    /**
     * Column names of the table.
     */
    public static final String PRODUCT_NAME_COLUMN = "productName";
    public static final String PRODUCT_VERSION_COLUMN = "productVersion";
    public static final String CHANNEL_COLUMN = "channel";

    private static final long serialVersionUID = 5812347338918334430L;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "product_version", nullable = false, length = 20)
    private String productVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeploymentPattern> deploymentPatterns;

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
    public List<DeploymentPattern> getDeploymentPatterns() {
        return deploymentPatterns;
    }

    /**
     * Sets the associated test plans list.
     *
     * @param deploymentPatterns associated test plans list
     */
    public void setDeploymentPatterns(List<DeploymentPattern> deploymentPatterns) {
        this.deploymentPatterns = deploymentPatterns;
    }

    @Override
    public String toString() {
        return StringUtil.concatStrings("Product{",
                "id='", this.getId(), "\'",
                ", productName='", productName, "\'",
                ", productVersion='", productVersion, "\'",
                ", channel='", channel, "\'",
                '}');
    }

    /**
     * This defines the possible channels of the Product.
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
}
