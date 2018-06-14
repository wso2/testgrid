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

import org.wso2.testgrid.common.Status;

/**
 * Bean class for managing information related to scenario execution.
 *
 * @since 1.0.0
 */
public class ScenarioSummary {

    private final String scenarioDescription;
    private final long totalSuccess;
    private final long totalFail;
    private final double successPercentage;
    private final Status scenarioStatus;
    private final String scenarioDir;

    /**
     * Constructs an instance of a {@link ScenarioSummary}.
     *
     * @param scenarioDescription   scenario description
     * @param totalSuccess   total number of success test cases
     * @param totalFail      total number of failed test cases
     * @param scenarioStatus test scenario overall result
     */
    public ScenarioSummary(String scenarioDescription, long totalSuccess, long totalFail, Status scenarioStatus,
                           String scenarioDir) {
        this.scenarioDescription = scenarioDescription;
        this.totalSuccess = totalSuccess;
        this.totalFail = totalFail;
        this.successPercentage = (double) totalSuccess / ((double) totalSuccess + (double) totalFail) * 100d;
        this.scenarioStatus = scenarioStatus;
        this.scenarioDir = scenarioDir;
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
     * Returns the number of success test cases.
     *
     * @return number of success test cases
     */
    public long getTotalSuccess() {
        return totalSuccess;
    }

    /**
     * Returns the number of failed test cases.
     *
     * @return failed test cases
     */
    public long getTotalFail() {
        return totalFail;
    }

    /**
     * Returns the test success percentage.
     *
     * @return test success percentage
     */
    public double getSuccessPercentage() {
        return successPercentage;
    }

    /**
     * Returns the status of the test scenario.
     *
     * @return status of the test scenario
     */
    public Status getScenarioStatus() {
        return scenarioStatus;
    }

    /**
     * Returns the directory of the test scenario.
     *
     * @return directory of the test scenario
     */
    public String getScenarioDir() {
        return scenarioDir;
    }

    @Override
    public String toString() {
        return "ScenarioSummary{" +
               "scenarioDescription='" + scenarioDescription + '\'' +
               ", totalSuccess='" + totalSuccess + '\'' +
               ", totalFail='" + totalFail + '\'' +
               ", successPercentage='" + successPercentage + "%\'" +
               ", scenarioStatus='" + scenarioStatus + "\'" +
               '}';
    }
}
