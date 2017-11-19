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
package org.wso2.carbon.testgrid.automation.executor;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.testgrid.automation.TestAutomationException;
import org.wso2.carbon.testgrid.common.Deployment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * Test class to test the functionality of the {@link JMeterExecutor} class.
 *
 * @since 1.0.0
 */
public class JMeterExecutorTest {

    @DataProvider(name = "invalidJMeterFiles")
    public Object[][] invalidJMeterFiles() {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testPath = Paths.get(resource.getPath(), "SolutionPattern22", "Tests", "JMeter", "solution22",
                "src", "test", "jmeter").toAbsolutePath().toString();
        String testLocation = Paths.get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();

        return new String[][]{
                {testLocation, Paths.get(testPath, "empty.jmx").toAbsolutePath().toString(),
                 "JMeter test plan is empty."},
                {testLocation, Paths.get(testPath, "invalid.jmx").toAbsolutePath().toString(),
                 "Error occurred when loading test script."}
        };
    }

    @Test(description = "Test for executing a valid JMeter test")
    public void testExecuteJMeter() throws URISyntaxException, TestAutomationException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testScript = Paths.get(resource.getPath(), "SolutionPattern22", "Tests", "JMeter", "solution22",
                "src", "test", "jmeter", "mock.jmx").toAbsolutePath().toString();

        String testLocation = Paths.get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestExecutor testExecutor = new JMeterExecutor();
        testExecutor.init(testLocation, "solution22");
        testExecutor.execute(testScript, new Deployment());

        // Assert test result file
        Assert.assertTrue(Files.exists(Paths.get(resource.getPath(), "SolutionPattern22", "Tests", "Results",
                "Jmeter", "mock.jmx.xml")));
    }

    @Test(description = "Test for executing JMeter invalid test files.",
          dataProvider = "invalidJMeterFiles")
    public void testExecuteInvalidTest(String testLocation, String scriptPath, String exceptionMessage)
            throws URISyntaxException {
        try {
            TestExecutor testExecutor = new JMeterExecutor();
            testExecutor.init(testLocation, "solution22");
            testExecutor.execute(scriptPath, new Deployment());
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
        TestExecutor testExecutor = new JMeterExecutor();
        testExecutor.execute("Script", new Deployment());
    }

    @Test(description = "Test for testing invalid JMeter files",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "Error occurred when loading test script.")
    public void testErrorLoadJMeterFile() throws TestAutomationException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testScript = Paths.get(resource.getPath(), "SolutionPattern22", "Tests", "JMeter", "solution22",
                "src", "test", "jmeter", "nofile.jmx").toAbsolutePath().toString();

        String testLocation = Paths.get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestExecutor testExecutor = new JMeterExecutor();
        testExecutor.init(testLocation, "solution22");
        testExecutor.execute(testScript, new Deployment());
    }

    @Test(description = "Test for testing JMeter files as directories",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "Error occurred when loading test script.")
    public void testJMeterFileIsDirectory() throws TestAutomationException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testScript = Paths.get(resource.getPath(), "SolutionPattern22", "Tests", "JMeter", "solution22",
                "src", "test", "jmeter").toAbsolutePath().toString();

        String testLocation = Paths.get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestExecutor testExecutor = new JMeterExecutor();
        testExecutor.init(testLocation, "solution22");
        testExecutor.execute(testScript, new Deployment());
    }

    @Test(description = "Test for failing to create temp directory.",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "Error occurred when creating temporary directory in .{0,}")
    public void createTempDirectoryFailTest() throws TestAutomationException, IOException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        // Create test location without write permission
        // Permissions
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.GROUP_READ);

        Path testLocation = Paths.get(resource.getPath(), "temp");
        if (!Files.exists(testLocation)) {
            Files.createDirectory(testLocation);
        }
        Files.setPosixFilePermissions(testLocation, permissions);

        TestExecutor testExecutor = new JMeterExecutor();
        // Trying to create temp directory in a location without permission
        testExecutor.init(testLocation.toAbsolutePath().toString(), "solution22");
    }
}
