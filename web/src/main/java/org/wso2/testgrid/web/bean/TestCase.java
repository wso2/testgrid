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

import org.wso2.testgrid.common.Status;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Bean class of TestCase object used in APIs.
 */
public class TestCase {

    private String id;
    private String name;
    private String errorMsg;
    private Timestamp createdTimestamp;
    private Timestamp modifiedTimestamp;
    private Status status;

    /**
     * Returns the id of the test-case.
     *
     * @return test-case id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the test-case.
     *
     * @param id test-case id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the test-case.
     *
     * @return test-case name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the test-case.
     *
     * @param name test-case name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the start timestamp of the test-case.
     *
     * @return test-case start timestamp
     */
    public Timestamp getCreatedTimestamp() {
        if (createdTimestamp != null) {
            return new Timestamp(createdTimestamp.getTime());
        }
        return new Timestamp(new Date().getTime());
    }

    /**
     * Sets the start timestamp of the test-case.
     *
     * @param createdTimestamp test-case start timestamp
     */
    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = new Timestamp(createdTimestamp.getTime());
    }

    /**
     * Returns the modified timestamp of the test-case.
     *
     * @return modified test-case timestamp
     */
    public Timestamp getModifiedTimestamp() {
        if (modifiedTimestamp != null) {
            return new Timestamp(modifiedTimestamp.getTime());
        }
        return new Timestamp(new Date().getTime());
    }

    /**
     * Sets the modified timestamp of the test-case.
     *
     * @param modifiedTimestamp modified test-case timestamp
     */
    public void setModifiedTimestamp(Timestamp modifiedTimestamp) {
        this.modifiedTimestamp = new Timestamp(modifiedTimestamp.getTime());
    }

    /**
     * Returns the status of the test-case.
     *
     * @return {@code true} if the test is success, {@code false} otherwise
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the test-case.
     *
     * @param status test-case getStatus
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the error message if there's any.
     *
     * @return error message
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Sets the error message (if available) in the test-case.
     *
     * @param errorMsg error message
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
