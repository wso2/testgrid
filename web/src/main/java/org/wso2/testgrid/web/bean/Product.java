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

import java.sql.Timestamp;

/**
 * Bean class of Product object used in APIs.
 */
public class Product {
    private String id;
    private String name;
    private Timestamp lastSuccessTimestamp;
    private Timestamp lastFailureTimestamp;

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
     * Returns the product name of the product test plan.
     *
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product name of the product test plan.
     *
     * @param name product test plan product name
     */
    public void setName(String name) {
        this.name = name;
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
