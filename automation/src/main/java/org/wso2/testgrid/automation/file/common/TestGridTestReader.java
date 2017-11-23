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

package org.wso2.testgrid.automation.file.common;

import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.beans.Test;
import org.wso2.testgrid.common.TestScenario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class is responsible for reading all the tests of the folder structure.
 */
public class TestGridTestReader {

    /**
     * This method goes thorugh every type of test in the folder structure and returns a list of tests
     * with common Test interface.
     *
     * @param testLocation The file path of the test location as a String.
     * @param scenario
     * @return a List of Tests.
     * @throws TestAutomationException when there is an error with test reading process.
     */
    public List<Test> getTests(String testLocation, TestScenario scenario) throws TestAutomationException {
        File tests = new File(testLocation);
        List<Test> testList = new ArrayList<>();

        if (tests.exists()) {
            TestReader testReader = TestReaderFactory.getTestReader(scenario.getTestEngine().toString()
                    .toUpperCase(Locale.ENGLISH));
            List<Test> tests1;
            if (testReader != null) {
                tests1 = testReader.readTests(testLocation, scenario);
                testList.addAll(tests1);
            }
        }

        return testList;
    }
}
