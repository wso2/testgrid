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
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.InfraResult;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.ProductTestPlanUOW;
import org.wso2.testgrid.reporting.model.GroupBy;
import org.wso2.testgrid.reporting.model.Report;
import org.wso2.testgrid.reporting.model.ReportElement;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;
import org.wso2.testgrid.reporting.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is responsible for generating the test reports.
 *
 * @since 1.0.0
 */
public class TestReportEngine {

    private static final Log log = LogFactory.getLog(TestReportEngine.class);

    private static final String REPORT_MUSTACHE = "report.mustache";
    private static final String REPORT_TEMPLATE_KEY = "parsedReport";
    private static final String HTML_EXTENSION = ".html";

    /**
     * Generates a test report based on the given product name and product version.
     *
     * @param productName    product name to generate the test plan for
     * @param productVersion product version to generate the test plan for
     * @param channel        product test plan channel
     * @param showSuccess    whether success tests should be show as well
     * @param groupBy        columns to group by
     * @throws ReportingException thrown when error on generating test report
     */
    public void generateReport(String productName, String productVersion, String channel, boolean showSuccess,
                               String groupBy)
            throws ReportingException {
        ProductTestPlan productTestPlan = getProductTestPlan(productName, productVersion, channel);
        GroupByColumn groupByColumn = getGroupByColumn(groupBy);

        // Construct report elements
        List<ReportElement> reportElements = constructReportElements(productTestPlan, showSuccess);

        // Break elements by group by
        List<GroupBy> groupByList = groupReportElementsBy(groupByColumn, reportElements);

        // Generate HTML string
        Report report = new Report(productTestPlan, groupByList);
        Map<String, Object> parsedResultMap = new HashMap<>();
        parsedResultMap.put(REPORT_TEMPLATE_KEY, report);
        Renderable renderable = RenderableFactory.getRenderable(REPORT_MUSTACHE);
        String htmlString = renderable.render(REPORT_MUSTACHE, parsedResultMap);

        // Write to HTML file
        String fileName = StringUtil.concatStrings(productTestPlan.getProductName(), "-",
                productTestPlan.getProductVersion(), "-", productTestPlan.getChannel(), HTML_EXTENSION);
        writeHTMLToFile(fileName, htmlString);
    }

    /**
     * Group a list of {@link ReportElement}s by a given {@link GroupByColumn}.
     *
     * @param groupByColumn  column to group by
     * @param reportElements report elements to group
     * @return list of grouped report elements
     * @throws ReportingException thrown when error on grouping report elements
     */
    private List<GroupBy> groupReportElementsBy(GroupByColumn groupByColumn, List<ReportElement> reportElements)
            throws ReportingException {
        List<GroupBy> groupByList = new ArrayList<>();
        Map<String, List<ReportElement>> groupedReportElements;
        switch (groupByColumn) {
            case SCENARIO:
                groupedReportElements = reportElements.stream()
                        .collect(Collectors.groupingBy(ReportElement::getScenarioName));
                break;
            case INFRASTRUCTURE:
                groupedReportElements = reportElements.stream()
                        .collect(Collectors.groupingBy(this::constructGroupByInfraString));
                break;
            case DEPLOYMENT:
                groupedReportElements = reportElements.stream()
                        .collect(Collectors.groupingBy(ReportElement::getDeployment));
                break;
            case NONE:
            default:
                groupedReportElements = Collections.singletonMap("", reportElements);
        }

        for (Map.Entry<String, List<ReportElement>> element : groupedReportElements.entrySet()) {
            groupByList.add(new GroupBy(element.getKey(), sortReportElements(element.getValue()), groupByColumn));
        }

        // Sort group by list and return
        return sortGroupByList(groupByList);
    }

    /**
     * Sorts a given group by list by the group by column value.
     *
     * @param groupByList group by list to sort
     * @return sorted group by list
     */
    private List<GroupBy> sortGroupByList(List<GroupBy> groupByList) {
        groupByList.sort((gb1, gb2) -> gb1.getGroupByColumnValue().compareToIgnoreCase(gb2.getGroupByColumnValue()));
        return groupByList;
    }

    /**
     * Sort report elements first by the deployment, then by infra, then by the scenario and finally by test case.
     *
     * @param reportElements report elements to sort
     * @return sorted list of report elements
     */
    private List<ReportElement> sortReportElements(List<ReportElement> reportElements) {
        reportElements.sort((re1, re2) -> {
            // First sort by deployment
            int result = re1.getDeployment().compareToIgnoreCase(re2.getDeployment());
            if (result != 0) {
                return result;
            }
            // The by Infra
            result = constructGroupByInfraString(re1).compareToIgnoreCase(constructGroupByInfraString(re2));
            if (result != 0) {
                return result;
            }
            // Then by scenario
            result = re1.getScenarioName().compareToIgnoreCase(re2.getScenarioName());
            if (result != 0) {
                return result;
            }
            // Finally by test case
            return re1.getTestCase().compareToIgnoreCase(re2.getTestCase());
        });
        return reportElements;
    }

    /**
     * Constructs a string to group report elements by the infrastructures.
     *
     * @param reportElement report element to consider
     * @return string to group report elements by the infrastructures
     */
    private String constructGroupByInfraString(ReportElement reportElement) {
        return StringUtil.concatStrings("Operating System: ", reportElement.getOperatingSystem(),
                " | Database: ", reportElement.getDatabase(), " | JDK: ", reportElement.getJdk());
    }

    /**
     * Returns the group by column to the given group by column value.
     *
     * @param groupBy raw string where group by column is specified
     * @return group by column
     * @throws ReportingException thrown on error on getting group by column
     */
    private GroupByColumn getGroupByColumn(String groupBy) throws ReportingException {
        try {
            if (StringUtil.isStringNullOrEmpty(groupBy)) {
                return GroupByColumn.NONE;
            }
            return GroupByColumn.valueOf(groupBy.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new ReportingException(StringUtil
                    .concatStrings("Column to group by is not supported ", groupBy), e);
        }
    }

    /**
     * Returns constructed the report elements for the report.
     *
     * @param productTestPlan product test plan to construct the report elements
     * @param showSuccess     whether success tests should be show as well
     * @return constructed report elements
     */
    private List<ReportElement> constructReportElements(ProductTestPlan productTestPlan, boolean showSuccess) {
        List<TestPlan> testPlans = productTestPlan.getTestPlans();
        List<ReportElement> reportElements = new ArrayList<>();

        for (TestPlan testPlan : testPlans) {
            InfraResult.Status infraStatus = testPlan.getInfraResult().getStatus();

            // If infra is failed then there are no test scenarios
            if (infraStatus.equals(InfraResult.Status.INFRASTRUCTURE_ERROR)) {
                ReportElement reportElement = createReportElement(testPlan, null, null);
                reportElements.add(reportElement);
                continue;
            }

            // Test scenarios
            List<TestScenario> testScenarios = testPlan.getTestScenarios();
            for (TestScenario testScenario : testScenarios) {

                // Test cases
                List<TestCase> testCases = testScenario.getTestCases();
                for (TestCase testCase : testCases) {
                    // If show success is false and the test is a success ignore the result of this test
                    if (testCase.isTestSuccess() && !showSuccess) {
                        continue;
                    }

                    ReportElement reportElement = createReportElement(testPlan, testScenario, testCase);
                    reportElements.add(reportElement);
                }
            }
        }
        return reportElements;
    }

    /**
     * Creates and returns an {@link ReportElement} instance for the given params.
     *
     * @param testPlan     test plan
     * @param testScenario test scenario (can be null if the infra is failed)
     * @param testCase     test case (can be null if the infra is failed)
     * @return {@link ReportElement} for the given params
     */
    private ReportElement createReportElement(TestPlan testPlan, TestScenario testScenario, TestCase testCase) {
        InfraCombination infraCombination = testPlan.getInfraResult().getInfraCombination();
        InfraResult.Status infraStatus = testPlan.getInfraResult().getStatus();
        boolean isInfraSuccess = !infraStatus.equals(InfraResult.Status.INFRASTRUCTURE_ERROR) &&
                                 !infraStatus.equals(InfraResult.Status.INFRASTRUCTURE_DESTROY_ERROR);

        ReportElement reportElement = new ReportElement();

        // Information related to the test plan.
        reportElement.setDeployment(testPlan.getDeploymentPattern());
        reportElement.setOperatingSystem(infraCombination.getOperatingSystem());
        reportElement.setDatabase(infraCombination.getDatabase());
        reportElement.setJdk(infraCombination.getJdk());
        reportElement.setInfraSuccess(isInfraSuccess);

        // Test scenario can be null if the infra fails.
        String testScenarioName = testScenario != null ? testScenario.getName() : "";
        reportElement.setScenarioName(testScenarioName);

        // Test case can be null if the infra fails.
        if (testCase != null) {
            reportElement.setTestCase(testCase.getName());
            reportElement.setTestSuccess(testCase.isTestSuccess());

            if (!testCase.isTestSuccess()) {
                reportElement.setTestCaseFailureMessage(testCase.getFailureMessage());
            }
        } else {
            reportElement.setTestCase("");
            reportElement.setTestSuccess(false);

            // Failure is due to infra failure
            reportElement.setTestCaseFailureMessage(infraStatus.toString());
        }
        return reportElement;
    }

    /**
     * Write the given HTML string to the given file at test grid home.
     *
     * @param fileName   file name to write
     * @param htmlString HTML string to be written to the file
     * @throws ReportingException thrown when error on writing the HTML string to file
     */
    private void writeHTMLToFile(String fileName, String htmlString) throws ReportingException {
        String testGridHome = TestGridUtil.getTestGridHomePath();
        Path reportPath = Paths.get(testGridHome).resolve(fileName);

        log.info("Started writing test results to file...");
        FileUtil.writeToFile(reportPath.toAbsolutePath().toString(), htmlString);
        log.info("Finished writing test results to file");
    }

    /**
     * Returns an instance of {@link ProductTestPlan} for the given product name and product version.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product test plan channel
     * @return an instance of {@link ProductTestPlan} for the given product name and product version
     * @throws ReportingException throw when error on obtaining product test plan for the given product name
     *                            and product version
     */
    private ProductTestPlan getProductTestPlan(String productName, String productVersion, String channel)
            throws ReportingException {
        try {
            ProductTestPlanUOW productTestPlanUOW = new ProductTestPlanUOW();
            ProductTestPlan.Channel productTestPlanChannel = ProductTestPlan.Channel.valueOf(channel);
            return productTestPlanUOW.getProductTestPlan(productName, productVersion, productTestPlanChannel)
                    .orElseThrow(() -> new ReportingException(StringUtil
                            .concatStrings("No product test plan found for product {product name: ", productName,
                                    ", product version: ", productVersion, ", channel: ", channel, "}")));
        } catch (IllegalArgumentException e) {
            throw new ReportingException(StringUtil.concatStrings("Channel ", channel,
                    " not found in channels enum."));
        }
    }
}
