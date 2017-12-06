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
import org.wso2.testgrid.automation.Test;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.constants.TestGridConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reader for reading Jmeter test files of the standard maven structure.
 */
public class JMeterTestReader implements TestReader {

    private static final String JMETER_TEST_PATH = "src" + File.separator + "test" + File.separator + "jmeter";
    private static final String SHELL_SUFFIX = ".sh";
    private static final String PRE_STRING = "pre-scenario-steps";
    private static final String POST_STRING = "post-scenario-steps";

    /**
     * This method goes through the file structure and create an object model of the tests.
     *
     * @param file     File object for the test folder
     * @param scenario test scenario associated with the test
     * @return a list of {@link Test} instances
     */
    private List<Test> processTestStructure(File file, TestScenario scenario) throws TestAutomationException {
        String preScript = "";
        String postScript = "";
        List<Test> testsList = new ArrayList<>();
        File tests = new File(file.getAbsolutePath() +
                File.separator + JMETER_TEST_PATH);
        List<String> jmxList = new ArrayList<>();
        if (tests.exists()) {
            for (String script : ArrayUtils.nullToEmpty(tests.list())) {
                if (script.endsWith(TestGridConstants.JMTER_SUFFIX)) {
                    jmxList.add(tests.getAbsolutePath() + File.separator + script);
                } else if (script.endsWith(SHELL_SUFFIX) && script.contains(PRE_STRING)) {
                    preScript = tests.getAbsolutePath() + File.separator + script;
                } else if (script.endsWith(SHELL_SUFFIX) && script.contains(POST_STRING)) {
                    postScript = tests.getAbsolutePath() + File.separator + script;
                }
            }
        }
        Collections.sort(jmxList);
        Test test = new Test(scenario.getName(), TestScenario.TestEngine.JMETER, jmxList, scenario);
        test.setPreScenarioScript(preScript);
        test.setPostScenarioScript(postScript);
        testsList.add(test);
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
