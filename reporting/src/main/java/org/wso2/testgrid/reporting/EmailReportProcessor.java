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
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.ConfigurationContext.ConfigurationProperties;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridRuntimeException;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.common.plugins.AWSArtifactReader;
import org.wso2.testgrid.common.plugins.ArtifactReadable;
import org.wso2.testgrid.common.plugins.ArtifactReaderException;
import org.wso2.testgrid.common.util.S3StorageUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.reporting.model.email.TPResultSection;
import org.wso2.testgrid.reporting.summary.InfrastructureBuildStatus;
import org.wso2.testgrid.reporting.summary.InfrastructureSummaryReporter;
import org.wso2.testgrid.reporting.surefire.SurefireReporter;
import org.wso2.testgrid.reporting.surefire.TestResult;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
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
    public List<TPResultSection> generatePerTestPlanSection(Product product, List<TestPlan> testPlans) {
        List<TPResultSection> perTestPlanList = new ArrayList<>();
        String testGridHost = ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.TESTGRID_HOST);
        String productName = product.getName();
        SurefireReporter surefireReporter = new SurefireReporter();
        for (TestPlan testPlan : testPlans) {
            if (testPlan.getStatus().equals(TestPlanStatus.SUCCESS)) {
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
                final Path reportPath = TestGridUtil.getSurefireReportsDir(testPlan);
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
    public TestPlanStatus getProductStatus(Product product) {
        return testPlanUOW.getCurrentStatus(product);
    }

    /**
     * Returns the Git build information of the latest build of the product.
     * This will consist of git location and git revision used to build the distribution.
     *
     * @return Git build information
     */
    public String getGitBuildDetails(List<TestPlan> testPlans) {
        StringBuilder stringBuilder = new StringBuilder();
        //All the test-plans are executed from the same git revision. Thus git build details are similar across them.
        //Therefore we refer the fist test-plan's git-build details.
        if (testPlans.isEmpty()) {
            return "No test plans were run!";
        }
        TestPlan testPlan = testPlans.get(0);
        final InfrastructureConfig.Provisioner provisioner = testPlan.getInfrastructureConfig().getProvisioners()
                .get(0);
        final DeploymentConfig.DeploymentPattern dp = testPlan.getDeploymentConfig()
                .getDeploymentPatterns().get(0);
        final List<ScenarioConfig> scenarioConfigs = testPlan.getScenarioConfigs();
        final String infraFiles = provisioner.getScripts().stream()
                .filter(s -> Script.Phase.CREATE.equals(s.getPhase())
                        || Script.Phase.CREATE_AND_DELETE.equals(s.getPhase()))
                .map(Script::getFile)
                .collect(Collectors.joining(", "));
        final String deployFiles = dp.getScripts().stream()
                .filter(s -> Script.Phase.CREATE.equals(s.getPhase())
                        || Script.Phase.CREATE_AND_DELETE.equals(s.getPhase()))
                .map(Script::getFile)
                .collect(Collectors.joining(", "));
        stringBuilder.append("Infrastructure script: ")
                .append(Optional.ofNullable(provisioner.getRemoteRepository()).orElse(
                        TestGridConstants.NOT_CONFIGURED_STR))
                .append(" @ ")
                .append(provisioner.getRemoteBranch())
                .append(": ")
                .append(infraFiles)
                .append(HTML_LINE_SEPARATOR);
        stringBuilder.append("Deployment script: ")
                .append(Optional.ofNullable(dp.getRemoteRepository()).orElse(
                        TestGridConstants.NOT_CONFIGURED_STR))
                .append(" @ ")
                .append(dp.getRemoteBranch())
                .append(": ")
                .append(deployFiles)
                .append(HTML_LINE_SEPARATOR);
        for (ScenarioConfig scenarioConfig : scenarioConfigs) {
            stringBuilder.append("Test repo: ")
                    .append(Optional.ofNullable(scenarioConfig.getRemoteRepository()).orElse(
                            TestGridConstants.NOT_CONFIGURED_STR))
                    .append(" @ ")
                    .append(scenarioConfig.getRemoteBranch())
                    .append(HTML_LINE_SEPARATOR);
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
            if (testPlan.getStatus().equals(TestPlanStatus.FAIL)) {
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
    public Map<String, InfrastructureBuildStatus> getSummaryTable(List<TestPlan> testPlans)
            throws TestGridDAOException {
        return infrastructureSummaryReporter.getSummaryTable(testPlans);
    }

    /**
     * Get the tested infrastructures as a html string content.
     *
     * @param testPlans executed test plans
     */
    public String getTestedInfrastructures(List<TestPlan> testPlans) throws TestGridDAOException {

        final Set<InfrastructureValueSet> infrastructureValueSet = infrastructureParameterUOW.getValueSet();
        Set<InfrastructureParameter> infraParams = new HashSet<>();
        for (TestPlan testPlan : testPlans) {
            infraParams.addAll(TestGridUtil.
                    getInfraParamsOfTestPlan(infrastructureValueSet, testPlan));
        }
        final Map<String, List<InfrastructureParameter>> infraMap = infraParams.stream()
                .collect(Collectors.groupingBy(InfrastructureParameter::getType));
        StringJoiner infraStr = new StringJoiner(", <br/>");
        for (Map.Entry<String, List<InfrastructureParameter>> entry : infraMap.entrySet()) {
            String s = "&nbsp;&nbsp;<b>" + entry.getKey() + "</b> : " + entry.getValue().stream()
                    .map(InfrastructureParameter::getName).collect(Collectors.joining(", "));
            infraStr.add(s);
        }
        return infraStr.toString();
    }

    public Map<String, String> getErroneousInfrastructures(List<TestPlan> testPlans) throws TestGridDAOException {
        Map<String, String> erroneousInfraMap = new HashMap<>();
        final Set<InfrastructureValueSet> infrastructureValueSet = infrastructureParameterUOW.getValueSet();
        Set<InfrastructureParameter> infraParams;
        Map<String, List<InfrastructureParameter>> infraMap;
        String infraStr;
        for (TestPlan testPlan : testPlans) {
            String logDownloadPath = TestGridUtil.getDashboardURLFor(testPlan);
            if (testPlan.getStatus() != TestPlanStatus.SUCCESS && testPlan.getStatus() != TestPlanStatus.FAIL) {
                infraParams = new HashSet<>(TestGridUtil.
                        getInfraParamsOfTestPlan(infrastructureValueSet, testPlan));
                infraMap = infraParams.stream().collect(Collectors.groupingBy(InfrastructureParameter::getType));
                infraStr = infraMap.entrySet().stream()
                        .map(entry -> entry.getValue().stream().map(InfrastructureParameter::getName)
                                .collect(Collectors.joining(", ")))
                        .collect(Collectors.joining(", "));
                erroneousInfraMap.put(infraStr, logDownloadPath);
            }
        }
        return erroneousInfraMap;
    }

    /**
     * Returns the Properties in output.properties located in AWS S3 bucket.
     *
     * @param testPlan test plan to read the outputs from
     * @return Properties in output.properties
     */
    private Properties getOutputPropertiesFile (TestPlan testPlan) {
        Properties properties = new Properties();
        try {
            ArtifactReadable artifactReadable = new AWSArtifactReader(ConfigurationContext.
                    getProperty(ConfigurationProperties.AWS_REGION_NAME), ConfigurationContext.
                    getProperty(ConfigurationProperties.AWS_S3_BUCKET_NAME));
            String outputPropertyFilePath = S3StorageUtil.deriveS3ScenarioOutputsFilePath(testPlan, artifactReadable);
            logger.info("Scenario outputs location is " + outputPropertyFilePath);

            try (InputStreamReader inputStreamReader = new InputStreamReader(
                    artifactReadable.getArtifactStream(outputPropertyFilePath), StandardCharsets.UTF_8)) {
            properties.load(inputStreamReader);
            }
        } catch (ArtifactReaderException e) {
            logger.error("Error while initiating AWS artifacts reader.", e.getMessage());
        } catch (TestGridRuntimeException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error("Error while reading git build details from remote storage.", e.getMessage());
        }
        return properties;
    }
}
