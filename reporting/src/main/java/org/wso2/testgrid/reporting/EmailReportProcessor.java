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
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.reporting.model.email.TestPlanResultSection;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.testgrid.common.TestGridConstants.HTML_LINE_SEPARATOR;
import static org.wso2.testgrid.common.TestGridConstants.TEST_PLANS_URI;

/**
 * This class is responsible for process and generate all the content for the email-report for TestReportEngine.
 * The report will consist of base details such as product status, git build details, as well as per test-plan
 * (per infra-combination) details such as test-plan log, test-plan infra combination.
 */
public class EmailReportProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EmailReportProcessor.class);
    private TestPlanUOW testPlanUOW;

    public EmailReportProcessor() {
        this.testPlanUOW = new TestPlanUOW();
    }

    /**
     * This is created with default access modifier for the purpose of unit tests.
     *
     * @param testPlanUOW the TestPlanUOW
     */
    EmailReportProcessor(TestPlanUOW testPlanUOW) {
        this.testPlanUOW = testPlanUOW;
    }

    /**
     * Populates test-plan result sections in the report considering the latest test-plans of the product.
     *
     * @param product product needing the results
     * @return list of test-plan sections
     */
    public List<TestPlanResultSection> generatePerTestPlanSection(Product product, List<TestPlan> testPlans)
            throws ReportingException {
        List<TestPlanResultSection> perTestPlanMap = new ArrayList<>();
        String testGridHost = ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.TESTGRID_HOST);
        String productName = product.getName();
        for (TestPlan testPlan : testPlans) {
            if (testPlan.getStatus().equals(Status.SUCCESS)) {
                continue;
            }
            String deploymentPattern = testPlan.getDeploymentPattern().getName();
            String testPlanId = testPlan.getId();
            TestPlanResultSection testPlanResultSection = new TestPlanResultSection();
            testPlanResultSection.setDeployment(deploymentPattern);
            testPlanResultSection.setInfraCombination(testPlan.getInfraParameters());
            try {
                testPlanResultSection.setResult(getIntegrationTestLogResult(testPlan));
            } catch (ReportingException e) {
                logger.error("Integration test results of the test-plan with id " + testPlan.getId() +
                        " is not added to the email-report due to a reporting exception occurred.", e.getMessage());
            }
            testPlanResultSection.setDashboardLink(Paths.get(
                    testGridHost, productName, deploymentPattern, TEST_PLANS_URI, testPlanId).toString());
            perTestPlanMap.add(testPlanResultSection);
        }
        return perTestPlanMap;
    }

    /**
     * Returns a summary of the integration test results of a given test-plan.
     *
     * @param testPlan test-plan
     * @return Summary of integration test results
     */
    private String getIntegrationTestLogResult(TestPlan testPlan) throws ReportingException {
        StringBuilder stringBuilder = new StringBuilder();
        Path filePath;
        filePath = Paths.get(TestGridUtil.deriveTestIntegrationLogFilePath(testPlan));

        if (Files.exists(filePath)) {
            try (BufferedReader bufferedReader = Files.newBufferedReader(filePath)) {
                String currentLine;
                int lineCount = 0;
                while ((currentLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(currentLine).append(HTML_LINE_SEPARATOR);
                    if (++lineCount == 100) {
                        stringBuilder.append(".........").append(HTML_LINE_SEPARATOR).append(".........")
                                .append(HTML_LINE_SEPARATOR).append("(view complete log in detailed results..)");
                        break;
                    }
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                throw new ReportingException("Error occurred while reading integration-test log file to get" +
                        " integration test results for test-plan with id " + testPlan.getId() + "of the product " +
                        testPlan.getDeploymentPattern().getProduct().getName() + "having infra-combination as " +
                        testPlan.getInfraParameters(), e);
            }
        } else {
            if (testPlan.getStatus().equals(Status.SUCCESS) || testPlan.getStatus().equals(Status.FAIL)) {
                logger.error("Integration-test log file does not exists for test-plan with id "
                        + testPlan.getId() + "of the product " +
                        testPlan.getDeploymentPattern().getProduct().getName() + "having infra-combination as " +
                        testPlan.getInfraParameters());
                return "Integration test-log is missing. Please contact TestGrid administrator.";
            } else {
                return "Test status is " + testPlan.getStatus() + ". Hence can not display the log.";
            }
        }
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
}
