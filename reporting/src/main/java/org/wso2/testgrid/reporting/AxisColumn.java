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
package org.wso2.testgrid.reporting;

/**
 * Enum class to define the axis columns.
 *
 * @since 1.0.0
 */
public enum AxisColumn {

    /**
     * Deployment column
     */
    DEPLOYMENT("DEPLOYMENT"),

    /**
     * Infrastructure column
     */
    INFRASTRUCTURE("INFRASTRUCTURE"),

    /**
     * Scenario column
     */
    SCENARIO("SCENARIO");

    private final String axisColumn;

    /**
     * Sets axis column.
     *
     * @param axisColumn axis column
     */
    AxisColumn(String axisColumn) {
        this.axisColumn = axisColumn;
    }

    @Override
    public String toString() {
        return this.axisColumn;
    }
}
