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
import org.wso2.carbon.testgrid.reporting.result.TestResultable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean class to capture the information required in test scenarios to generate the report for the test plan.
 *
 * @param <T> the type of the value being TestScenarioReport
 * @since 1.0.0
 */
public class TestScenarioReport<T extends TestResultable> {

    private static final String RESULT_TEMPLATE_KEY_NAME = "parsedTestResultsView";
    private final String testScenarioName;
    private final List<TestResultReport<T>> testResultReports;
    private String parsedTestResultsView;

    /**
     * Constructs an instance of a test scenario report.
     *
     * @param testScenarioName name of the test scenario
     * @param testResults      list of test results for the test scenario
     * @param view             view to be rendered
     * @throws ReportingException thrown when error on rendering view
     */
    public TestScenarioReport(String testScenarioName, List<T> testResults, String view) throws ReportingException {
        this.testScenarioName = testScenarioName;

        List<TestResultReport<T>> testResultReports = new ArrayList<>();
        for (T testResult : testResults) {
            TestResultReport<T> testResultReport = new TestResultReport<>(testResult);
            testResultReports.add(testResultReport);
        }
        this.testResultReports = Collections.unmodifiableList(testResultReports);

        // Render test results
        Map<String, Object> parsedTestResults = new HashMap<>();
        parsedTestResults.put(RESULT_TEMPLATE_KEY_NAME, testResultReports);
        Renderable renderable = RenderableFactory.getRenderable(view);
        this.parsedTestResultsView = renderable.render(view, parsedTestResults);
    }

    /**
     * Returns the name of the test scenario.
     *
     * @return name of the test scenario
     */
    public String getTestScenarioName() {
        return testScenarioName;
    }

    /**
     * Returns the list of test result reports for the test scenario.
     *
     * @return list of test result reports for the test scenario
     */
    public List<TestResultReport<T>> getTestResultReports() {
        return testResultReports;
    }

    /**
     * Returns the rendered view string.
     *
     * @return rendered view string
     */
    public String getParsedTestResultsView() {
        return parsedTestResultsView;
    }
}
