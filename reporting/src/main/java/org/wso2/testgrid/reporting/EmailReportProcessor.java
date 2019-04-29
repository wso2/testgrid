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
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.reporting.summary.InfrastructureBuildStatus;
import org.wso2.testgrid.reporting.summary.InfrastructureSummaryReporter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.wso2.testgrid.common.TestGridConstants.HTML_LINE_SEPARATOR;
import static org.wso2.testgrid.common.TestPlanStatus.FAIL;

/**
 * This class is responsible for process and generate all the content for the email-report for TestReportEngine.
 * The report will consist of base details such as product status, git build details, as well as per test-plan
 * (per infra-combination) details such as test-plan log, test-plan infra combination.
 */
class EmailReportProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EmailReportProcessor.class);
    private final InfrastructureSummaryReporter infrastructureSummaryReporter;
    private TestPlanUOW testPlanUOW;
    private InfrastructureParameterUOW infrastructureParameterUOW;

    EmailReportProcessor() {
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
     * Returns the current status of the product.
     *
     * @param product product
     * @return current status of the product
     */
    TestPlanStatus getProductStatus(Product product) {
        return testPlanUOW.getCurrentStatus(product);
    }

    /**
     * Returns the Git build information of the latest build of the product.
     * This will consist of git location and git revision used to build the distribution.
     *
     * @return Git build information
     */
    String getGitBuildDetails(List<TestPlan> testPlans) {
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

        Function<List<Script>, String> scriptPathsDisplayFunc = (script) -> script.stream()
                .filter(s -> Script.Phase.CREATE.equals(s.getPhase())
                        || Script.Phase.CREATE_AND_DELETE.equals(s.getPhase()))
                .map(Script::getFile)
                .collect(Collectors.joining(", "));
        final String infraFiles = scriptPathsDisplayFunc.apply(provisioner.getScripts());
        final String deployFiles = scriptPathsDisplayFunc.apply(dp.getScripts());
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
    boolean hasFailedTests(List<TestPlan> testPlans) {
        for (TestPlan testPlan : testPlans) {
            if (testPlan.getStatus().equals(FAIL)) {
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
    Map<String, InfrastructureBuildStatus> getSummaryTable(List<TestPlan> testPlans)
            throws TestGridDAOException {
        return infrastructureSummaryReporter.getSummaryTable(testPlans);
    }

    /**
     * Get the tested infrastructures as a html string content.
     *
     * @param testPlans executed test plans
     */
    String getTestedInfrastructures(List<TestPlan> testPlans) throws TestGridDAOException {

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

    /**
     *
     * @param testPlans list of test plans of a particular tg build
     * @param filter filter the test plans
     * @return a map where key = infra-combination string, value = test plan dashboard url
     */
    Map<String, String> getErroneousInfrastructuresOf(List<TestPlan> testPlans,
            Function<TestPlan, Boolean> filter) {
        Map<String, String> erroneousInfraMap = new HashMap<>();
        try {
            final Set<InfrastructureValueSet> infrastructureValueSet = infrastructureParameterUOW.getValueSet();
            Set<InfrastructureParameter> infraParams;
            Map<String, List<InfrastructureParameter>> infraMap;
            String infraStr;
            for (TestPlan testPlan : testPlans) {
                String dashboardUrl = TestGridUtil.getDashboardURLFor(testPlan);
                if (filter.apply(testPlan)) {
                    infraParams = new HashSet<>(TestGridUtil.
                            getInfraParamsOfTestPlan(infrastructureValueSet, testPlan));
                    infraMap = infraParams.stream().collect(Collectors.groupingBy(InfrastructureParameter::getType));
                    infraStr = infraMap.entrySet().stream()
                            .map(entry -> entry.getValue().stream().map(InfrastructureParameter::getName)
                                    .collect(Collectors.joining(", ")))
                            .collect(Collectors.joining(", "));
                    erroneousInfraMap.put(infraStr, dashboardUrl);
                }
            }
        } catch (TestGridDAOException e) {
            logger.warn("Error while email generation. Failed to connect to db. Error: " + e.getMessage());
            erroneousInfraMap.put("Error while collecting testplan errors", "empty");
        }
        return erroneousInfraMap;
    }

}
