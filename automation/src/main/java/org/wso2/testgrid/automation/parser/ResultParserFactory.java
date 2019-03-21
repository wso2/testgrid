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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.testgrid.automation.parser;

import org.wso2.testgrid.automation.TestEngine;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.util.DataBucketsHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.wso2.testgrid.common.TestGridConstants.TEST_TYPE_FUNCTIONAL;
import static org.wso2.testgrid.common.TestGridConstants.TEST_TYPE_PERFORMANCE;

/**
 * Abstract factory class that defines the common functionalities of factory implementations
 *
 * @since 1.0.0
 */
public class ResultParserFactory {

    /**
     * This method should return the appropriate {@link ResultParser} implementation that can handle
     * the given test scenario and the artifacts produced in the test location after the test execution.
     *
     * @param testPlan     TestPlan being parsed
     * @param testScenario specific scenario being parsed.
     * @return {@link ResultParser} implementation that can parse the give scenario
     */
    public static Optional<ResultParser> getParser(TestPlan testPlan, TestScenario testScenario,
                                                   ScenarioConfig scenarioConfig) {
        // ex. $testgrid_home/$job_name/data-bucket/test-outputs/$scenario_outputdir/scenarios/$scenario_name
        Path testResultsLocation = DataBucketsHelper.getTestOutputsLocation(testScenario.getTestPlan());
        testResultsLocation = Paths.get(testResultsLocation.toString(), testScenario.getOutputDir(),
                TestGridConstants.TEST_RESULTS_SCENARIO_DIR, testScenario.getName());

        ResultParser resultParser = null;
        final String testType =
                Optional.ofNullable(scenarioConfig.getTestType()).orElse(TestEngine.TESTNG.toString());
        if (TestEngine.JMETER.toString().equalsIgnoreCase(testType) ||
                TEST_TYPE_FUNCTIONAL.equalsIgnoreCase(testType)) {
            resultParser = new JMeterTestResultParser(testScenario, testResultsLocation);
        } else if (TEST_TYPE_PERFORMANCE.equalsIgnoreCase(testType)) {
            resultParser = new PerformanceTestCSVParser(testScenario, testResultsLocation);
        } else if (TestEngine.TESTNG.toString().equalsIgnoreCase(testType)) {
            resultParser = new TestNgResultsParser(testScenario, testResultsLocation);
        }
        return Optional.ofNullable(resultParser);
    }
}

