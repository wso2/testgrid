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
package org.wso2.testgrid.core.config;

import org.wso2.carbon.config.annotation.Element;
import org.wso2.testgrid.common.Product;

/**
 * This class is used to retrieve product configuration values.
 *
 * @since 1.0.0
 */
public class ProductConfig {

    @Element(required = true, description = "Name of the product.")
    private String name;

    @Element(required = true, description = "Version of the product.")
    private String version;

    @Element(required = true, description = "Channel of the product.")
    private Product.Channel channel;

    /**
     * Returns the product name.
     *
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product name.
     *
     * @param name product name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the product version.
     *
     * @return product version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the product version.
     *
     * @param version product version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the channel of the product test plan.
     *
     * @return product test plan channel
     */
    public Product.Channel getChannel() {

        return channel;
    }

    /**
     * Sets the channel of the product test plan.
     *
     * @param channel product test plan channel
     */
    public void setChannel(Product.Channel channel) {
        this.channel = channel;
    }
}
