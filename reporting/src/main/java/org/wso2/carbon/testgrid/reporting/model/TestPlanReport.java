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
package org.wso2.carbon.testgrid.reporting.model;

import org.wso2.carbon.testgrid.reporting.ReportingException;
import org.wso2.carbon.testgrid.reporting.renderer.Renderable;
import org.wso2.carbon.testgrid.reporting.renderer.RenderableFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean class to capture the information required to generate the test plan report.
 *
 * @since 1.0.0
 */
public class TestPlanReport {

    private static final String SCENARIO_TEMPLATE_KEY_NAME = "parsedTestScenarioView";
    private final String testPlanName;
    private final List<TestScenarioReport> testScenarioReports;
    private final String parsedTestScenarioView;

    /**
     * Constructs an instance of a test plan report.
     *
     * @param testPlanName        name of the test plan
     * @param testScenarioReports list of test scenario results
     * @param view                view to be rendered
     * @throws ReportingException thrown when error on rendering view
     */
    public TestPlanReport(String testPlanName, List<TestScenarioReport> testScenarioReports, String view)
            throws ReportingException {
        this.testPlanName = testPlanName;
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
     * Returns the list of test scenario reports.
     *
     * @return test scenario reports
     */
    public List<TestScenarioReport> getTestScenarioReports() {
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
