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

import java.util.ArrayList;
import java.util.List;

/**
 * Bean class of Product with test status object used in APIs.
 */
public class ProductTestStatus {
    private String id;
    private String name;
    private String version;
    private String channel;
    private List<TestStatus> testStatuses;

    public ProductTestStatus(String key) {
        String properties[] = key.split("_");
        this.id = properties[0];
        this.name = properties[1];
        this.version = properties[2];
        this.channel = properties[3];
        this.testStatuses = new ArrayList<>();
    }

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
     * Returns the name of the product.
     *
     * @return name of the product
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the product.
     *
     * @param name product-name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the version of the product.
     *
     * @return version of the product
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the product.
     *
     * @param version product-version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the channel of the product.
     *
     * @return channel of the product
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the channel of the product.
     *
     * @param channel product-channel
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Returns the testStatuses of the product test runs.
     *
     * @return {@link TestStatus} statues of product tests runs
     */
    public List<TestStatus> getTestStatuses() {
        return testStatuses;
    }

    /**
     * Sets the statues of the product test runs.
     *
     * @param testStatuses {@link TestStatus} product test testStatuses
     */
    public void setTestStatuses(List<TestStatus> testStatuses) {
        this.testStatuses = testStatuses;
    }
}
