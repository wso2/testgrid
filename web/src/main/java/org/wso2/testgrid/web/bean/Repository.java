/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.List;

/**
 * Bean class of Repository object used in TestPlanRequests.
 */
public class Repository {
    private String repository;
    private List<String> inputs;

    /**
     * Returns the repository URL.
     *
     * @return repository URL.
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Sets the repository URL
     *
     * @param repository URL of the repository.
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Return the list of inputs relates to the repository.
     *
     * @return the list of inputs relates to the repository.
     */
    public List<String> getInputs() {
        return inputs;
    }

    /**
     * Sets the list of inputs relates to the repository.
     *
     * @param inputs list of inputs related to the repository.
     */
    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }
}
