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
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.dto.TestCaseFailureResultDTO;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.reporting.model.email.TestExecutionSummary;
import org.wso2.testgrid.reporting.model.email.TestFailureSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * This class is responsible for providing required data in order to  generate email graphs.
 *
 * @since 1.0.0
 */
public class GraphDataProvider {
    private TestPlanUOW testPlanUOW;
    private int passedTestCases = 0;
    private int failedTestCases = 0;
    private int skippedTestCases = 0;

    public GraphDataProvider() {
        this.testPlanUOW = new TestPlanUOW();
    }

    public List<TestFailureSummary> getTestFailureSummary(String workspace) throws TestGridException {
        List<TestCaseFailureResultDTO> testFailureSummary = testPlanUOW
                .getTestFailureSummary(FileUtil.getTestPlanIdByReadingTGYaml(workspace));
        if (testFailureSummary.isEmpty()) {
            throw new TestGridException("Couldn't find test case data for given test plan ids");
        }
        return processTestFailureSummary(testFailureSummary);
    }

    private List<TestFailureSummary> processTestFailureSummary(List<TestCaseFailureResultDTO> testFailureSummary) {
        TreeMap<String, TestFailureSummary> testFailureSummaryMap = new TreeMap<>();
        for (TestCaseFailureResultDTO testFailure : testFailureSummary) {
            TestFailureSummary testFailureSummaryData = new TestFailureSummary();
            Gson gson = new GsonBuilder().create();
            InfraCombination infraCombination = new InfraCombination();
            String testName = testFailure.getTestName();
            JsonElement jelem = gson.fromJson(testFailure.getInfraParameters(), JsonElement.class);
            JsonObject jobj = jelem.getAsJsonObject();
            infraCombination.setOs(StringUtil
                    .concatStrings(jobj.get("OS").getAsString(), " - ", jobj.get("OSVersion").getAsString()));
            infraCombination.setJdk(jobj.get("JDK").getAsString());
            infraCombination.setDbEngine(StringUtil.concatStrings(jobj.get("DBEngine").getAsString(), " - ",
                    jobj.get("DBEngineVersion").getAsString()));
            infraCombination.setOs(jobj.get("OS").getAsString());
            if (testFailureSummaryMap.containsKey(testName)) {
                testFailureSummaryMap.get(testName).getInfraCombinations().add(infraCombination);
            } else {
                testFailureSummaryData.setTestCaseName(testName);
                testFailureSummaryData.setTestCaseDescription(testFailure.getFailureMessage());
                testFailureSummaryData.setInfraCombinations(Collections.singletonList(infraCombination));
                testFailureSummaryMap.put(testName, testFailureSummaryData);
            }
        }
        return new ArrayList<>(testFailureSummaryMap.values());
    }

    public TestExecutionSummary getTestExecutionSummary(String workspace) throws TestGridException {
        List<String> testExecutionSummary = testPlanUOW
                .getTestExecutionSummary(FileUtil.getTestPlanIdByReadingTGYaml(workspace));
        TestExecutionSummary testExecutionSummaryData = new TestExecutionSummary();
        if (testExecutionSummary.isEmpty()) {
            throw new TestGridException("Couldn't find test plan status for given test plan ids");
        }

        for (String status : testExecutionSummary) {
            if (Status.SUCCESS.toString().equals(status)) {
                this.passedTestCases++;
            } else if (Status.FAIL.toString().equals(status)) {
                this.failedTestCases++;
            } else if (Status.SKIP.toString().equals(status)) {
                this.skippedTestCases++;
            }
        }
        testExecutionSummaryData.setPassedTestCases(this.passedTestCases);
        testExecutionSummaryData.setFailedTestCases(this.failedTestCases);
        testExecutionSummaryData.setSkippedTestCase(this.skippedTestCases);
        return testExecutionSummaryData;
    }
}
