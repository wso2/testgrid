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

package org.wso2.testgrid.reporting.summary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This generates the infrastructure summary report table that shows
 * the success/failure of each test-case in a given infrastructure.
 * <p>
 * Sample summary report may look like below:
 * <p>
 * CentOS      Windows      MySQL
 * T1     fail      success       fail
 * T2   success     success     success
 */
public class InfrastructureSummaryReporter {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureSummaryReporter.class);
    private final InfrastructureParameterUOW infrastructureParameterUOW;

    public InfrastructureSummaryReporter() {
        infrastructureParameterUOW = new InfrastructureParameterUOW();
    }

    public InfrastructureSummaryReporter(InfrastructureParameterUOW infrastructureParameterUOW) {
        this.infrastructureParameterUOW = infrastructureParameterUOW;
    }

    /**
     * This generates the infrastructure summary report table that shows
     * the success/failure of each test-case in a given infrastructure.
     *
     * @param testPlans the test plans for which we need to generate the summary table.
     * @return mapping between tests and its status in each infrastructure.
     */
    public Map<String, InfrastructureBuildStatus> getSummaryTable(List<TestPlan> testPlans)
            throws TestGridDAOException {
        Map<String, InfrastructureBuildStatus> testCaseInfraBuildStatusMap = new HashMap<>();
        // Mapping between test-case to infrastructure-score-map
        TestCaseInfrastructureScoreTable testCaseInfraScores = getTCAndInfraScoreMap(testPlans);

        for (Map.Entry<String, InfrastructureScores> testCaseEntry : testCaseInfraScores.get().entrySet()) {
            String testCase = testCaseEntry.getKey();
            InfrastructureScores infraScoreMap = testCaseEntry.getValue();
            calculateInfraStatusOf(testCase, testPlans, testCaseInfraBuildStatusMap, infraScoreMap);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("the testcase vs infrastructurebuild status map: " + testCaseInfraBuildStatusMap);
        }

        return testCaseInfraBuildStatusMap;
    }

    /**
     * Calculate the infrastructure success/failure status of the given test case.
     *
     * @param testCase                    the test case
     * @param testPlans                   test plans that contain test cases
     * @param testCaseInfraBuildStatusMap the result map
     * @param infraScores                 the success and failure counts of each infrastructure for the given test case.
     */
    private void calculateInfraStatusOf(String testCase, List<TestPlan> testPlans,
            Map<String, InfrastructureBuildStatus> testCaseInfraBuildStatusMap,
            InfrastructureScores infraScores) throws TestGridDAOException {
        boolean allSuccessInfrasFound = false;
        InfrastructureBuildStatus infraBuildStatus = getInfraBuildStatusFrom(testCaseInfraBuildStatusMap, testCase);
        if (logger.isDebugEnabled()) {
            logger.debug("The testCaseInfraBuildStatusMap: " + testCaseInfraBuildStatusMap);
            logger.debug("The infra score map: " + infraScores);
        }
        for (Map.Entry<InfrastructureParameter, Score> infraEntry : infraScores.getScores().entrySet()) {
            final InfrastructureParameter infra = infraEntry.getKey();
            final Score score = infraEntry.getValue();
            if (score.success == 0 && score.failed > 0) {
                handleBuildStatusOfFailedInfra(infra, infraScores, infraBuildStatus);
            } else if (score.failed == 0 && score.success > 0) {
                infraBuildStatus.addSuccessInfra(infra);
                allSuccessInfrasFound = true;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.info("The testCaseInfraBuildStatusMap: " + testCaseInfraBuildStatusMap);
        }

        if (allSuccessInfrasFound) {
            List<TestPlan> testPlansWithoutAllSuccessInfra = testPlans.stream()
                    .filter(tp -> infraBuildStatus.getSuccessInfra().stream()
                            .noneMatch(sip -> {
                                final Map<String, String> infraParamsStr = TestGridUtil
                                        .parseInfraParametersString(tp.getInfraParameters());
                                final InfrastructureParameter oneSubInfraParam = sip
                                        .getProcessedSubInfrastructureParameters().get(0);
                                final String s = infraParamsStr.get(oneSubInfraParam.getType());
                                return s != null && s.equals(oneSubInfraParam.getName());
                            }))
                    .collect(Collectors.toList());
            if (testPlansWithoutAllSuccessInfra.isEmpty()) {
                return;
            }

            final TestCaseInfrastructureScoreTable tcAndInfraScoreMap = getTCAndInfraScoreMap(
                    testPlansWithoutAllSuccessInfra); //todo no need to re-calc scores of all test cases
            calculateInfraStatusOf(testCase, testPlansWithoutAllSuccessInfra, testCaseInfraBuildStatusMap,
                    tcAndInfraScoreMap.get(testCase));
        }

    }

    /**
     * handle the infra build status of a given test case for a failed infra.
     * The test case must have been failed in all the test plans that contain this infra.
     *
     * @param infra            the infrastructure that has failure
     * @param infraScores      the infra scores of a given test case
     * @param infraBuildStatus infrastructure build status that needs to be populated.
     */
    private void handleBuildStatusOfFailedInfra(InfrastructureParameter infra, InfrastructureScores infraScores,
            InfrastructureBuildStatus infraBuildStatus) {
        String infraCategory = infra.getType();
        //check if rest of the infras in the same category are success
        boolean allOtherInfraInCurrentCatFailed = true;
        boolean allOtherInfraInCurrentCatSuccess = true;
        for (Map.Entry<InfrastructureParameter, Score> scoreEntry : infraScores.getScores().entrySet()) {
            if (scoreEntry.getKey().getType().equals(infraCategory) && !scoreEntry.getKey().getName()
                    .equals(infra.getName())) {
                if (scoreEntry.getValue().success > 0) {
                    allOtherInfraInCurrentCatFailed = false;
                } else if (scoreEntry.getValue().failed > 0) {
                    allOtherInfraInCurrentCatSuccess = false;
                }
            }
        }

        if (allOtherInfraInCurrentCatSuccess) {
            infraBuildStatus.addFailedInfra(infra);
            //add all the infras in all the categories
            // todo: what if there's a failure in an infra in another category?
            infraScores.getScores().entrySet().stream().filter(e -> !e.getKey().getName().equals(infra
                    .getName())).forEach(e -> infraBuildStatus.addSuccessInfra(e.getKey()));
        } else if (allOtherInfraInCurrentCatFailed) {
            infraScores.getScores().entrySet().stream().filter(e -> !e.getKey().getName().equals(infra
                    .getName())).forEach(e -> infraBuildStatus.addUnknownInfra(e.getKey()));
        }
    }

    /**
     * Get the {@link InfrastructureBuildStatus} from the testCaseInfrastructureBuildStatusMap
     * for the given testCase. If the key does not exist, then add the testCase with an empty
     * {@link InfrastructureBuildStatus}, and return.
     * <p>
     *
     * @param testCaseInfrastructureBuildStatusMap the mapping between testcase vs infrastructure
     * @param testCase                             given testcase
     * @return the existing @{@link InfrastructureBuildStatus} if one exists. Or an empty one.
     */
    private InfrastructureBuildStatus getInfraBuildStatusFrom(
            Map<String, InfrastructureBuildStatus> testCaseInfrastructureBuildStatusMap, String testCase) {
        InfrastructureBuildStatus infrastructureBuildStatus = testCaseInfrastructureBuildStatusMap.get(testCase);
        if (infrastructureBuildStatus == null) {
            infrastructureBuildStatus = new InfrastructureBuildStatus();
            testCaseInfrastructureBuildStatusMap.put(testCase, infrastructureBuildStatus);
        }
        return infrastructureBuildStatus;
    }

    /**
     * To determine the success/failure status of each test case in a given infrastructure, we set
     * scores.
     *
     * @param testPlans the test plans
     * @return the scores
     */
    private TestCaseInfrastructureScoreTable getTCAndInfraScoreMap(List<TestPlan> testPlans)
            throws TestGridDAOException {
        //map between test-case and its infrastructure-scores
        TestCaseInfrastructureScoreTable tcInfraScoreTable = new TestCaseInfrastructureScoreTable();
        for (TestPlan testPlan : testPlans) {
            final Set<InfrastructureValueSet> valueSets = infrastructureParameterUOW.getValueSet();
            final List<InfrastructureParameter> infraParams = TestGridUtil
                    .getInfraParamsOfTestPlan(valueSets, testPlan);
            //for each test case, T1
            for (TestScenario testScenario : testPlan.getTestScenarios()) {
                for (TestCase testCase : testScenario.getTestCases()) {
                    final InfrastructureScores infraScoreMap = tcInfraScoreTable.get(testCase.getName());
                    addScores(infraParams, testCase, infraScoreMap);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Summary report generation: Infrastructure scores: " + tcInfraScoreTable);
        }
        return tcInfraScoreTable;
    }

    /**
     * Calculate the scores in each infra for a given test-case of a given test-plan.
     *
     * @param infraParams   the infra params
     * @param tc            given test case.
     * @param infraScoreMap The infrastructures and their scores for a given test case
     */
    private void addScores(List<InfrastructureParameter> infraParams, TestCase tc, InfrastructureScores infraScoreMap) {
        for (InfrastructureParameter infraParam : infraParams) {
            Score score = infraScoreMap.getScore(infraParam);

            if (tc.getStatus() == Status.SUCCESS) {
                score.success++;
            } else if (tc.getStatus() == Status.FAIL) {
                score.failed++;
            } else if (tc.getStatus() == Status.SKIP) {
                score.skipped++;
            }
            //todo take care of other test statuses

        }
    }

    /**
     * Bean class that keep the score of each infra for a given test case.
     */
    public static class TestCaseInfrastructureScoreTable {
        Map<String, InfrastructureScores> tcInfraScores = new HashMap<>();

        public void add(String tc, InfrastructureParameter infra, Score score) {
            final InfrastructureScores infrastructureScores = tcInfraScores
                    .computeIfAbsent(tc, k -> new InfrastructureScores());
            infrastructureScores.putScore(infra, score);
        }

        public InfrastructureScores get(String tc) {
            return tcInfraScores.computeIfAbsent(tc, atc -> new InfrastructureScores());
        }

        public Map<String, InfrastructureScores> get() {
            return tcInfraScores;
        }

        @Override
        public String toString() {
            return "TestCaseInfrastructureScoreTable=" + tcInfraScores;
        }
    }

    /**
     * Contains the infra to score mapping.
     */
    public static class InfrastructureScores {
        Map<InfrastructureParameter, Score> scores = new HashMap<>();

        public Map<InfrastructureParameter, Score> getScores() {
            return scores;
        }

        public Score getScore(InfrastructureParameter infraParam) {
            return scores.computeIfAbsent(infraParam, ip -> new Score());
        }

        public void putScore(InfrastructureParameter infraParam, Score score) {
            scores.put(infraParam, score);
        }

        @Override
        public String toString() {
            return "InfrastructureScores{" + scores +
                    '}';
        }
    }

    /**
     * Success and failure scores of a given test case on a given infrastructure.
     */
    public static class Score {
        private int success = 0;
        private int failed = 0;
        private int skipped = 0;

        @Override
        public String toString() {
            return success + "/" + failed;
        }
    }

}
