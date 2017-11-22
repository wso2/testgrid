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
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestReportEngine;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.Utils;
import org.wso2.testgrid.common.exception.TestReportEngineException;
import org.wso2.testgrid.reporting.model.ProductPlanReport;
import org.wso2.testgrid.reporting.model.TestPlanReport;
import org.wso2.testgrid.reporting.model.TestScenarioReport;
import org.wso2.testgrid.reporting.reader.ResultReadable;
import org.wso2.testgrid.reporting.reader.ResultReaderFactory;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;
import org.wso2.testgrid.reporting.result.TestResultBeanFactory;
import org.wso2.testgrid.reporting.result.TestResultable;
import org.wso2.testgrid.reporting.util.FileUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is responsible for generating the test reports.
 *
 * @since 1.0.0
 */
public class TestReportEngineImpl implements TestReportEngine {

    private static final Log log = LogFactory.getLog(TestReportEngineImpl.class);

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
     * @param testPlan test plan to generate the test report
     * @param productTestPlan
     * @throws TestReportEngineException thrown when reading the results from files or when writing the test report
     * to file
     */
    public void generateReport(TestPlan testPlan, ProductTestPlan productTestPlan) throws TestReportEngineException {
        //todo store per testplan persistence logic here. productTestPlan's details are not supposed to be persisted
        //as part of this. There's a separate method for that.
    }

    /**
     * Generates a test report based on the given Overall test plans.
     *
     * @param productTestPlan test plan to generate the test report
     * @throws TestReportEngineException thrown when reading the results from files or when writing the test report
     * to file
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

        //todo use a constant for tetgrid_home
        Path reportPath = Paths.get(getTestGridHome()).resolve(fileName);
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

        //todo @Vidura/Asma - read the test plan content from DB. The ProductTestPlan does not have any info abt the
        //test plans. It only has product level details like product name, version etc.

        List<TestPlan> testPlans = productTestPlan.getTestPlans();  //todo this returns null. coz test plans r not
        // part of this now.

        if (testPlans == null) {
            String msg = "Test Plans information are not available. These info are no longer stored as part of the "
                    + "ProductTestPlan class. Instead, it should be retrieved from the database.";
            log.error(msg);
            throw new ReportingException(msg);
        }

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
                        Optional<Class<T>> classOptional = TestResultBeanFactory.getResultType(directoryPath);
                        if (!classOptional.isPresent()) {
                            continue; // Ignore directory location
                        }
                        testResults = readResultFiles(directoryPath, classOptional.get());
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

    /**
     * Returns the result list after reading result files.
     *
     * @param path base path to read result files from
     * @param type type of the result bean
     * @param <T>  type of the result bean
     * @return final test result list
     * @throws ReportingException thrown when error on reading result files
     */
    private <T extends TestResultable> List<T> readResultFiles(Path path, Class<T> type) throws ReportingException {
        File[] fileList = FileUtil.getFileList(path);
        List<T> testResults = new ArrayList<>();
        for (File file : fileList) {
            if (file.isDirectory()) {
                testResults.addAll(readResultFiles(Paths.get(file.getAbsolutePath()), type));
            }
            if (file.isFile()) {
                Path filePath = Paths.get(file.getAbsolutePath());
                Optional<ResultReadable> resultReadableOptional = ResultReaderFactory.getResultReader(filePath);
                if (!resultReadableOptional.isPresent()) {
                    continue;
                }
                testResults.addAll(resultReadableOptional.get().readFile(filePath, type));
            }
        }
        return testResults;
    }

    public String getTestGridHome() {
        String testgridHome = System.getenv("TESTGRID_HOME");
        if (testgridHome == null) {
            String tmp = System.getProperty("java.io.tmpdir");
            return Paths.get(tmp, "my-testgrid-home").toString();
        }
        return testgridHome;
    }
}
