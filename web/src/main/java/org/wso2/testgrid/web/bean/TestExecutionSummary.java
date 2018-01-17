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

import java.util.Collections;
import java.util.List;

/**
 * Bean class for managing information related to displaying test execution results.
 *
 * @since 1.0.0
 */
public class TestExecutionSummary {

    private final List<ScenarioSummary> scenarioSummaries;
    private final List<ScenarioTestCaseEntry> scenarioTestCaseEntries;

    /**
     * Constructs an instance of {@link TestExecutionSummary}.
     *
     * @param scenarioSummaries       scenario summary list
     * @param scenarioTestCaseEntries scenario test case entries
     */
    public TestExecutionSummary(List<ScenarioSummary> scenarioSummaries,
                                List<ScenarioTestCaseEntry> scenarioTestCaseEntries) {
        this.scenarioSummaries = Collections.unmodifiableList(scenarioSummaries);
        this.scenarioTestCaseEntries = scenarioTestCaseEntries;
    }

    /**
     * Returns the list of scenario summaries.
     *
     * @return list of scenario summaries
     */
    public List<ScenarioSummary> getScenarioSummaries() {
        return scenarioSummaries;
    }

    /**
     * Returns the scenario test case entries list.
     *
     * @return scenario test case entries list
     */
    public List<ScenarioTestCaseEntry> getScenarioTestCaseEntries() {
        return scenarioTestCaseEntries;
    }

    @Override
    public String toString() {
        return "TestExecutionSummary{" +
               "scenarioSummaries=" + scenarioSummaries +
               "scenarioTestCaseEntries=" + scenarioTestCaseEntries +
               '}';
    }
}
