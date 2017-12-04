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
package org.wso2.testgrid.dao;

/**
 * Enum class to define sorting order (ascending or descending).
 *
 * @since 1.0.0
 */
public enum SortOrder {

    /**
     * Sort ascending.
     */
    ASCENDING("ASCENDING"),

    /**
     * Sort descending.
     */
    DESCENDING("DESCENDING");

    private final String sortOrder;

    /**
     * Sets the sort order.
     *
     * @param sortOrder sort order (either ascending or descending)
     */
    SortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return this.sortOrder;
    }
}
