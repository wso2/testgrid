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

import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to capture the information required to generate the test plan view.
 *
 * @since 1.0.0
 */
public class TestPlanView {

    private static final String SCENARIO_TEMPLATE_KEY_NAME = "parsedTestScenarioView";
    private final String testPlanName;
    private final String deploymentPattern;
    private final String deployerType;
    private final String status;
    private final String description;
    private final List<TestScenarioView> testScenarioReports;
    private final String parsedTestScenarioView;

    /**
     * Constructs an instance of a test plan report.
     *
     * @param testPlan            test plan to create the test plan report
     * @param testScenarioReports list of test scenario results
     * @param view                view to be rendered
     * @throws ReportingException thrown when error on rendering view
     */
    public TestPlanView(TestPlan testPlan, List<TestScenarioView> testScenarioReports, String view)
            throws ReportingException {
        this.testPlanName = testPlan.getName();
        this.deploymentPattern = testPlan.getDeploymentPattern();
        this.deployerType = testPlan.getDeployerType().toString();
        this.status = testPlan.getStatus().toString();
        this.description = testPlan.getDescription();
        this.testScenarioReports = Collections.unmodifiableList(testScenarioReports);

        // Render test scenarios
        Map<String, Object> parsedTestScenarios = new HashMap<>();
        parsedTestScenarios.put(SCENARIO_TEMPLATE_KEY_NAME, testScenarioReports);
        Renderable renderable = RenderableFactory.getRenderable(view);
        this.parsedTestScenarioView = renderable.render(view, parsedTestScenarios);
    }

    /**
     * Returns the name of the test plan.
     *
     * @return test plan name
     */
    public String getTestPlanName() {
        return testPlanName;
    }

    /**
     * Returns the deployment pattern for the test plan.
     *
     * @return deployment pattern for the test plan
     */
    public String getDeploymentPattern() {
        return deploymentPattern;
    }

    /**
     * Returns the deployer type for the test plan.
     *
     * @return deployer type for the test plan
     */
    public String getDeployerType() {
        return deployerType;
    }

    /**
     * Returns the status of the test plan.
     *
     * @return infrastructure type of the test plan
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the description of the test plan.
     *
     * @return description of the test plan
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the list of test scenario reports.
     *
     * @return test scenario reports
     */
    public List<TestScenarioView> getTestScenarioReports() {
        return testScenarioReports;
    }

    /**
     * Returns the rendered view string.
     *
     * @return rendered view string
     */
    public String getParsedTestScenarioView() {
        return parsedTestScenarioView;
    }
}
