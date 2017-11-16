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

package org.wso2.carbon.testgrid.automation.file;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.TestAutomationException;
import org.wso2.carbon.testgrid.automation.beans.Test;
import org.wso2.carbon.testgrid.automation.beans.TestNGTest;
import org.wso2.carbon.testgrid.automation.file.common.TestReader;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for reading testNG tests from test jars.
 *
 * Test jars are jars with dependencies that contain the test classes + testng.xml
 */
public class TestNGTestReader implements TestReader {

    private static final Log log = LogFactory.getLog(JMeterTestReader.class);
    private static final String JAR_EXTENSION = ".jar";

    /**
     * This method goes through the file structure and create an object model of the tests.
     *
     * @param file File object for the test folder.
     * @return a List of Test objects.
     */
    private List<Test> processTestStructure(File file) throws TestAutomationException {
        List<Test> testsList = new ArrayList<>();
        File tests = new File(file.getAbsolutePath());
        TestNGTest test = new TestNGTest();

        test.setTestName(file.getName());
        List<String> testNGList = new ArrayList<>();

        if (tests.exists()) {
            for (String testFile : ArrayUtils.nullToEmpty(tests.list())) {
                if (testFile.endsWith(JAR_EXTENSION)) {
                    testNGList.add(Paths.get(tests.getAbsolutePath(), testFile).toString());
                }
            }
        }

        Collections.sort(testNGList);
        test.setTestNGJars(testNGList);
        testsList.add(test);

        return testsList;
    }

    @Override
    public List<Test> readTests(String testLocation) throws TestAutomationException {
        return processTestStructure(new File(testLocation));
    }
}
