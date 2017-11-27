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

package org.wso2.testgrid.bean;

/**
 * Bean class of TestScenario object used in APIs.
 */
public class TestScenario {

    private String id;
    private String status;
    private String name;

    /**
     * Returns the id of the test scenario.
     *
     * @return test scenario id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the test scenario id.
     *
     * @param id test scenario id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the status of the test scenario.
     *
     * @return test scenario status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the test scenario status.
     *
     * @param status test scenario status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the name of the test scenario.
     *
     * @return test scenario name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the test scenario name.
     *
     * @param name test scenario name
     */
    public void setName(String name) {
        this.name = name;
    }
}
