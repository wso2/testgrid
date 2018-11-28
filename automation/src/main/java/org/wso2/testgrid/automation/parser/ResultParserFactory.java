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

import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ScenarioConfig;

import java.nio.file.Paths;
import java.util.Optional;

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
        String testLocation = Paths.get(testPlan.getScenarioTestsRepository(), scenarioConfig.getFile()).toString();
        ResultParser resultParser = null;
        if (TestGridConstants.TEST_TYPE_FUNCTIONAL.equals(scenarioConfig.getTestType())) {
            resultParser = new FunctionalTestResultParser(testScenario, testLocation);
        } else if (TestGridConstants.TEST_TYPE_PERFORMANCE.equals(scenarioConfig.getTestType())) {
            resultParser = new PerformanceTestCSVParser(testScenario, testLocation);
        } else if (TestGridConstants.TEST_TYPE_INTEGRATION.equals(scenarioConfig.getTestType())) {
            resultParser = new TestNgResultsParser(testScenario, testLocation);
        }
        return Optional.ofNullable(resultParser);
    }
}

