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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.dto.TestCaseFailureResultDTO;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.reporting.model.email.BuildExecutionSummary;
import org.wso2.testgrid.reporting.model.email.BuildFailureSummary;
import org.wso2.testgrid.reporting.model.email.InfraCombination;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is responsible for providing required data in order to  generate email graphs.
 *
 * @since 1.0.0
 */
public class GraphDataProvider {
    private TestPlanUOW testPlanUOW;
    private static final int MAXIMUM_TIME_RANGE = 30;
    private static final int TEST_EXECUTION_HISTORY_RANGE = 7;
    private static final String OPERATING_SYSTEM = "OS";
    private static final String JDK = "JDK";
    private static final String DATABASE_ENGINE = "DBEngine";
    private static final String DATABASE_ENGINE_VERSION = "DBEngineVersion";

    public GraphDataProvider() {
        this.testPlanUOW = new TestPlanUOW();
    }

    /**
     * Provides the test failure summary for a given build job. Test Plan ids of the build job is retrieved by reading
     * testgrid yaml files which contains in the current workspace.
     *
     * @param workspace   current workspace
     * @throws ReportingException thrown when getting test plan ids by reading test plan yaml files
     */
    public List<BuildFailureSummary> getTestFailureSummary(String workspace) throws ReportingException {
        List<TestCaseFailureResultDTO> testFailureSummary;
        try {
            testFailureSummary = testPlanUOW
                    .getTestFailureSummary(FileUtil.getTestPlanIdByReadingTGYaml(workspace));
            if (testFailureSummary.isEmpty()) {
                return Collections.emptyList();
            }
            return processTestFailureSummary(testFailureSummary);
        } catch (TestGridException e) {
            throw new ReportingException("Error occurred while reading yaml files in the TG home", e);
        } catch (TestGridDAOException e) {
            throw new ReportingException("Error occurred while reading yaml files in the TG home", e);
        }
    }

    /**
     * Process the test failure summary which is retrieved by reading database and construct the list of
     * {@link BuildFailureSummary} to draw test failure summary chart.
     *
     * @param testFailureSummary   list of {@link TestCaseFailureResultDTO} which are retrieved by quering the databse.
     */
    private List<BuildFailureSummary> processTestFailureSummary(List<TestCaseFailureResultDTO> testFailureSummary) {
        TreeMap<String, BuildFailureSummary> testFailureSummaryMap = new TreeMap<>();
        for (TestCaseFailureResultDTO testFailure : testFailureSummary) {
            BuildFailureSummary buildFailureSummaryData = new BuildFailureSummary();
            Gson gson = new GsonBuilder().create();
            InfraCombination infraCombination = new InfraCombination();
            String testName = testFailure.getName();
            JsonElement jelem = gson.fromJson(testFailure.getInfraParameters(), JsonElement.class);
            JsonObject jobj = jelem.getAsJsonObject();
            infraCombination.setOs(StringUtil.concatStrings(jobj.get(OPERATING_SYSTEM).getAsString(), " - ",
                    jobj.get("OSVersion").getAsString()));
            infraCombination.setJdk(jobj.get(JDK).getAsString());
            infraCombination.setDbEngine(StringUtil.concatStrings(jobj.get(DATABASE_ENGINE).getAsString(), " - ",
                    jobj.get(DATABASE_ENGINE_VERSION).getAsString()));
            if (testFailureSummaryMap.containsKey(testName)) {
                testFailureSummaryMap.get(testName).getInfraCombinations().add(infraCombination);
            } else {
                List<InfraCombination> infraCombinations = new ArrayList<>();
                buildFailureSummaryData.setTestCaseName(testName);
                buildFailureSummaryData.setTestCaseDescription(testFailure.getFailureMessage());
                infraCombinations.add(infraCombination);
                buildFailureSummaryData.setInfraCombinations(infraCombinations);
                testFailureSummaryMap.put(testName, buildFailureSummaryData);
            }
        }
        return new ArrayList<>(testFailureSummaryMap.values());
    }

    /**
     * Provide test execution summary for a given build job..
     *
     * @param workspace   current workspace
     * @throws ReportingException thrown when error on getting test plan ids by reading testgrid yaml files located
     * in the current workspace.
     */
    public BuildExecutionSummary getTestExecutionSummary(String workspace) throws ReportingException {
        int passedTestPlans = 0;
        int failedTestPlans = 0;
        int skippedTestPlans = 0;
        List<String> testExecutionSummary;
        try {
            testExecutionSummary = testPlanUOW
                    .getTestExecutionSummary(FileUtil.getTestPlanIdByReadingTGYaml(workspace));
            BuildExecutionSummary testExecutionSummaryData = new BuildExecutionSummary();
            if (testExecutionSummary.isEmpty()) {
                throw new ReportingException("Couldn't find test plan status for given test plan ids");
            }

            for (String status : testExecutionSummary) {
                if (Status.SUCCESS.toString().equals(status)) {
                    passedTestPlans++;
                } else if (Status.FAIL.toString().equals(status)) {
                    failedTestPlans++;
                } else {
                    skippedTestPlans++;
                }
            }
            testExecutionSummaryData.setPassedTestPlans(passedTestPlans);
            testExecutionSummaryData.setFailedTestPlans(failedTestPlans);
            testExecutionSummaryData.setSkippedTestPlans(skippedTestPlans);
            return testExecutionSummaryData;
        } catch (TestGridException e) {
            throw new ReportingException("Error occurred while reading yaml files in the TG home", e);
        }
    }

    /**
     * Provide history of the test execution summary for a given build job..
     *
     * in the current workspace.
     */
    public Map<String, BuildExecutionSummary> getTestExecutionHistory(String productId) {

        Map<String, BuildExecutionSummary> buildExecutionSummariesHistory = new TreeMap<>();

        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalDate timeZone = LocalDate.now(ZoneId.of("UTC"));
        LocalDateTime todayMidnight = LocalDateTime.of(timeZone, midnight);

        for (int i = 0; i < MAXIMUM_TIME_RANGE; i++) {
            if (TEST_EXECUTION_HISTORY_RANGE == buildExecutionSummariesHistory.size()) {
                break;
            }
            String from = todayMidnight.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String to = todayMidnight.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            TreeMap<String, TestPlan> testExecutionHistory = new TreeMap<>();
            BuildExecutionSummary buildExecutionSummary = new BuildExecutionSummary();
            List<TestPlan> filteredTestPlanHistory;

            int passedTestPlans = 0;
            int failedTestPlans = 0;
            int skippedTestPlans = 0;

            List<TestPlan> testPlanHistory = testPlanUOW.getTestExecutionHistory(productId, from, to);

            if (testPlanHistory.isEmpty()) {
                todayMidnight = todayMidnight.minusDays(1);
                continue;
            }

            for (TestPlan testplan : testPlanHistory) {
                String key = testplan.getInfraParameters();
                if (testExecutionHistory.containsKey(key)) {
                    if (testplan.getTestRunNumber() > testExecutionHistory.get(key).getTestRunNumber()) {
                        testExecutionHistory.replace(key, testplan);
                    }
                } else {
                    testExecutionHistory.put(testplan.getInfraParameters(), testplan);
                }
            }

            filteredTestPlanHistory = new ArrayList<>(testExecutionHistory.values());
            for (TestPlan testplan : filteredTestPlanHistory) {
                if (Status.SUCCESS.equals(testplan.getStatus())) {
                    passedTestPlans++;
                } else if (Status.FAIL.equals(testplan.getStatus())) {
                    failedTestPlans++;
                } else {
                    skippedTestPlans++;
                }
            }
            buildExecutionSummary.setPassedTestPlans(passedTestPlans);
            buildExecutionSummary.setFailedTestPlans(failedTestPlans);
            buildExecutionSummary.setSkippedTestPlans(skippedTestPlans);
            buildExecutionSummariesHistory.put(to, buildExecutionSummary);
            todayMidnight = todayMidnight.minusDays(1);
        }
        return buildExecutionSummariesHistory;
    }
}
