/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.testgrid.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.PropertyFileReader;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.reporting.model.email.TPResultSection;
import org.wso2.testgrid.reporting.summary.InfrastructureBuildStatus;
import org.wso2.testgrid.reporting.summary.InfrastructureSummaryReporter;
import org.wso2.testgrid.reporting.surefire.SurefireReporter;
import org.wso2.testgrid.reporting.surefire.TestResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.testgrid.common.TestGridConstants.HTML_LINE_SEPARATOR;
import static org.wso2.testgrid.common.TestGridConstants.TEST_PLANS_URI;

/**
 * This class is responsible for process and generate all the content for the email-report for TestReportEngine.
 * The report will consist of base details such as product status, git build details, as well as per test-plan
 * (per infra-combination) details such as test-plan log, test-plan infra combination.
 */
public class EmailReportProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EmailReportProcessor.class);
    private static final int MAX_DISPLAY_TEST_COUNT = 20;
    private final InfrastructureSummaryReporter infrastructureSummaryReporter;
    private TestPlanUOW testPlanUOW;
    private InfrastructureParameterUOW infrastructureParameterUOW;

    public EmailReportProcessor() {
        this.testPlanUOW = new TestPlanUOW();
        this.infrastructureParameterUOW = new InfrastructureParameterUOW();
        this.infrastructureSummaryReporter = new InfrastructureSummaryReporter(infrastructureParameterUOW);
    }

    /**
     * This is created with default access modifier for the purpose of unit tests.
     *
     * @param testPlanUOW the TestPlanUOW
     */
    EmailReportProcessor(TestPlanUOW testPlanUOW, InfrastructureParameterUOW infrastructureParameterUOW) {
        this.testPlanUOW = testPlanUOW;
        this.infrastructureParameterUOW = infrastructureParameterUOW;
        this.infrastructureSummaryReporter = new InfrastructureSummaryReporter(infrastructureParameterUOW);
    }

    /**
     * Populates test-plan result sections in the report considering the latest test-plans of the product.
     *
     * @param product product needing the results
     * @return list of test-plan sections
     */
    public List<TPResultSection> generatePerTestPlanSection(Product product, List<TestPlan> testPlans)
            throws ReportingException {
        List<TPResultSection> perTestPlanList = new ArrayList<>();
        String testGridHost = ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.TESTGRID_HOST);
        String productName = product.getName();
        SurefireReporter surefireReporter = new SurefireReporter();
        for (TestPlan testPlan : testPlans) {
            if (testPlan.getStatus().equals(Status.SUCCESS)) {
                logger.info(String.format("Test plan ,%s, status is set to success. Not adding to email report. "
                        + "Infra combination: %s", testPlan.getId(), testPlan.getInfraParameters()));
                continue;
            }
            String deploymentPattern = testPlan.getDeploymentPattern().getName();
            String testPlanId = testPlan.getId();
            final String infraCombination = testPlan.getInfraParameters();
            final String dashboardLink = String.join("/", testGridHost, productName, deploymentPattern,
                    TEST_PLANS_URI, testPlanId);

            final TestResult report = surefireReporter.getReport(testPlan);
            if (logger.isDebugEnabled()) {
                logger.debug("Test results of test plan '" + testPlan.getId() + "': " + report);
            }

            if ("?".equals(report.getTotalTests()) || "0".equals(report.getTotalTests())
                    || report.getTotalTests().isEmpty()) {
                final Path reportPath = Paths.get(TestGridUtil.getTestGridHomePath())
                        .relativize(TestGridUtil.getSurefireReportsDir(testPlan));
                logger.error("Integration-test log file does not exist at '" + reportPath
                        + "' for test-plan: " + testPlan);
            }

            List<TestResult.TestCaseResult> failureTests = getTrimmedTests(report.getFailureTests(),
                    MAX_DISPLAY_TEST_COUNT);
            List<TestResult.TestCaseResult> errorTests = getTrimmedTests(report.getErrorTests(),
                    MAX_DISPLAY_TEST_COUNT);

            TPResultSection testPlanResultSection = new TPResultSection.TPResultSectionBuilder(
                    infraCombination, deploymentPattern, testPlan.getStatus())
                    .jobName(productName)
                    .dashboardLink(dashboardLink)
                    .failureTests(failureTests)
                    .errorTests(errorTests)
                    .totalTests(report.getTotalTests())
                    .totalFailures(report.getTotalFailures())
                    .totalErrors(report.getTotalErrors())
                    .totalSkipped(report.getTotalSkipped())
                    .build();
            perTestPlanList.add(testPlanResultSection);
        }
        return perTestPlanList;
    }

    private List<TestResult.TestCaseResult> getTrimmedTests(List<TestResult.TestCaseResult> tests,
            int maxDisplayTestCount) {
        final int actualCount = tests.size();
        int displayCount = tests.size();
        displayCount = displayCount < maxDisplayTestCount ? displayCount : maxDisplayTestCount;

        tests = new ArrayList<>(tests.subList(0, displayCount));
        if (displayCount < actualCount) {
            TestResult.TestCaseResult continuation = new TestResult.TestCaseResult();
            continuation.setClassName("...");
            TestResult.TestCaseResult allTestsInfo = new TestResult.TestCaseResult();
            allTestsInfo.setClassName("(view complete list of tests (" + actualCount + ") in testgrid-live..)");
            tests.add(continuation);
            tests.add(continuation);
            tests.add(allTestsInfo);
        }
        return tests;
    }

    /**
     * Returns the current status of the product.
     *
     * @param product product
     * @return current status of the product
     */
    public Status getProductStatus(Product product) {
        return testPlanUOW.getCurrentStatus(product);
    }

    /**
     * Returns the Git build information of the latest build of the product.
     * This will consist of git location and git revision used to build the distribution.
     *
     * @param product product
     * @return Git build information
     */
    public String getGitBuildDetails(Product product, List<TestPlan> testPlans) {
        StringBuilder stringBuilder = new StringBuilder();
        //All the test-plans are executed from the same git revision. Thus git build details are similar across them.
        //Therefore we refer the fist test-plan's git-build details.
        TestPlan testPlan = testPlans.get(0);
        String outputPropertyFilePath = null;
        outputPropertyFilePath =
                TestGridUtil.deriveScenarioOutputPropertyFilePath(testPlan);
        PropertyFileReader propertyFileReader = new PropertyFileReader();
        String gitRevision = propertyFileReader.
                getProperty(PropertyFileReader.BuildOutputProperties.GIT_REVISION, outputPropertyFilePath)
                .orElse("");
        String gitLocation = propertyFileReader.
                getProperty(PropertyFileReader.BuildOutputProperties.GIT_LOCATION, outputPropertyFilePath)
                .orElse("");
        if (gitLocation.isEmpty()) {
            logger.error("Git location received as null/empty for test plan with id " + testPlan.getId());
            stringBuilder.append("Git location: Unknown!");
        } else {
            stringBuilder.append("Git location: ").append(gitLocation);
        }
        stringBuilder.append(HTML_LINE_SEPARATOR);
        if (gitRevision.isEmpty()) {
            logger.error("Git revision received as null/empty for test plan with id " + testPlan.getId());
            stringBuilder.append("Git revision: Unknown!");
        } else {
            stringBuilder.append("Git revision: ").append(gitRevision);
        }
        return stringBuilder.toString();
    }

    /**
     * Check if the latest run of a certain product include failed tests.
     *
     * @param testPlans List of test plans
     * @return if test-failures exists or not
     */
    public boolean hasFailedTests(List<TestPlan> testPlans) {
        for (TestPlan testPlan : testPlans) {
            if (testPlan.getStatus().equals(Status.FAIL)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see InfrastructureSummaryReporter#getSummaryTable(List)
     * @param testPlans the test plans for which we need to generate the summary
     * @return summary table
     */
    public Map<String, InfrastructureBuildStatus> getSummaryTable(List<TestPlan> testPlans) {
        return infrastructureSummaryReporter.getSummaryTable(testPlans);
    }

    /**
     * Get the tested infrastructures as a html string content.
     *
     * @param testCaseInfraSummaryMap the summary map
     */
    public String getTestedInfrastructures(Map<String, InfrastructureBuildStatus> testCaseInfraSummaryMap) {
        final Set<InfrastructureParameter> s = testCaseInfraSummaryMap.values().stream()
                .map(InfrastructureBuildStatus::getSuccessInfra)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        final Set<InfrastructureParameter> f = testCaseInfraSummaryMap.values().stream()
                .map(InfrastructureBuildStatus::getFailedInfra)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        final Set<InfrastructureParameter> u = testCaseInfraSummaryMap.values().stream()
                .map(InfrastructureBuildStatus::getUnknownInfra)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        s.addAll(f);
        s.addAll(u);
        final Map<String, List<InfrastructureParameter>> infraGroupedByType = s.stream()
                .collect(Collectors.groupingBy(InfrastructureParameter::getType));
        final String infraStr = "{<br/>" + infraGroupedByType.entrySet().stream()
                .map(entry -> "&nbsp;&nbsp;<b>" + entry.getKey() + "</b> : " +
                        entry.getValue().stream()
                                .map(InfrastructureParameter::getName)
                                .collect(Collectors.joining(", ")))
                .collect(Collectors.joining(", <br/>")) + "<br/>}";
        return infraStr;

    }
}
