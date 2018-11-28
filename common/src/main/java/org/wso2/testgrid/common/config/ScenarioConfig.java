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


import org.wso2.testgrid.common.ConfigChangeSet;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String dir;
    private String name;
    private Status status;
    private TestPlan testPlan;
    private String outputDir;

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

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public TestPlan getTestPlan() {
        return testPlan;
    }

    public void setTestPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<TestScenario> getScenarios() {
        if (scenarios == null) {
            return new ArrayList<>();
        }
        return scenarios;
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

    private Map<String, String> inputParameters = new HashMap<>();


    private String file;

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

    /**
     * Returns the list of input parameters required for the test script.
     *
     * @return List of inputParameters
     */
    public Map<String, String> getInputParameters() {
        return inputParameters;
    }

    /**
     * Sets the input parameters required to run test script.
     *
     * @param inputParameters List of input parameters taken from scenarioConfig
     */
    public void setInputParameters(Map<String, String> inputParameters) {
        this.inputParameters = inputParameters;
    }

    /**
     * Returns the file name of the test script (e.g. test.sh).
     *
     * @return file name
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the filename of the test script.
     *
     * @param file file name
     */
    public void setFile(String file) {
        this.file = file;
    }

    public String getOutputDir() {
        if (StringUtil.isStringNullOrEmpty(outputDir)) {
            try {
                URL url = new URL(remoteRepository);
                String [] splitbyDash = url.getPath().split("/");
                String [] splitByDot = splitbyDash[splitbyDash.length - 1].split("\\.");
                return splitByDot[0];
            } catch (MalformedURLException e) {
                return null;
            }

        }
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

}

