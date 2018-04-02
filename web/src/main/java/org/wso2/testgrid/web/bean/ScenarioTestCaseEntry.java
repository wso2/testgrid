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
 * Bean class for managing multiple test case entries.
 *
 * @since 1.0.0
 */
public class ScenarioTestCaseEntry {

    private final String scenarioDescription;
    private final List<TestCaseEntry> testCaseEntries;

    /**
     * Constructs an instance of {@link ScenarioTestCaseEntry}
     *
     * @param scenarioDescription    scenario description
     * @param testCaseEntries list of test case entries
     */
    public ScenarioTestCaseEntry(String scenarioDescription, List<TestCaseEntry> testCaseEntries) {
        this.scenarioDescription = scenarioDescription;
        this.testCaseEntries = testCaseEntries;
    }

    /**
     * Returns the scenario description.
     *
     * @return scenario description
     */
    public String getScenarioDescription() {
        return scenarioDescription;
    }

    /**
     * Returns the test case entries list.
     *
     * @return test case entries list
     */
    public List<TestCaseEntry> getTestCaseEntries() {
        return testCaseEntries;
    }
}
