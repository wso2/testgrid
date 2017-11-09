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
package org.wso2.carbon.testgrid.reporting;

import org.wso2.carbon.testgrid.common.ProductTestPlan;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.TestReportEngine;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.common.Utils;
import org.wso2.carbon.testgrid.common.exception.TestReportEngineException;
import org.wso2.carbon.testgrid.reporting.model.ProductPlanReport;
import org.wso2.carbon.testgrid.reporting.model.TestPlanReport;
import org.wso2.carbon.testgrid.reporting.model.TestScenarioReport;
import org.wso2.carbon.testgrid.reporting.reader.ResultReadable;
import org.wso2.carbon.testgrid.reporting.reader.ResultReaderFactory;
import org.wso2.carbon.testgrid.reporting.renderer.Renderable;
import org.wso2.carbon.testgrid.reporting.renderer.RenderableFactory;
import org.wso2.carbon.testgrid.reporting.result.TestResultBeanFactory;
import org.wso2.carbon.testgrid.reporting.result.TestResultable;
import org.wso2.carbon.testgrid.reporting.util.FileUtil;

import java.io.File;
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

    private static final String TEST_ARTIFACT_DIR = "Tests";
    private static final String RESULTS_DIR = "Results";
    private static final String HTML_TEMPLATE = "html_template.mustache";
    private static final String RESULT_TEMPLATE = "result_template.mustache";
    private static final String SCENARIO_TEMPLATE = "scenario_template.mustache";
    private static final String PRODUCT_TEMPLATE = "product_template.mustache";
    private static final String HTML_TEMPLATE_KEY_NAME = "productTestPlanReport";
    private static final String HTML_EXTENSION = ".html";

    /**
     * Generates a test report based on the given test plan.
     *
     * @param productTestPlan test plan to generate the test report
     * @throws TestReportEngineException thrown when reading the results from files or when writing the test report to file
     */
    public void generateReport(ProductTestPlan productTestPlan) throws TestReportEngineException {

        Map<String, Object> parsedTestResultMap = null;
        try {
            parsedTestResultMap = parseTestResult(productTestPlan);
        } catch (ReportingException e) {
            throw new TestReportEngineException("Exception occurred while parsing the test results.", e);
        }
        String fileName = productTestPlan.getProductName() + "-" + productTestPlan.getProductVersion() + "-" +
                          productTestPlan.getCreatedTimeStamp() + HTML_EXTENSION;

        // Populate the html from the template
        String htmlString = null;
        try {
            Renderable renderable = RenderableFactory.getRenderable(HTML_TEMPLATE);
            htmlString = renderable.render(HTML_TEMPLATE, parsedTestResultMap);
        } catch (ReportingException e) {
            throw new TestReportEngineException("Exception occurred while rendering the html template.", e);
        }

        Path reportPath = Paths.get(productTestPlan.getHomeDir()).resolve(fileName);
        try {
            FileUtil.writeToFile(reportPath.toAbsolutePath().toString(), htmlString);
        } catch (ReportingException e) {
            throw new TestReportEngineException("Exception occurred while saving the test report.", e);
        }
    }

    /**
     * Parse test plan result to a map.
     *
     * @param productTestPlan test plan to generate results
     * @param <T>             {@link TestResultable type}
     * @return parsed result map
     * @throws ReportingException thrown when reading the results from files
     */
    private <T extends TestResultable> Map<String, Object> parseTestResult(ProductTestPlan productTestPlan)
            throws ReportingException {

        List<TestPlan> testPlans = productTestPlan.getTestPlans();
        List<TestPlanReport> testPlanReports = new ArrayList<>();

        for (TestPlan testPlan : testPlans) {
            List<TestScenario> testScenarios = testPlan.getTestScenarios();
            List<TestScenarioReport> testScenarioReports = new ArrayList<>();

            for (TestScenario testScenario : testScenarios) {
                Path resultPath = Paths.get(Utils.getTestScenarioLocation(testScenario, testPlan.getTestRepoDir()),
                        TEST_ARTIFACT_DIR, RESULTS_DIR);
                File[] directoryList = FileUtil.getFileList(resultPath);
                List<T> testResults = new ArrayList<>();

                for (File directory : directoryList) {
                    if (directory.isDirectory()) {
                        Path directoryPath = Paths.get(directory.getAbsolutePath());
                        Class<T> type = TestResultBeanFactory.getResultType(directoryPath);
                        File[] fileList = FileUtil.getFileList(directoryPath);

                        for (File file : fileList) {
                            if (file.isFile()) {
                                Path filePath = Paths.get(file.getAbsolutePath());
                                ResultReadable resultReader = ResultReaderFactory.getResultReader(filePath);
                                testResults.addAll(resultReader.readFile(filePath, type));
                            }
                        }
                    }
                }
                TestScenarioReport<T> testScenarioReport =
                        new TestScenarioReport<>(testScenario.getSolutionPattern(), testResults, RESULT_TEMPLATE);
                testScenarioReports.add(testScenarioReport);
            }

            TestPlanReport testPlanReport = new TestPlanReport(testPlan, productTestPlan.
                    getInfrastructure(testPlan.getDeploymentPattern()), testScenarioReports, SCENARIO_TEMPLATE);
            testPlanReports.add(testPlanReport);
        }

        ProductPlanReport productPlanReport = new ProductPlanReport(productTestPlan.getProductName(),
                productTestPlan.getProductVersion(), testPlanReports, PRODUCT_TEMPLATE);

        Map<String, Object> parsedResultMap = new HashMap<>();
        parsedResultMap.put(HTML_TEMPLATE_KEY_NAME, productPlanReport);
        return parsedResultMap;
    }
}
