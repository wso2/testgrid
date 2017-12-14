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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.InfraResult;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestReportEngine;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.TestReportEngineException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.ProductTestPlanUOW;
import org.wso2.testgrid.reporting.model.Report;
import org.wso2.testgrid.reporting.model.ReportElement;
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

    private static final Logger logger = LoggerFactory.getLogger(TestReportEngineImpl.class);

    private static final String REPORT_MUSTACHE = "report.mustache";
    private static final String REPORT_TEMPLATE_KEY = "parsedReport";
    private static final String HTML_EXTENSION = ".html";

    @Override
    public void generateReport(String productName, String productVersion, String channel)
            throws TestReportEngineException {
        try {
            ProductTestPlan productTestPlan = getProductTestPlan(productName, productVersion, channel);

            // Construct views
            List<ReportElement> reportElements = constructReportElements(productTestPlan);
            Report report = new Report(productTestPlan, reportElements);
            Map<String, Object> parsedResultMap = new HashMap<>();
            parsedResultMap.put(REPORT_TEMPLATE_KEY, report);
            Renderable renderable = RenderableFactory.getRenderable(REPORT_MUSTACHE);
            String htmlString = renderable.render(REPORT_MUSTACHE, parsedResultMap);

            String fileName = StringUtil.concatStrings(productTestPlan.getProductName(), "-",
                    productTestPlan.getProductVersion(), "-", productTestPlan.getChannel(), HTML_EXTENSION);
            writeHTMLToFile(fileName, htmlString);
        } catch (ReportingException e) {
            throw new TestReportEngineException(StringUtil
                    .concatStrings("Error on generating report for product test plan {Product name: ",
                            productName, ", product version: ", productVersion, ", channel: ", channel));
        }
    }

    /**
     * Returns constructed the report elements for the report.
     *
     * @param productTestPlan product test plan to construct the report elements
     * @return constructed report elements
     */
    private List<ReportElement> constructReportElements(ProductTestPlan productTestPlan) {
        List<TestPlan> testPlans = productTestPlan.getTestPlans();
        List<ReportElement> reportElements = new ArrayList<>();

        for (TestPlan testPlan : testPlans) {
            InfraResult.Status infraStatus = testPlan.getInfraResult().getStatus();
            InfraCombination infraCombination = testPlan.getInfraResult().getInfraCombination();
            boolean isInfraSuccess = !infraStatus.equals(InfraResult.Status.INFRASTRUCTURE_ERROR) &&
                                     !infraStatus.equals(InfraResult.Status.INFRASTRUCTURE_DESTROY_ERROR);

            if (infraStatus.equals(InfraResult.Status.INFRASTRUCTURE_ERROR)) {
                ReportElement reportElement = new ReportElement();
                reportElement.setDeployment(testPlan.getDeploymentPattern());
                reportElement.setOperatingSystem(infraCombination.getOperatingSystem());
                reportElement.setDatabase(infraCombination.getDatabase());
                reportElement.setJdk(infraCombination.getJdk());
                reportElement.setInfraSuccess(isInfraSuccess);
                reportElement.setInfraFailureMessage(InfraResult.Status.INFRASTRUCTURE_ERROR.toString());

                // Add report element to list
                reportElements.add(reportElement);
                continue; // If infra is failed then there are no test scenarios
            }

            // Test scenarios
            List<TestScenario> testScenarios = testPlan.getTestScenarios();
            for (TestScenario testScenario : testScenarios) {

                // Test cases
                List<TestCase> testCases = testScenario.getTestCases();
                for (TestCase testCase : testCases) {

                    // Create report element.
                    ReportElement reportElement = new ReportElement();
                    reportElement.setDeployment(testPlan.getDeploymentPattern());
                    reportElement.setOperatingSystem(infraCombination.getOperatingSystem());
                    reportElement.setDatabase(infraCombination.getDatabase());
                    reportElement.setJdk(infraCombination.getJdk());
                    reportElement.setInfraSuccess(isInfraSuccess);
                    reportElement.setScenarioName(testScenario.getName());
                    reportElement.setTestCase(testCase.getName());
                    reportElement.setTestSuccess(testCase.isTestSuccess());

                    if (!testCase.isTestSuccess()) {
                        reportElement.setTestCaseFailureMessage(testCase.getFailureMessage());
                    }

                    // Add report element to list
                    reportElements.add(reportElement);
                }
            }
        }
        return reportElements;
    }

    /**
     * Write the given HTML string to the given file at test grid home.
     *
     * @param fileName   file name to write
     * @param htmlString HTML string to be written to the file
     * @throws TestReportEngineException thrown when error on writing the HTML string to file
     */
    private void writeHTMLToFile(String fileName, String htmlString) throws TestReportEngineException {
        try {
            String testGridHome = TestGridUtil.getTestGridHomePath();
            Path reportPath = Paths.get(testGridHome).resolve(fileName);

            logger.info("Started writing test results to file...");
            FileUtil.writeToFile(reportPath.toAbsolutePath().toString(), hTMLString);
            logger.info("Finished writing test results to file");
        } catch (ReportingException e) {
            throw new TestReportEngineException(StringUtil
                    .concatStrings("Error occurred while writing the HTML string to file", fileName), e);
        }
    }

    /**
     * Returns an instance of {@link ProductTestPlan} for the given product name and product version.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product test plan channel
     * @return an instance of {@link ProductTestPlan} for the given product name and product version
     * @throws TestReportEngineException throw when error on obtaining product test plan for the given product name
     *                                   and product version
     */
    private ProductTestPlan getProductTestPlan(String productName, String productVersion, String channel)
            throws TestReportEngineException {
        try {
            ProductTestPlanUOW productTestPlanUOW = new ProductTestPlanUOW();
            ProductTestPlan.Channel productTestPlanChannel = ProductTestPlan.Channel.valueOf(channel);
            return productTestPlanUOW.getProductTestPlan(productName, productVersion, productTestPlanChannel)
                    .orElseThrow(() -> new TestReportEngineException(StringUtil
                            .concatStrings("No product test plan found for product {product name: ", productName,
                                    ", product version: ", productVersion, ", channel: ", channel, "}")));
        } catch (IllegalArgumentException e) {
            throw new TestReportEngineException(StringUtil.concatStrings("Channel ", channel,
                    " not found in channels enum."));
        }
    }
}
