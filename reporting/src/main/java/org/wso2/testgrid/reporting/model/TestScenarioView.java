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

import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to capture the information required to generate the test scenario view.
 *
 * @since 1.0.0
 */
public class TestScenarioView {

    private static final String RESULT_TEMPLATE_KEY_NAME = "parsedTestResultsView";
    private final String testScenarioName;
    private final List<TestCaseView> testCaseViews;
    private String parsedTestResultsView;

    /**
     * Constructs an instance of a test scenario view.
     *
     * @param testScenario test scenario to be rendered in the view
     * @param testCases    list of test testCases of the test scenario
     * @param view         view to be rendered
     * @throws ReportingException thrown when error on rendering view
     */
    public TestScenarioView(TestScenario testScenario, List<TestCase> testCases, String view)
            throws ReportingException {
        this.testScenarioName = testScenario.getName();

        List<TestCaseView> testCaseViews = new ArrayList<>();
        for (TestCase testCase : testCases) {
            TestCaseView testCaseView = new TestCaseView(testCase);
            testCaseViews.add(testCaseView);
        }
        this.testCaseViews = Collections.unmodifiableList(testCaseViews);

        // Render test results
        Map<String, Object> parsedTestResults = new HashMap<>();
        parsedTestResults.put(RESULT_TEMPLATE_KEY_NAME, this.testCaseViews);
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
     * Returns the list of test case views for the test scenario.
     *
     * @return list of test case views for the test scenario
     */
    public List<TestCaseView> getTestCaseViews() {
        return testCaseViews;
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
