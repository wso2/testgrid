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
    private long startTimestamp;
    private long modifiedTimestamp;
    private String status;

    /**
     * Returns the id of the test case.
     *
     * @return test case id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the test case.
     *
     * @param id test case id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the test case.
     *
     * @return test case name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the test case.
     *
     * @param name test case name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the start timestamp of the test case.
     *
     * @return test case start timestamp
     */
    public long getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Sets the start timestamp of the test case.
     *
     * @param startTimestamp test case start timestamp
     */
    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    /**
     * Returns the modified timestamp of the test case.
     *
     * @return modified test case timestamp
     */
    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    /**
     * Sets the modified timestamp of the test case.
     *
     * @param modifiedTimestamp modified test case timestamp
     */
    public void setModifiedTimestamp(long modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    /**
     * Returns the status of the test case.
     *
     * @return test case status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the test case.
     *
     * @param status test case status
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
