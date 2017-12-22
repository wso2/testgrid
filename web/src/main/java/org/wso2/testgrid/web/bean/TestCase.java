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

/**
 * Bean class of TestCase object used in APIs.
 */
public class TestCase {

    private String id;
    private String name;
    private String errorMsg;
    private long createdTimestamp;
    private long modifiedTimestamp;
    private boolean isSuccess;

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
    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Sets the start timestamp of the test-case.
     *
     * @param createdTimestamp test-case start timestamp
     */
    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /**
     * Returns the modified timestamp of the test-case.
     *
     * @return modified test-case timestamp
     */
    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    /**
     * Sets the modified timestamp of the test-case.
     *
     * @param modifiedTimestamp modified test-case timestamp
     */
    public void setModifiedTimestamp(long modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    /**
     * Returns the isSuccess of the test-case.
     *
     * @return {@code true} if the test is success, {@code false} otherwise
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Sets the isSuccess of the test-case.
     *
     * @param success test-case isSuccess
     */
    public void setSuccess(boolean success) {
        this.isSuccess = success;
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
