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
 */

package org.wso2.testgrid.reporting.surefire;

import org.apache.maven.plugin.surefire.log.api.NullConsoleLogger;
import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.SurefireReportParser;
import org.apache.maven.reporting.MavenReportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;

/**
 * This class is responsible for parsing the surefire reports
 * and providing a object model that can be read and further processed.
 * <p>
 * Surefire reports need to located inside the ${data-bucket}/surefire-reports folder.
 * All '*.xml' files that begin with "testsuite" are treated as a surefire report.
 * <p>
 * Default report is named as <b>TEST-TestSuite.xml</b>.
 * <p>
 * Internally, this uses the maven-surefire-reporter.
 */
public class SurefireReporter {

    private static final Logger logger = LoggerFactory.getLogger(SurefireReporter.class);
    private static final int FAILURE_MSG_LENGTH = 72;

    /**
     * Parses the surefire reports and returns a summarized TestResult object.
     *
     * @param testPlan the testplan
     * @return test result
     */
    public TestResult getReport(TestPlan testPlan) {
        TestResult testResult = new TestResult();
        try {
            Path filePath = TestGridUtil.getSurefireReportsDir(testPlan);
            final SurefireReportParser surefireReportParser = new SurefireReportParser(
                    Collections.singletonList(filePath.toFile()),
                    ENGLISH,
                    new NullConsoleLogger());
            final List<ReportTestSuite> reportTestSuites = surefireReportParser.parseXMLReportFiles();
            final Map<String, String> summary = surefireReportParser.getSummary(reportTestSuites);
            testResult.totalTests = summary.get("totalTests");
            testResult.totalFailures = summary.get("totalFailures");
            testResult.totalErrors = summary.get("totalErrors");
            testResult.totalSkipped = summary.get("totalSkipped");

            final List<ReportTestCase> failureDetails = surefireReportParser.getFailureDetails(reportTestSuites);
            testResult.failureTests = getTests(failureDetails, ReportTestCase::hasFailure);
            testResult.errorTests = getTests(failureDetails, ReportTestCase::hasError);

            return testResult;
        } catch (MavenReportException e) {
            logger.warn("Error while processing surefire-reports for " + testPlan.getId() + " for infra combination:"
                    + " " + testPlan.getInfraParameters() + ". Continuing processing of other test plans", e);
        }

        return testResult;
    }

    private List<String> getTests(List<ReportTestCase> failureDetails, Predicate<? super ReportTestCase> filter) {
        return failureDetails.parallelStream()
                .filter(filter)
                .map(tc -> {
                    String failureMsg = tc.getFailureMessage() == null ? "" : tc.getFailureMessage();
                    int length = failureMsg.length() < FAILURE_MSG_LENGTH ? failureMsg.length() : FAILURE_MSG_LENGTH;
                    failureMsg = failureMsg.substring(0, length);
                    return tc.getClassName() + "#" + tc.getName() + " --- " + failureMsg + " --- since 0 days.";
                }) // todo add historical data of the test cases.
                .collect(Collectors.toList());
    }

}
