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
import org.wso2.testgrid.reporting.model.email.BuildExecutionSummary;
import org.wso2.testgrid.reporting.model.email.BuildFailureSummary;

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
    private int passedTestPlans = 0;
    private int failedTestPlans = 0;
    private int skippedTestPlans = 0;

    public GraphDataProvider() {
        this.testPlanUOW = new TestPlanUOW();
    }

    /**
     * Provides the test failure summary for a given build job. Test Plan ids of the build job is retrieved by reading
     * testgrid yaml files which contains in the current workspace.
     *
     * @param workspace   current workspace
     * @throws TestGridException thrown when getting test plan ids by reading test plan yaml files
     */
    public List<BuildFailureSummary> getTestFailureSummary(String workspace) throws TestGridException {
        List<TestCaseFailureResultDTO> testFailureSummary = testPlanUOW
                .getTestFailureSummary(FileUtil.getTestPlanIdByReadingTGYaml(workspace));
        if (testFailureSummary.isEmpty()) {
            return Collections.emptyList();
        }
        return processTestFailureSummary(testFailureSummary);
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
            String testName = testFailure.getTestName();
            JsonElement jelem = gson.fromJson(testFailure.getInfraParameters(), JsonElement.class);
            JsonObject jobj = jelem.getAsJsonObject();
            infraCombination.setOs(StringUtil
                    .concatStrings(jobj.get("OS").getAsString(), " - ", jobj.get("OSVersion").getAsString()));
            infraCombination.setJdk(jobj.get("JDK").getAsString());
            infraCombination.setDbEngine(StringUtil.concatStrings(jobj.get("DBEngine").getAsString(), " - ",
                    jobj.get("DBEngineVersion").getAsString()));
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
     * @throws TestGridException thrown when error on getting test plan ids by reading testgrid yaml files located
     * in the current workspace.
     */
    public BuildExecutionSummary getTestExecutionSummary(String workspace) throws TestGridException {
        List<String> testExecutionSummary = testPlanUOW
                .getTestExecutionSummary(FileUtil.getTestPlanIdByReadingTGYaml(workspace));
        BuildExecutionSummary testExecutionSummaryData = new BuildExecutionSummary();
        if (testExecutionSummary.isEmpty()) {
            throw new TestGridException("Couldn't find test plan status for given test plan ids");
        }

        for (String status : testExecutionSummary) {
            if (Status.SUCCESS.toString().equals(status)) {
                this.passedTestPlans++;
            } else if (Status.FAIL.toString().equals(status)) {
                this.failedTestPlans++;
            } else {
                this.skippedTestPlans++;
            }
        }
        testExecutionSummaryData.setPassedTestPlans(this.passedTestPlans);
        testExecutionSummaryData.setFailedTestPlans(this.failedTestPlans);
        testExecutionSummaryData.setSkippedTestPlans(this.skippedTestPlans);
        return testExecutionSummaryData;
    }
}
