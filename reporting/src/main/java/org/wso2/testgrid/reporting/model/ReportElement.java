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
package org.wso2.testgrid.reporting.model;

import org.wso2.testgrid.reporting.AxisColumn;

/**
 * Bean class to maintain a single report element in a report.
 *
 * @since 1.0.0
 */
public class ReportElement {

    private final boolean isGroupByDeployment;
    private final boolean isGroupByInfrastructure;
    private final boolean isGroupByScenario;
    private String deployment;
    private String infraParams;
    private String scenarioDescription;
    private String testCase;
    private String testCaseFailureMessage;
    private boolean isTestSuccess;

    /**
     * Constructs an instance of {@link ReportElement} for the given parameters.
     *
     * @param axisColumn axis column grouped by
     */
    public ReportElement(AxisColumn axisColumn) {
        isGroupByDeployment = axisColumn.equals(AxisColumn.DEPLOYMENT);
        isGroupByInfrastructure = axisColumn.equals(AxisColumn.INFRASTRUCTURE);
        isGroupByScenario = axisColumn.equals(AxisColumn.SCENARIO);
    }

    /**
     * Returns the name of the deployment.
     *
     * @return deployment name
     */
    public String getDeployment() {
        return deployment;
    }

    /**
     * Sets the name of the deployment.
     *
     * @param deployment deployment name
     */
    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    /**
     * Returns the infra params.
     *
     * @return infra params
     */
    public String getInfraParams() {
        return infraParams;
    }

    /**
     * Sets the infra params.
     *
     * @param infraParams infra params
     */
    public void setInfraParams(String infraParams) {
        this.infraParams = infraParams;
    }

    /**
     * Returns the test scenario description.
     *
     * @return test scenario description
     */
    public String getScenarioDescription() {
        return scenarioDescription;
    }

    /**
     * Sets the test scenario description.
     *
     * @param scenarioDescription test scenario description
     */
    public void setScenarioDescription(String scenarioDescription) {
        this.scenarioDescription = scenarioDescription;
    }

    /**
     * Returns the test case name.
     *
     * @return test case name
     */
    public String getTestCase() {
        return testCase;
    }

    /**
     * Sets the test case name.
     *
     * @param testCase test case name
     */
    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    /**
     * Returns the failure message of the test.
     *
     * @return failure message of the test
     */
    public String getTestCaseFailureMessage() {
        return testCaseFailureMessage;
    }

    /**
     * Sets the failure message of the test
     *
     * @param testCaseFailureMessage failure message of the test
     */
    public void setTestCaseFailureMessage(String testCaseFailureMessage) {
        this.testCaseFailureMessage = testCaseFailureMessage;
    }

    /**
     * Returns whether the test case is successful or not.
     *
     * @return {@code true} if the test case is successful, {@code false} otherwise
     */
    public boolean isTestSuccess() {
        return isTestSuccess;
    }

    /**
     * Sets whether the test case is successful or not.
     *
     * @param testSuccess whether the test case is successful or not.
     */
    public void setTestSuccess(boolean testSuccess) {
        isTestSuccess = testSuccess;
    }


    /**
     * Returns whether the grouping is done by the deployment column.
     *
     * @return {@code true} if grouped by deployment, {@code false} otherwise
     */
    public boolean isGroupByDeployment() {
        return isGroupByDeployment;
    }

    /**
     * Returns whether the grouping is done by the infrastructure column.
     *
     * @return {@code true} if grouped by infrastructure, {@code false} otherwise
     */
    public boolean isGroupByInfrastructure() {
        return isGroupByInfrastructure;
    }

    /**
     * Returns whether the grouping is done by the scenario column.
     *
     * @return {@code true} if grouped by scenario, {@code false} otherwise
     */
    public boolean isGroupByScenario() {
        return isGroupByScenario;
    }
}
