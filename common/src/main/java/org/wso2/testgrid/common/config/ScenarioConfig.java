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
 *
 */

package org.wso2.testgrid.common.config;


import org.apache.commons.collections4.ListUtils;
import org.wso2.testgrid.common.ConfigChangeSet;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestScenario;
import java.io.Serializable;
import java.util.List;

/**
 * Represent the scenario configuration in the testgrid.yaml file that is
 * denoted by the {@link TestgridYaml}.
 *
 */
public class ScenarioConfig implements Serializable {
    private static final long serialVersionUID = 6295205041044769906L;

    private List<ConfigChangeSet> configChangeSets;
    private List<TestScenario> scenarios;
    private List<Script> scripts;
    //keep default value of test type as functional
    private String testType = TestGridConstants.TEST_TYPE_FUNCTIONAL;
    private String remoteRepository;
    private String remoteBranch = "master";

    /**
     * This method returns the list of scenarios.
     *
     * @return List of test scenarios that need to be run in testgrid.
     */

    public List<ConfigChangeSet> getConfigChangeSets() {
        return configChangeSets;
    }

    public void setConfigChangeSets(List<ConfigChangeSet> configChangeSets) {
        this.configChangeSets = configChangeSets;
    }

    public List<TestScenario> getScenarios() {
        return ListUtils.emptyIfNull(scenarios);
    }

    public void setScenarios(List<TestScenario> scenarios) {
        this.scenarios = scenarios;
    }

    public List<Script> getScripts() {
        return scripts;
    }

    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    /**
     * The git repo URL of the remote git repository.
     * This parameter is only used within Jenkins pipeline (for cloning purpose).
     * It does not have any relevance within the testgrid core.
     *
     * Same applies to {@link #getRemoteBranch()} as well.
     */
    public String getRemoteRepository() {
        return remoteRepository;
    }

    public void setRemoteRepository(String remoteRepository) {
        this.remoteRepository = remoteRepository;
    }

    /**
     * The git branch name of the remote git repository.
     *
     */
    public String getRemoteBranch() {
        return remoteBranch;
    }

    public void setRemoteBranch(String remoteBranch) {
        this.remoteBranch = remoteBranch;
    }
}

