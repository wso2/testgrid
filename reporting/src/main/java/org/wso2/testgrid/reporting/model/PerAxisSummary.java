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
package org.wso2.testgrid.reporting.model;

/**
 * Bean class to maintain per infra summary.
 *
 * @since 1.0.0
 */
public class PerAxisSummary {

    private final String axis1Value;
    private final String axis2Value;
    private final boolean isSuccess;

    /**
     * Constructs an instance of {@link PerAxisSummary} for the given parameters.
     *
     * @param axis1Value value for axis 1
     * @param axis2Value value for axis 2
     * @param isSuccess  is test success
     */
    public PerAxisSummary(String axis1Value, String axis2Value, boolean isSuccess) {
        this.axis1Value = axis1Value;
        this.axis2Value = axis2Value;
        this.isSuccess = isSuccess;
    }

    /**
     * Returns the value of axis 1.
     *
     * @return value of axis 1
     */
    public String getAxis1Value() {
        return axis1Value;
    }

    /**
     * Returns the value of axis 2.
     *
     * @return value of axis 2
     */
    public String getAxis2Value() {
        return axis2Value;
    }

    /**
     * Returns whether this combination is successful.
     *
     * @return {@code true} if the combination is successful, {@code false} otherwise
     */
    public boolean isSuccess() {
        return isSuccess;
    }
}
