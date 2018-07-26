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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This generates the infrastructure summary report table that shows
 * the success/failure of each test-case in a given infrastructure.
 *
 * Sample summary report may look like below:
 *
 *      CentOS      Windows      MySQL
 * T1     fail      success       fail
 * T2   success     success     success
 *
 */
public class InfrastructureSummaryReporter {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureSummaryReporter.class);

    /**
     * This generates the infrastructure summary report table that shows
     * the success/failure of each test-case in a given infrastructure.
     *
     * @param testPlans the test plans for which we need to generate the summary table.
     * @return mapping between tests and its status in each infrastructure.
     */
    public Map<String, InfrastructureBuildStatus> getSummaryTable(List<TestPlan> testPlans) {
        Map<String, InfrastructureBuildStatus> testCaseInfraBuildStatusMap = new HashMap<>();
        // Mapping between test-case to infrastructure-score-map
        Map<String, InfrastructureScoreMap> testCaseInfraScores = getTCAndInfraScoreMap(
                testPlans);

        for (Map.Entry<String, InfrastructureScoreMap> testCaseEntry : testCaseInfraScores.entrySet()) {
            String testCase = testCaseEntry.getKey();
            InfrastructureScoreMap infraScoreMap = testCaseEntry.getValue();
            // first pass
            for (String infra : infraScoreMap.infraScores.keySet()) {
                final Score score = infraScoreMap.infraScores.get(infra);
                InfrastructureBuildStatus infraBuildStatus = getInfraBuildStatusFrom(testCaseInfraBuildStatusMap,
                        testCase);
                if (score.success == 0 && score.failed > 0) {
                    infraBuildStatus.addFailedInfra(infra);
                } else if (score.failed == 0 && score.success > 0) {
                    infraBuildStatus.addSuccessInfra(infra);
                }
                testCaseInfraBuildStatusMap.put(testCase, infraBuildStatus);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("the testcase vs infrastructurebuild status map: " + testCaseInfraBuildStatusMap);
        }

        return testCaseInfraBuildStatusMap;
    }

    /**
     * Get the {@link InfrastructureBuildStatus} from the testCaseInfrastructureBuildStatusMap
     * for the given testCase. If the key does not exist, then add the testCase with an empty
     * {@link InfrastructureBuildStatus}, and return.
     * <p>
     *
     * @param testCaseInfrastructureBuildStatusMap the mapping between testcase vs infrastructure
     * @param testCase given testcase
     * @return the existing @{@link InfrastructureBuildStatus} if one exists. Or an empty one.
     *
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
     * To determine the success/failure status of each test case in a given infrastructure, we provide
     * scores.
     *
     * @param testPlans the test plans
     * @return the scores
     */
    private Map<String, InfrastructureScoreMap> getTCAndInfraScoreMap(
            List<TestPlan> testPlans) {
        //map between test-case and its infrastructure-scores
        Map<String, InfrastructureScoreMap> testCaseInfrastructureScores = new HashMap<>();
        for (TestPlan testPlan : testPlans) {
            final Properties infraParams = testPlan.getInfrastructureConfig().getParameters();
            //for each test case, T1
            for (TestScenario testScenario : testPlan.getTestScenarios()) {
                for (TestCase testCase : testScenario.getTestCases()) {
                    InfrastructureScoreMap scoreMap = testCaseInfrastructureScores.get(testCase.getName());
                    if (scoreMap == null) {
                        scoreMap = new InfrastructureScoreMap();
                        testCaseInfrastructureScores.put(testCase.getName(), scoreMap);
                    }
                    addScore(infraParams, testCase.getStatus() == Status.SUCCESS, scoreMap);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Summary report generation: Infrastructure scores: " + testCaseInfrastructureScores);
        }
        return testCaseInfrastructureScores;
    }

    /**
     * Calculate the scores in each infra for a given test-case of a given test-plan.
     *
     * @param infraParams the infra params
     * @param isTestCaseSuccess given test case's success/failure status.
     * @param scoreMap          The infrastructures and their scores for a given test case
     */
    private void addScore(Properties infraParams, boolean isTestCaseSuccess, InfrastructureScoreMap scoreMap) {
        @SuppressWarnings("unchecked")
        final Enumeration<String> infraIt = (Enumeration<String>) infraParams.propertyNames();
        while (infraIt.hasMoreElements()) {
            final String infraParam = infraParams.getProperty(infraIt.nextElement());
            Score score = scoreMap.infraScores.get(infraParam);
            if (score == null) {
                score = new Score();
                scoreMap.infraScores.put(infraParam, score);
            }

            if (isTestCaseSuccess) {
                score.success++;
            } else {
                score.failed++;
            }

        }
    }

    /**
     * Bean class that keep the score of each infra for a given test case.
     */
    public static class InfrastructureScoreMap {
        /**
         * infra to score mapping.
         */
        Map<String, Score> infraScores = new HashMap<>();

        @Override
        public String toString() {
            return "InfrastructureScoreMap=" + infraScores;
        }
    }

    /**
     * Success and failure scores of a given test case on a given infrastructure.
     *
     */
    public static class Score {
        private int success = 0;
        private int failed = 0;

        @Override
        public String toString() {
            return success + "/" + failed;
        }
    }

}
