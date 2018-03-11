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
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.reporting.model.GroupBy;
import org.wso2.testgrid.reporting.model.PerAxisHeader;
import org.wso2.testgrid.reporting.model.PerAxisSummary;
import org.wso2.testgrid.reporting.model.Report;
import org.wso2.testgrid.reporting.model.ReportElement;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;
import org.wso2.testgrid.reporting.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.wso2.testgrid.reporting.AxisColumn.DEPLOYMENT;
import static org.wso2.testgrid.reporting.AxisColumn.INFRASTRUCTURE;
import static org.wso2.testgrid.reporting.AxisColumn.SCENARIO;

/**
 * This class is responsible for generating the test reports.
 *
 * @since 1.0.0
 */
public class TestReportEngine {

    private static final Logger logger = LoggerFactory.getLogger(TestReportEngine.class);

    private static final String REPORT_MUSTACHE = "report.mustache";
    private static final String REPORT_TEMPLATE_KEY = "parsedReport";
    private static final String HTML_EXTENSION = ".html";

    /**
     * Generates a test report based on the given product name and product version.
     *
     * @param product     product to generate the report
     * @param showSuccess whether success tests should be show as well
     * @param groupBy     columns to group by
     * @throws ReportingException thrown when error on generating test report
     */
    public void generateReport(Product product, boolean showSuccess, String groupBy) throws ReportingException {
        AxisColumn uniqueAxisColumn = getGroupByColumn(groupBy);

        // Construct report elements
        List<ReportElement> reportElements = Collections
                .unmodifiableList(constructReportElements(product, uniqueAxisColumn));

        // Break elements by group by (sorting also handled)
        List<GroupBy> groupByList = groupReportElementsBy(uniqueAxisColumn, reportElements, showSuccess);

        // Create per axis summaries
        List<PerAxisHeader> perAxisHeaders = createPerAxisHeaders(uniqueAxisColumn, reportElements, showSuccess);

        // Generate HTML string
        Report report = new Report(showSuccess, product, groupByList, perAxisHeaders);
        Map<String, Object> parsedResultMap = new HashMap<>();
        parsedResultMap.put(REPORT_TEMPLATE_KEY, report);
        Renderable renderable = RenderableFactory.getRenderable(REPORT_MUSTACHE);
        String htmlString = renderable.render(REPORT_MUSTACHE, parsedResultMap);

        // Write to HTML file
        String fileName = StringUtil
                .concatStrings(product.getName(), "-", product.getCreatedTimestamp(), "-", uniqueAxisColumn,
                        HTML_EXTENSION);
        String testGridHome = TestGridUtil.getTestGridHomePath();
        Path reportPath = Paths.get(testGridHome).resolve(product.getName()).resolve(fileName);
        writeHTMLToFile(reportPath, htmlString);
    }

    /**
     * Creates and returns a list of per axis headers for the given params.
     *
     * @param uniqueAxisColumn unique axis column
     * @param reportElements   report elements
     * @param showSuccess      whether success tests should be show as well
     * @return list of per axis headers for the given params
     * @throws ReportingException thrown when error on creating per axis headers
     */
    private List<PerAxisHeader> createPerAxisHeaders(AxisColumn uniqueAxisColumn, List<ReportElement> reportElements,
                                                     boolean showSuccess)
            throws ReportingException {
        List<PerAxisHeader> perAxisHeaders = new ArrayList<>();

        // Get report elements grouped
        Map<String, List<ReportElement>> groupedReportElements =
                getGroupedReportElementsByColumn(uniqueAxisColumn, reportElements);

        for (Map.Entry<String, List<ReportElement>> entry : groupedReportElements.entrySet()) {
            List<PerAxisSummary> perAxisSummaries = new ArrayList<>();

            // Capture success and fail count
            Map<Boolean, List<ReportElement>> groupedByTestStatusMap = entry.getValue().stream()
                    .collect(Collectors.groupingBy(ReportElement::isTestSuccess));
            int successCount = groupedByTestStatusMap.get(true) == null ? 0 : groupedByTestStatusMap.get(true).size();
            int failCount = groupedByTestStatusMap.get(false) == null ? 0 : groupedByTestStatusMap.get(false).size();

            // Process overall result
            List<ReportElement> reportElementsForOverallResult =
                    processOverallResultForUniqueAxis(uniqueAxisColumn, entry.getValue());

            // Filter success test cases based on user input
            List<ReportElement> filteredReportElements = showSuccess ?
                                                         reportElementsForOverallResult :
                                                         filterSuccessReportElements(reportElementsForOverallResult);

            // grouped by the value of the unique column
            for (ReportElement reportElement : filteredReportElements) {
                PerAxisSummary perAxisSummary = createPerAxisSummary(uniqueAxisColumn, reportElement);
                perAxisSummaries.add(perAxisSummary);
            }

            if (!perAxisSummaries.isEmpty()) {
                PerAxisHeader perAxisHeader = createPerAxisHeader(uniqueAxisColumn, entry.getKey(),
                        sortPerAxisSummaries(perAxisSummaries), successCount, failCount);
                perAxisHeaders.add(perAxisHeader);
            }
        }
        return sortPerAxisHeaders(perAxisHeaders);
    }

    /**
     * Processes the overall result for the given axis.
     *
     * @param reportElements report elements to process
     * @return processed report elements
     */
    @SuppressWarnings("unchecked")
    private List<ReportElement> processOverallResultForUniqueAxis(AxisColumn uniqueAxisColumn,
                                                                  List<ReportElement> reportElements) {
        switch (uniqueAxisColumn) {
            case INFRASTRUCTURE:
                return processOverallResultForInfrastructureAxis(reportElements);
            case DEPLOYMENT:
                return processOverallResultForDeploymentAxis(reportElements);
            case SCENARIO:
            default:
                return processOverallResultForScenarioAxis(reportElements);
        }
    }

    /**
     * Processes the overall result for scenario axis.
     *
     * @param reportElements report elements to process
     * @return processed report elements
     */
    private List<ReportElement> processOverallResultForScenarioAxis(List<ReportElement> reportElements) {
        List<ReportElement> distinctElements = distinctList(reportElements, ReportElement::getDeployment,
                ReportElement::getInfraParams, ReportElement::isTestSuccess);

        // Fail list
        List<ReportElement> failList = distinctElements.stream()
                .filter(reportElement -> !reportElement.isTestSuccess())
                .collect(Collectors.toList());

        // Success list
        List<ReportElement> successList = distinctElements.stream()
                .filter(ReportElement::isTestSuccess)
                .collect(Collectors.toList());

        List<ReportElement> filteredSuccessList = new ArrayList<>(failList);

        for (ReportElement successListReportElement : successList) {
            boolean isBreakLoop = false;
            for (ReportElement failListReportElement : failList) {
                if (successListReportElement.getDeployment().equals(failListReportElement.getDeployment()) &&
                    successListReportElement.getInfraParams()
                            .equals(failListReportElement.getInfraParams())) {

                    // If same combination os found, omit this next time
                    failList.remove(failListReportElement);
                    isBreakLoop = true;
                    break;
                }
            }
            if (isBreakLoop) {
                continue;
            }
            filteredSuccessList.add(successListReportElement);
        }
        return filteredSuccessList;
    }

    /**
     * Processes the overall result for deployment axis.
     *
     * @param reportElements report elements to process
     * @return processed report elements
     */
    private List<ReportElement> processOverallResultForDeploymentAxis(List<ReportElement> reportElements) {
        List<ReportElement> distinctElements = distinctList(reportElements, ReportElement::getInfraParams,
                ReportElement::getScenarioName, ReportElement::isTestSuccess);

        // Fail list
        List<ReportElement> failList = distinctElements.stream()
                .filter(reportElement -> !reportElement.isTestSuccess())
                .collect(Collectors.toList());

        // Success list
        List<ReportElement> successList = distinctElements.stream()
                .filter(ReportElement::isTestSuccess)
                .collect(Collectors.toList());

        List<ReportElement> filteredSuccessList = new ArrayList<>(failList);

        for (ReportElement successListReportElement : successList) {
            boolean isBreakLoop = false;
            for (ReportElement failListReportElement : failList) {
                if (successListReportElement.getInfraParams()
                            .equals(failListReportElement.getInfraParams()) &&
                    successListReportElement.getScenarioName().equals(failListReportElement.getScenarioName())) {

                    // If same combination os found, omit this next time
                    failList.remove(failListReportElement);
                    isBreakLoop = true;
                    break;
                }
            }
            if (isBreakLoop) {
                continue;
            }
            filteredSuccessList.add(successListReportElement);
        }
        return filteredSuccessList;
    }

    /**
     * Processes the overall result for infrastructure axis.
     *
     * @param reportElements report elements to process
     * @return processed report elements
     */
    private List<ReportElement> processOverallResultForInfrastructureAxis(List<ReportElement> reportElements) {
        List<ReportElement> distinctElements = distinctList(reportElements, ReportElement::getDeployment,
                ReportElement::getScenarioName, ReportElement::isTestSuccess);

        // Fail list
        List<ReportElement> failList = distinctElements.stream()
                .filter(reportElement -> !reportElement.isTestSuccess())
                .collect(Collectors.toList());

        // Success list
        List<ReportElement> successList = distinctElements.stream()
                .filter(ReportElement::isTestSuccess)
                .collect(Collectors.toList());

        List<ReportElement> filteredSuccessList = new ArrayList<>(failList);

        for (ReportElement successListReportElement : successList) {
            boolean isBreakLoop = false;
            for (ReportElement failListReportElement : failList) {
                if (successListReportElement.getDeployment().equals(failListReportElement.getDeployment()) &&
                    successListReportElement.getScenarioName().equals(failListReportElement.getScenarioName())) {

                    // If same combination os found, omit this next time
                    failList.remove(failListReportElement);
                    isBreakLoop = true;
                    break;
                }
            }
            if (isBreakLoop) {
                continue;
            }
            filteredSuccessList.add(successListReportElement);
        }
        return filteredSuccessList;
    }

    /**
     * Returns a list distinct by the given predicates.
     *
     * @param list          list for distinction
     * @param keyExtractors key extractors
     * @param <T>           type of the list
     * @return list distinct by the given predicates
     */
    @SafeVarargs
    private final <T> List<T> distinctList(List<T> list, Function<? super T, ?>... keyExtractors) {
        return list
                .stream()
                .filter(distinctByKeys(keyExtractors))
                .collect(Collectors.toList());
    }

    /**
     * Returns a predicate for distinction by the given fields.
     *
     * @param keyExtractors key extractors
     * @param <T>           type of the predicate
     * @return predicate for distinction by the given fields
     */
    @SafeVarargs
    private final <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();
        return t -> {
            final List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());
            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }

    /**
     * Sort per axis headers by unique column value.
     *
     * @param perAxisHeaders per axis headers to sort
     * @return sorted per axis headers
     */
    private List<PerAxisHeader> sortPerAxisHeaders(List<PerAxisHeader> perAxisHeaders) {
        perAxisHeaders.sort((perAxisHeader1, perAxisHeader2) ->
                perAxisHeader1.getUniqueAxisValue().compareToIgnoreCase(perAxisHeader2.getUniqueAxisValue()));
        return perAxisHeaders;
    }

    /**
     * Sort per axis summaries by axis 1 and 2.
     *
     * @param perAxisSummaries per axis summaries to sort
     * @return sorted per axis summaries
     */
    private List<PerAxisSummary> sortPerAxisSummaries(List<PerAxisSummary> perAxisSummaries) {
        perAxisSummaries.sort((perAxisSummary1, perAxisSummary2) -> {
            // First sort by axis 1
            int result = perAxisSummary1.getAxis1Value().compareToIgnoreCase(perAxisSummary2.getAxis1Value());
            if (result != 0) {
                return result;
            }
            // Finally by axis 2
            return perAxisSummary1.getAxis2Value().compareToIgnoreCase(perAxisSummary2.getAxis2Value());
        });
        return perAxisSummaries;
    }

    /**
     * Creates and returns an instance of {@link PerAxisSummary} for the given params.
     *
     * @param uniqueAxisColumn unique axis column
     * @param reportElement    report element
     * @return instance of {@link PerAxisSummary}
     */
    private PerAxisSummary createPerAxisSummary(AxisColumn uniqueAxisColumn, ReportElement reportElement) {
        boolean isTestSuccess = reportElement.isTestSuccess();
        String scenarioName = reportElement.getScenarioName();
        String deployment = reportElement.getDeployment();
        String infrastructure = reportElement.getInfraParams();

        switch (uniqueAxisColumn) {
            case INFRASTRUCTURE:
                return new PerAxisSummary(deployment, scenarioName, isTestSuccess);
            case DEPLOYMENT:
                return new PerAxisSummary(infrastructure, scenarioName, isTestSuccess);
            case SCENARIO:
            default:
                return new PerAxisSummary(deployment, infrastructure, isTestSuccess);
        }
    }

    /**
     * Returns an instance of {@link PerAxisHeader} for the given unique column. By default this will return the per
     * axis header for the scenario
     *
     * @param uniqueAxisColumn unique axis column
     * @param uniqueAxisValue  unique axis value
     * @param perAxisSummaries per axis summaries
     * @param failCount        total failed test count
     * @param successCount     total success test count
     * @return an instance of {@link PerAxisHeader} for the given unique column
     * @throws ReportingException thrown when error on returning the instance
     */
    private PerAxisHeader createPerAxisHeader(AxisColumn uniqueAxisColumn, String uniqueAxisValue,
                                              List<PerAxisSummary> perAxisSummaries, int successCount, int failCount)
            throws ReportingException {
        switch (uniqueAxisColumn) {
            case INFRASTRUCTURE:
                return new PerAxisHeader(INFRASTRUCTURE, uniqueAxisValue, DEPLOYMENT, SCENARIO, perAxisSummaries,
                        successCount, failCount);
            case DEPLOYMENT:
                return new PerAxisHeader(DEPLOYMENT, uniqueAxisValue, INFRASTRUCTURE, SCENARIO, perAxisSummaries,
                        successCount, failCount);
            case SCENARIO:
            default:
                return new PerAxisHeader(SCENARIO, uniqueAxisValue, DEPLOYMENT, INFRASTRUCTURE, perAxisSummaries,
                        successCount, failCount);
        }
    }

    /**
     * Returns a map of unique column value and report elements grouped by the unique column.
     *
     * @param uniqueAxisColumn unique axis column
     * @param reportElements   report elements to group
     * @return map of unique column value and report elements grouped by the unique column
     */
    private Map<String, List<ReportElement>> getGroupedReportElementsByColumn(AxisColumn uniqueAxisColumn,
                                                                              List<ReportElement> reportElements) {
        switch (uniqueAxisColumn) {
            case INFRASTRUCTURE:
                return reportElements.stream().collect(Collectors.groupingBy(ReportElement::getInfraParams));
            case DEPLOYMENT:
                return reportElements.stream().collect(Collectors.groupingBy(ReportElement::getDeployment));
            case SCENARIO:
            default:
                return reportElements.stream().collect(Collectors.groupingBy(ReportElement::getScenarioName));
        }
    }

    /**
     * Filter out success tests from report elements.
     *
     * @param reportElements report elements to filter
     * @return filtered report elements
     */
    private List<ReportElement> filterSuccessReportElements(List<ReportElement> reportElements) {
        return reportElements.stream()
                .filter(reportElement -> !reportElement.isTestSuccess())
                .collect(Collectors.toList());
    }

    /**
     * Group a list of {@link ReportElement}s by a given {@link AxisColumn}.
     *
     * @param uniqueAxisColumn column to group by
     * @param reportElements   report elements to group
     * @param showSuccess      whether success tests should be show as well
     * @return list of grouped report elements
     * @throws ReportingException thrown when error on grouping report elements
     */
    private List<GroupBy> groupReportElementsBy(AxisColumn uniqueAxisColumn, List<ReportElement> reportElements,
                                                boolean showSuccess)
            throws ReportingException {
        List<GroupBy> groupByList = new ArrayList<>();

        // If show success is false and the test is a success ignore the result of this test
        List<ReportElement> filteredReportElements = showSuccess ?
                                                     new ArrayList<>(reportElements) :
                                                     filterSuccessReportElements(reportElements);

        Map<String, List<ReportElement>> groupedReportElements =
                getGroupedReportElementsByColumn(uniqueAxisColumn, filteredReportElements);

        for (Map.Entry<String, List<ReportElement>> element : groupedReportElements.entrySet()) {
            groupByList.add(new GroupBy(element.getKey(), sortReportElements(element.getValue()), uniqueAxisColumn));
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
            result = re1.getInfraParams().compareToIgnoreCase(re2.getInfraParams());
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
     * Returns the group by column to the given group by column value.
     *
     * @param groupBy raw string where group by column is specified
     * @return group by column
     * @throws ReportingException thrown on error on getting group by column
     */
    private AxisColumn getGroupByColumn(String groupBy) throws ReportingException {
        try {
            if (StringUtil.isStringNullOrEmpty(groupBy)) {
                throw new ReportingException(StringUtil
                        .concatStrings("Column to group by is null or empty", groupBy));
            }
            return AxisColumn.valueOf(groupBy.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new ReportingException(StringUtil
                    .concatStrings("Column to group by is not supported ", groupBy), e);
        }
    }

    /**
     * Returns constructed the report elements for the report.
     *
     * @param product           product test plan to construct the report elements
     * @param groupByAxisColumn grouped by column name
     * @return constructed report elements
     */
    private List<ReportElement> constructReportElements(Product product, AxisColumn groupByAxisColumn) {
        List<ReportElement> reportElements = new ArrayList<>();
        TestPlanUOW testPlanUOW = new TestPlanUOW();
        List<TestPlan> testPlans = testPlanUOW.getLatestTestPlans(product);
        for (TestPlan testPlan : testPlans) {
            List<ReportElement> reportElementsForTestPlan = createReportElementsForTestPlan(testPlan,
                    testPlan.getDeploymentPattern(), groupByAxisColumn);
            reportElements.addAll(reportElementsForTestPlan);
        }
        return reportElements;
    }

    /**
     * Create report elements for the given test plan.
     *
     * @param testPlan          test plan to create report elements
     * @param deploymentPattern deployment pattern to create report elements
     * @param groupByAxisColumn grouped by column
     * @return created report elements
     */
    private List<ReportElement> createReportElementsForTestPlan(
            TestPlan testPlan, DeploymentPattern deploymentPattern, AxisColumn groupByAxisColumn) {
        List<ReportElement> reportElements = new ArrayList<>();

        List<TestScenario> testScenarios = testPlan.getTestScenarios();
        for (TestScenario testScenario : testScenarios) {
            List<TestCase> testCases = testScenario.getTestCases();
            for (TestCase testCase : testCases) {
                ReportElement reportElement = createReportElement(deploymentPattern, testPlan, testScenario,
                        testCase, groupByAxisColumn);
                reportElements.add(reportElement);
            }
        }
        return reportElements;
    }

    /**
     * Creates and returns an {@link ReportElement} instance for the given params.
     *
     * @param deploymentPattern deployment pattern
     * @param testPlan          test plan
     * @param testScenario      test scenario (can be null if the infra is failed)
     * @param testCase          test case (can be null if the infra is failed)
     * @param groupByAxisColumn grouped by column name
     * @return {@link ReportElement} for the given params
     */
    private ReportElement createReportElement(DeploymentPattern deploymentPattern, TestPlan testPlan,
                                              TestScenario testScenario, TestCase testCase,
                                              AxisColumn groupByAxisColumn) {

        ReportElement reportElement = new ReportElement(groupByAxisColumn);

        // Information related to the test plan.
        reportElement.setDeployment(deploymentPattern.getName());
        reportElement.setInfraParams(testPlan.getInfraParameters());

        // Test scenario information
        reportElement.setScenarioName(testScenario.getName());

        // Test case can be null if the infra fails.
        if (testCase != null) {
            reportElement.setTestCase(testCase.getName());
            reportElement.setTestSuccess(testCase.isSuccess());

            if (!testCase.isSuccess()) {
                reportElement.setTestCaseFailureMessage(testCase.getFailureMessage());
            }
        } else {
            reportElement.setTestCase("");
            reportElement.setTestSuccess(false);
        }
        return reportElement;
    }

    /**
     * Write the given HTML string to the given file at test grid home.
     *
     * @param filePath   fully qualified file path
     * @param htmlString HTML string to be written to the file
     * @throws ReportingException thrown when error on writing the HTML string to file
     */
    private void writeHTMLToFile(Path filePath, String htmlString) throws ReportingException {
        logger.info("Writing test results to file: " + filePath.toString());
        FileUtil.writeToFile(filePath.toAbsolutePath().toString(), htmlString);
    }
}
