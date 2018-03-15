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
package org.wso2.testgrid.automation.executor;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Test class to test the functionality of the {@link JMeterExecutor} class.
 *
 * @since 1.0.0
 */
public class JMeterExecutorTest {

    @Mock
    TestScenarioUOW testScenarioUOW;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @DataProvider(name = "invalidJMeterFiles")
    public Object[][] invalidJMeterFiles() {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testPath = Paths.get(resource.getPath(), "SolutionPattern22", "Tests", "jmeter").toAbsolutePath()
                .toString();
        String testLocation = Paths.get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();

        return new String[][] {
                {
                        testLocation, Paths.get(testPath, "empty.jmx").toAbsolutePath().toString(),
                        "JMeter test plan is empty."
                },
                {
                        testLocation, Paths.get(testPath, "invalid.jmx").toAbsolutePath().toString(),
                        "Error occurred when loading test script."
                }
        };
    }

    @Test(description = "Test for executing a valid JMeter test")
    public void testExecuteJMeter() throws URISyntaxException, TestAutomationException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testLocation = Paths.get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestExecutor testExecutor = new JMeterExecutor(testScenarioUOW);
        testExecutor.init(testLocation, "solution22", null);
        //        testExecutor.execute(testScript, new Deployment());

        // Assert if record exists in database
        // TODO: Do above assertion
    }

    @Test(description = "Test for executing JMeter invalid test files.",
          dataProvider = "invalidJMeterFiles")
    public void testExecuteInvalidTest(String testLocation, String scriptPath, String exceptionMessage)
            throws URISyntaxException {
        try {
            TestExecutor testExecutor = new JMeterExecutor(testScenarioUOW);
            testExecutor.init(testLocation, "solution22", null);
            testExecutor.execute(scriptPath, new DeploymentCreationResult());
        } catch (TestAutomationException e) {
            Assert.assertEquals(e.getMessage(), exceptionMessage);
            return;
        }
        Assert.fail(); // Exceptions are expected.
    }

    @Test(description = "Test for testing JMeter execute without init",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "JMeter Executor not initialised properly.\\{ Test Name: null, " +
                  "Test Location: null\\}")
    public void testNoInit() throws TestAutomationException {
        TestExecutor testExecutor = new JMeterExecutor(testScenarioUOW);
        testExecutor.execute("Script", new DeploymentCreationResult());
    }

    @Test(description = "Test for testing invalid JMeter files",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "Error occurred when loading test script.")
    public void testErrorLoadJMeterFile() throws TestAutomationException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testScript = Paths
                .get(resource.getPath(), "SolutionPattern22", "Tests", "JMeter", "solution22", "jmeter", "nofile.jmx")
                .toAbsolutePath().toString();

        String testLocation = Paths.get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestExecutor testExecutor = new JMeterExecutor(testScenarioUOW);
        testExecutor.init(testLocation, "solution22", null);
        testExecutor.execute(testScript, new DeploymentCreationResult());
    }

    @Test(description = "Test for testing JMeter files as directories",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "Error occurred when loading test script.")
    public void testJMeterFileIsDirectory() throws TestAutomationException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testScript = Paths
                .get(resource.getPath(), "SolutionPattern22", "Tests", "JMeter", "solution22", "jmeter")
                .toAbsolutePath().toString();

        String testLocation = Paths
                .get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestExecutor testExecutor = new JMeterExecutor(testScenarioUOW);
        testExecutor.init(testLocation, "solution22", null);
        testExecutor.execute(testScript, new DeploymentCreationResult());
    }
}
