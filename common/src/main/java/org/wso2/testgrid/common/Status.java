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

/**
 * This defines the possible statuses of an entity.
 *
 * @since 1.0.0
 */
public enum Status {

    /**
     * Entity execution is success.
     */
    SUCCESS("SUCCESS"),

    /**
     * Entity execution is failed.
     */
    FAIL("FAIL"),

    /**
     * Entity execution is pending.
     */
    PENDING("PENDING"),

    /**
     * Entity execution is running.
     */
    RUNNING("RUNNING"),

    /**
     * Entity execution didn't run.
     */
    DID_NOT_RUN("DID_NOT_RUN"),

    /**
     * Entity execution caused an error.
     */
    ERROR("ERROR"),

    /**
     * Entity execution is incomplete.
     */
    INCOMPLETE("INCOMPLETE"),

    /**
     * Entity execution is skipped.
     */
    SKIP("SKIP");

    private final String status;

    /**
     * Sets the status of the entity.
     *
     * @param status entity status
     */
    Status(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.status;
    }
}
