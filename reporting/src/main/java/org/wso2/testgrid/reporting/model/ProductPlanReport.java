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

import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean class to capture the information required to generate the test plan report.
 *
 * @since 1.0.0
 */
public class ProductPlanReport {

    private static final String PLAN_TEMPLATE_KEY_NAME = "parsedTestPlanView";
    private final String productName;
    private final String productVersion;
    private final List<TestPlanReport> testPlanReports;
    private final String parsedTestPlanView;

    /**
     * Constructs an instance of a test plan report.
     *
     * @param productName     product name of the test plan executed
     * @param productVersion  product version of the test plan executed
     * @param testPlanReports list of test plan results
     * @param view            view to be rendered
     * @throws ReportingException thrown when error on rendering view
     */
    public ProductPlanReport(String productName, String productVersion, List<TestPlanReport> testPlanReports,
                             String view) throws ReportingException {
        this.productName = productName;
        this.productVersion = productVersion;
        this.testPlanReports = Collections.unmodifiableList(testPlanReports);

        // Render test plans
        Map<String, Object> parsedTestScenarios = new HashMap<>();
        parsedTestScenarios.put(PLAN_TEMPLATE_KEY_NAME, testPlanReports);
        Renderable renderable = RenderableFactory.getRenderable(view);
        this.parsedTestPlanView = renderable.render(view, parsedTestScenarios);
    }

    /**
     * Returns the product name of the test plan executed.
     *
     * @return product name of the test plan executed
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Returns the product version of the test plan executed.
     *
     * @return product version of the test plan executed
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Returns the list of test plan reports.
     *
     * @return test plan reports
     */
    public List<TestPlanReport> getTestPlanReports() {
        return testPlanReports;
    }

    /**
     * Returns the rendered view string.
     *
     * @return rendered view string
     */
    public String getParsedTestPlanView() {
        return parsedTestPlanView;
    }
}
