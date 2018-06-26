/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.testgrid.automation.reader;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.Test;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.TestEngine;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestScenario;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reader for reading Jmeter test files of the standard maven structure.
 */
public class JMeterTestReader implements TestReader {

    private static final Logger logger = LoggerFactory.getLogger(JMeterTestReader.class);
    private static final String JMETER_TEST_PATH = "jmeter";

    /**
     * This method goes through the file structure and create an object model of the tests.
     *
     * @param file     {@link File} File object for the test folder
     * @param scenario {@link TestScenario} test scenario associated with the test
     * @return a list of {@link Test} instances
     */
    private List<Test> processTestStructure(File file, TestScenario scenario) throws TestAutomationException {
        List<Test> testsList = new ArrayList<>();
        Path scenarioScriptLocation = Paths.get(file.getAbsolutePath(), TestGridConstants.SCENARIO_SCRIPT);
        if (Files.exists(scenarioScriptLocation) && !Files.isDirectory(scenarioScriptLocation)) {
            File tests = new File(Paths.get(file.getAbsolutePath(), JMETER_TEST_PATH).toString());
            if (tests.exists()) {
                List<String> scripts = Arrays.asList(ArrayUtils.nullToEmpty(tests.list()));
                List<String> scriptList = new ArrayList<>();
                scriptList.add(scenarioScriptLocation.toString());
                Test test = new Test(scenario.getName(), TestEngine.JMETER, scriptList, scenario);

                scripts.stream()
                        .filter(x -> x.endsWith(TestGridConstants.SHELL_SUFFIX) && x.contains(
                                TestGridConstants.PRE_STRING))
                        .map(x -> Paths.get(tests.getAbsolutePath(), x).toString())
                        .findFirst()
                        .ifPresent(test::setPreScenarioScript);

                scripts.stream()
                        .filter(x -> x.endsWith(TestGridConstants.SHELL_SUFFIX) && x.contains(
                                TestGridConstants.POST_STRING))
                        .map(x -> Paths.get(tests.getAbsolutePath(), x).toString())
                        .findFirst()
                        .ifPresent(test::setPostScenarioScript);
                testsList.add(test);
            }
        } else {
            logger.error("Scenario script file (" + TestGridConstants.SCENARIO_SCRIPT + ") was not found in " + file.getAbsolutePath());
        }
        return testsList;
    }

    /**
     * This method initiates the test reading process.
     *
     * @param testLocation location of the tests as a String.
     * @return the list of tests.
     * @throws TestAutomationException when an error occurs while reading the tests.
     */
    @Override
    public List<Test> readTests(String testLocation, TestScenario scenario) throws TestAutomationException {
        return processTestStructure(new File(testLocation), scenario);
    }
}
