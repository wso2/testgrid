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
import org.wso2.carbon.testgrid.reporting.result.JmeterTestResult;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * Test class to test the functionality of the {@link CSVResultReader}.
 *
 * @since 1.0.0
 */
public class CSVResultReaderTest {

    private static final String TEST_ARTIFACT_DIR = "Tests";
    private static final String RESULTS_DIR = "Results";

    private String[][] expectedResults = new String[][]{
            {"1509341756940", "411", "Create Role", "200", "OK", "Create User, Role, SP and IDP 1-1", "text",
             "true", "", "657", "834", "1", "1", "410", "0", "236"},
            {"1509341758254", "52", "Create user", "200", "OK", "Create User, Role, SP and IDP 1-1", "text", "true",
             "", "657", "859", "1", "1", "52", "0", "0"},
            {"1509341759608", "31", "AddGoogle IDP", "200", "OK", "Create User, Role, SP and IDP 1-1", "text",
             "true", "", "658", "2409", "1", "1", "31", "0", "8"},
            {"1509341760247", "19", "Register Travelocity as SP for Google", "200", "OK",
             "Create User, Role, SP and IDP 1-1", "text", "true", "", "546", "1884", "1", "1", "19", "0", "0"},
            {"1509341760869", "9", "Create SP for Google", "200", "OK", "Create User, Role, SP and IDP 1-1", "text",
             "true", "", "594", "938", "1", "1", "9", "0", "0"}
    };

    @Test
    public void testReadFile() throws ReportingException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("results");
        Assert.assertNotNull(resource);

        Path path = new File(resource.getFile()).toPath()
                .resolve(TEST_ARTIFACT_DIR)
                .resolve(RESULTS_DIR)
                .resolve("Jmeter")
                .resolve("jmeteroutput.csv");
        ResultReadable csvFileReader = new CSVResultReader();
        List<JmeterTestResult> testResults = csvFileReader.readFile(path, JmeterTestResult.class);

        Assert.assertEquals(testResults.size(), 5);

        // Test records
        for (int i = 0; i < testResults.size(); i++) {
            Assert.assertEquals(testResults.get(i).getTimestamp(), expectedResults[i][0]);
            Assert.assertEquals(testResults.get(i).getElapsed(), expectedResults[i][1]);
            Assert.assertEquals(testResults.get(i).getTestCase(), expectedResults[i][2]);
            Assert.assertEquals(testResults.get(i).getResponseCode(), expectedResults[i][3]);
            Assert.assertEquals(testResults.get(i).getResponseMessage(), expectedResults[i][4]);
            Assert.assertEquals(testResults.get(i).getThreadName(), expectedResults[i][5]);
            Assert.assertEquals(testResults.get(i).getDataType(), expectedResults[i][6]);
            Assert.assertEquals(testResults.get(i).isTestSuccess(), (boolean) Boolean.valueOf(expectedResults[i][7]));
            Assert.assertEquals(testResults.get(i).getFailureMessage(), expectedResults[i][8]);
            Assert.assertEquals(testResults.get(i).getBytes(), expectedResults[i][9]);
            Assert.assertEquals(testResults.get(i).getSentBytes(), expectedResults[i][10]);
            Assert.assertEquals(testResults.get(i).getGrpThreads(), expectedResults[i][11]);
            Assert.assertEquals(testResults.get(i).getAllThreads(), expectedResults[i][12]);
            Assert.assertEquals(testResults.get(i).getLatency(), expectedResults[i][13]);
            Assert.assertEquals(testResults.get(i).getIdleTime(), expectedResults[i][14]);
            Assert.assertEquals(testResults.get(i).getConnect(), expectedResults[i][15]);
        }
    }
}
