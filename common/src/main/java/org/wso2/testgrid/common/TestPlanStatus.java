/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * This defines the possible statuses of a Test-Plan.
 */
public enum TestPlanStatus {

    /**
     * Entity execution is success.
     * The testplan should be executed completely and all the tests should be passed.
     */
    SUCCESS("SUCCESS"),

    /**
     * Entity execution is failed.
     * The testplan should be executed completely and at least one test should be failed.
     */
    FAIL("FAIL"),

    /**
     * Entity execution caused an error.
     * The testplan should be not executed completely.
     */
    ERROR("ERROR"),

    /**
     * Entity execution is running.
     * The testplan is currently being executed.
     */
    RUNNING("RUNNING"),

    @Deprecated //Only using till the old records in the db are migrated to new statuses
    DID_NOT_RUN("DID_NOT_RUN"),

    @Deprecated //Only using till the old records in the db are migrated to new statuses
    INCOMPLETE("INCOMPLETE");

    private final String testPlanStatus;

    /**
     * Sets the status of the entity.
     *
     * @param testPlanStatus entity status
     */
    TestPlanStatus(String testPlanStatus) {
        this.testPlanStatus = testPlanStatus;
    }

    @Override
    public String toString() {
        return this.testPlanStatus;
    }
}
