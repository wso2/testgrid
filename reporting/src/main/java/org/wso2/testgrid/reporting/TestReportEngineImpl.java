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
package org.wso2.testgrid.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestReportEngine;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.TestReportEngineException;
import org.wso2.testgrid.common.util.EnvironmentUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ReportGenerationUOW;
import org.wso2.testgrid.reporting.model.ProductTestPlanView;
import org.wso2.testgrid.reporting.model.TestPlanView;
import org.wso2.testgrid.reporting.model.TestScenarioView;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;
import org.wso2.testgrid.reporting.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for generating the test reports.
 *
 * @since 1.0.0
 */
public class TestReportEngineImpl implements TestReportEngine {

    private static final Log log = LogFactory.getLog(TestReportEngineImpl.class);

    private static final String PRODUCT_TEST_PLAN_VIEW = "productTestPlanView";
    private static final String PRODUCT_TEST_PLAN_MUSTACHE = "product_test_plan.mustache";
    private static final String TEST_PLAN_MUSTACHE = "test_plan.mustache";
    private static final String TEST_SCENARIO_MUSTACHE = "test_scenario.mustache";
    private static final String TEST_CASE_MUSTACHE = "test_case.mustache";
    private static final String HTML_EXTENSION = ".html";

    @Override
    public void generateReport(String productName, String productVersion) throws TestReportEngineException {

        ProductTestPlan productTestPlan = getProductTestPlan(productName, productVersion);
        Map<String, Object> parsedTestResultMap = parseTestResult(productTestPlan);

        String hTMLString = renderParsedTestResultMap(parsedTestResultMap);
        String fileName = productTestPlan.getProductName() + "-" + productTestPlan.getProductVersion() + "-" +
                          productTestPlan.getStartTimestamp() + HTML_EXTENSION;

        writeHTMLToFile(fileName, hTMLString);
    }

    /**
     * Write the given HTML string to the given file at test grid home.
     *
     * @param fileName   file name to write
     * @param hTMLString HTML string to be written to the file
     * @throws TestReportEngineException thrown when error on writing the HTML string to file
     */
    private void writeHTMLToFile(String fileName, String hTMLString) throws TestReportEngineException {
        try {
            // TODO: Implement a common way to get test grid home
            String testGridHome = EnvironmentUtil.getSystemVariableValue("TESTGRID_HOME");
            Path reportPath = Paths.get(testGridHome).resolve(fileName);

            log.info("Started writing test results to file...");
            FileUtil.writeToFile(reportPath.toAbsolutePath().toString(), hTMLString);
            log.info("Finished writing test results to file");
        } catch (ReportingException e) {
            throw new TestReportEngineException(StringUtil
                    .concatStrings("Error occurred while writing the HTML string to file", fileName), e);
        }
    }

    /**
     * Renders the parsed test result map and returns the HTML string.
     *
     * @param parsedTestResultMap parsed test result map
     * @return HTML string generated from the parsed test result map
     * @throws TestReportEngineException thrown when error on rendering HTML template
     */
    private String renderParsedTestResultMap(Map<String, Object> parsedTestResultMap) throws TestReportEngineException {
        try {
            Renderable renderable = RenderableFactory.getRenderable(PRODUCT_TEST_PLAN_MUSTACHE);
            return renderable.render(PRODUCT_TEST_PLAN_MUSTACHE, parsedTestResultMap);
        } catch (ReportingException e) {
            throw new TestReportEngineException("Exception occurred while rendering the html template.", e);
        }
    }

    /**
     * Parse test plan result to a map.
     *
     * @param productTestPlan test plan to generate results
     * @return parsed result map
     * @throws TestReportEngineException thrown error on parsing test result
     */
    private Map<String, Object> parseTestResult(ProductTestPlan productTestPlan) throws TestReportEngineException {
        try {
            List<TestPlan> testPlans = getTestPlans(productTestPlan);
            List<TestPlanView> testPlanViews = new ArrayList<>();

            for (TestPlan testPlan : testPlans) {
                List<TestScenario> testScenarios = getTestScenariosForTestPlan(testPlan);
                List<TestScenarioView> testScenarioViews = new ArrayList<>();

                for (TestScenario testScenario : testScenarios) {
                    List<TestCase> testCases = getTestCasesForTestScenario(testScenario);
                    TestScenarioView testScenarioReport =
                            new TestScenarioView(testScenario, testCases, TEST_CASE_MUSTACHE);
                    testScenarioViews.add(testScenarioReport);
                }

                TestPlanView testPlanView = new TestPlanView(testPlan, testScenarioViews, TEST_SCENARIO_MUSTACHE);
                testPlanViews.add(testPlanView);
            }

            ProductTestPlanView productTestPlanView =
                    new ProductTestPlanView(productTestPlan, testPlanViews, TEST_PLAN_MUSTACHE);
            Map<String, Object> parsedResultMap = new HashMap<>();
            parsedResultMap.put(PRODUCT_TEST_PLAN_VIEW, productTestPlanView);

            return parsedResultMap;
        } catch (ReportingException e) {
            throw new TestReportEngineException("Error on parsing test results.", e);
        }
    }

    /**
     * Returns a list of test plans associated with the product test plan.
     *
     * @param productTestPlan product test plan to obtain the list of test plans
     * @return a list of {@link TestPlan} instances associated with the product test plan
     * @throws TestReportEngineException thrown when error on obtaining records for the given product test plan
     */
    private List<TestPlan> getTestPlans(ProductTestPlan productTestPlan) throws TestReportEngineException {
        try {
            ReportGenerationUOW reportGenerationUOW = new ReportGenerationUOW();
            return reportGenerationUOW.getTestPlanListForProductTest(productTestPlan);
        } catch (TestGridDAOException e) {
            throw new TestReportEngineException("Error on obtaining test plans from product test plan.", e);
        }
    }

    /**
     * Returns an instance of {@link ProductTestPlan} for the given product name and product version.
     *
     * @param productName    product name
     * @param productVersion product version
     * @return an instance of {@link ProductTestPlan} for the given product name and product version
     * @throws TestReportEngineException throw when error on obtaining product test plan for the given product name
     *                                   and product version
     */
    private ProductTestPlan getProductTestPlan(String productName, String productVersion)
            throws TestReportEngineException {
        ReportGenerationUOW reportGenerationUOW = new ReportGenerationUOW();
        return reportGenerationUOW.getProductTestPlan(productName, productVersion)
                .orElseThrow(() -> new TestReportEngineException(StringUtil
                        .concatStrings("No product test plan found for product ", productName, " - ",
                                productVersion)));
    }

    /**
     * Returns a list of {@link TestScenario} instances associated with the given test plan.
     *
     * @param testPlan test plan to obtain the test scenarios
     * @return list of {@link TestScenario} instances associated with the given test plan
     * @throws TestReportEngineException thrown when error on retrieving test scenarios
     */
    private List<TestScenario> getTestScenariosForTestPlan(TestPlan testPlan) throws TestReportEngineException {
        try {
            ReportGenerationUOW reportGenerationUOW = new ReportGenerationUOW();
            return reportGenerationUOW.getTestScenariosForTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            throw new TestReportEngineException("Error on obtaining test scenarios for the test plan.", e);
        }
    }

    /**
     * Returns a list of {@link TestCase} instances associated with the given test scenario.
     *
     * @param testScenario test scenario to obtain the test cases
     * @return list of {@link TestCase} instances associated with the given test scenario
     * @throws TestReportEngineException thrown when error on retrieving test cases
     */
    private List<TestCase> getTestCasesForTestScenario(TestScenario testScenario) throws TestReportEngineException {
        try {
            ReportGenerationUOW reportGenerationUOW = new ReportGenerationUOW();
            return reportGenerationUOW.getTestCasesForTestScenario(testScenario);
        } catch (TestGridDAOException e) {
            throw new TestReportEngineException("Error on obtaining test cases for the test scenario.", e);
        }
    }
}
