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
package org.wso2.carbon.testgrid.reporting.reader;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.testgrid.reporting.ReportingException;
import org.wso2.carbon.testgrid.reporting.result.TestNGTestResult;
import org.wso2.carbon.testgrid.reporting.util.ReflectionUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class to test the functionality of the {@link XMLResultReader}.
 *
 * @since 1.0.0
 */
public class XMLResultReaderTest {

    private static final String TEST_ARTIFACT_DIR = "Tests";
    private static final String RESULTS_DIR = "Results";
    private final String XML_START_ELEMENT_NAME = "XML_START_ELEMENT_NAME";

    /**
     * Data set from the XML result file for assertion.
     */
    private String[][] expectedResults = new String[][]{
            {"PASS", "initTest()[pri:0, instance:org.wso2.carbon.automation.platform.esb.tests" +
                     ".SampleTestCase@738dc9b]", "initTest", "true", "3", "2017-11-08T17:26:31Z",
             "2017-11-08T17:26:31Z"},
            {"PASS", "testESB1()[pri:0, instance:org.wso2.carbon.automation.platform.esb.tests.SampleTestCase@738dc9b]",
             "testESB1", "false", "0", "2017-11-08T17:26:31Z", "2017-11-08T17:26:31Z"},
            {"PASS", "testESB3()[pri:0, instance:org.wso2.carbon.automation.platform.esb.tests.SampleTestCase@738dc9b]",
             "testESB3", "false", "0", "2017-11-08T17:26:31Z", "2017-11-08T17:26:31Z"},
            {"PASS", "testESB2()[pri:0, instance:org.wso2.carbon.automation.platform.esb.tests.SampleTestCase@738dc9b]",
             "testESB2", "false", "0", "2017-11-08T17:26:31Z", "2017-11-08T17:26:31Z"},
            {"PASS", "tearDown()[pri:0, instance:org.wso2.carbon.automation.platform.esb.tests.SampleTestCase@738dc9b]",
             "tearDown", "true", "0", "2017-11-08T17:26:31Z", "2017-11-08T17:26:31Z"},
            };

    /**
     * Formatted dates of the XML result file for assertion.
     */
    private String[][] expectedFormattedDates = new String[][]{
            {"08-11-2017 17:26:31"},
            {"08-11-2017 17:26:31"},
            {"08-11-2017 17:26:31"},
            {"08-11-2017 17:26:31"},
            {"08-11-2017 17:26:31"},
            };

    @Test
    public void testReadFile() throws ReportingException, NoSuchFieldException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("results");
        Assert.assertNotNull(resource);

        Path path = new File(resource.getFile()).toPath()
                .resolve(TEST_ARTIFACT_DIR)
                .resolve(RESULTS_DIR)
                .resolve("TestNG")
                .resolve("testng-results.xml");

        Map<String, Object> args = new HashMap<>();
        args.put(XML_START_ELEMENT_NAME, "test-method");
        ResultReadable xmlResultReader = new XMLResultReader(args);
        List<TestNGTestResult> testResults = xmlResultReader.readFile(path, TestNGTestResult.class);

        Assert.assertEquals(testResults.size(), 5);

        // Assert test records
        for (int i = 0; i < testResults.size(); i++) {
            TestNGTestResult testNGTestResult = testResults.get(i);
            Assert.assertEquals(getObjectFieldValue(testNGTestResult, "status"), expectedResults[i][0]);
            Assert.assertEquals(getObjectFieldValue(testNGTestResult, "signature"), expectedResults[i][1]);
            Assert.assertEquals(getObjectFieldValue(testNGTestResult, "name"), expectedResults[i][2]);
            Assert.assertEquals(getObjectFieldValue(testNGTestResult, "isConfig"), expectedResults[i][3]);
            Assert.assertEquals(getObjectFieldValue(testNGTestResult, "durationMs"), expectedResults[i][4]);
            Assert.assertEquals(getObjectFieldValue(testNGTestResult, "startedAt"), expectedResults[i][5]);
            Assert.assertEquals(getObjectFieldValue(testNGTestResult, "finishedAt"), expectedResults[i][6]);

            // Assert date format function
            Assert.assertEquals(testNGTestResult.getFormattedTimestamp(), expectedFormattedDates[i][0]);
        }
    }

    /**
     * Returns the value of the given field.
     *
     * @param instance  object to get the value for the given field
     * @param fieldName name of the field to get value from
     * @param <T>       type of the instance to get the field value from
     * @return the value of the field
     * @throws NoSuchFieldException   thrown when the given field name do not exists
     * @throws IllegalAccessException thrown when the given field cannot be accessed
     * @throws ReportingException     thrown when getting the class field from the given instance
     */
    private <T> String getObjectFieldValue(T instance, String fieldName)
            throws NoSuchFieldException, IllegalAccessException, ReportingException {
        Field privateStringField = ReflectionUtil.getClassField(instance, fieldName);
        return String.class.cast(privateStringField.get(instance));
    }
}
