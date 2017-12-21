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
package org.wso2.testgrid.core.config;

import org.wso2.carbon.config.annotation.Element;

import java.util.List;

/**
 * This class is used to retrieve scenario configuration values.
 *
 * @since 1.0.0
 */
public class ScenarioConfig {

    @Element(required = true, description = "Defines the scenario git repository.")
    private String gitRepo;

    @Element(description = "Defines the scenario name list to execute.")
    private List<String> names;

    /**
     * Returns the git repository for the scenario.
     *
     * @return scenario git repository
     */
    public String getGitRepo() {
        return gitRepo;
    }

    /**
     * Sets the git repository for the scenario.
     *
     * @param gitRepo scenario git repository
     */
    public void setGitRepo(String gitRepo) {
        this.gitRepo = gitRepo;
    }

    /**
     * Returns the list of scenarios to be executed.
     * <p>
     * If the list of scenarios are not provided, all the scenarios in the repository will be executed.
     *
     * @return list of scenarios to be executed
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * Sets list of deployments to be executed.
     * <p>
     * If the list of scenarios are not provided, all the scenarios in the repository will be executed.
     *
     * @param names list of scenarios to be executed
     */
    public void setNames(List<String> names) {
        this.names = names;
    }
}
