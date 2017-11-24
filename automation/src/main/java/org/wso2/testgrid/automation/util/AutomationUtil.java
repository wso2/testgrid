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
package org.wso2.testgrid.automation.util;

import org.wso2.testgrid.automation.Test;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.reader.TestReader;
import org.wso2.testgrid.automation.reader.TestReaderFactory;
import org.wso2.testgrid.common.TestScenario;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class to handle test automation related operations.
 *
 * @since 1.0.0
 */
public class AutomationUtil {

    /**
     * This method goes thorough every type of test in the folder structure and returns a list of tests
     * with common test interface.
     *
     * @param testLocation The file path of the test location.
     * @param scenario     test scenario to retrieve the associated tests
     * @return a list of {@link Test} instances
     * @throws TestAutomationException thrown when an error on reading tests
     */
    public static List<Test> getTests(String testLocation, TestScenario scenario) throws TestAutomationException {
        Path testLocationPath = Paths.get(testLocation);
        List<Test> testList = new ArrayList<>();

        if (Files.exists(testLocationPath)) {
            TestScenario.TestEngine testType = scenario.getTestEngine();
            Optional<TestReader> testReader = TestReaderFactory.getTestReader(testType);

            if (testReader.isPresent()) {
                List<Test> tests = testReader.get().readTests(testLocation, scenario);
                testList.addAll(tests);
            }
        }
        return testList;
    }
}
