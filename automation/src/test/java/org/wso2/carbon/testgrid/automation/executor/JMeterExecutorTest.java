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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.carbon.testgrid.automation.TestAutomationException;
import org.wso2.carbon.testgrid.automation.executor.util.ZipUtil;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Host;
import org.wso2.carbon.testgrid.common.Port;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;
import org.wso2.carbon.testgrid.common.util.EnvironmentUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Test class to test the functionality of the {@link JMeterExecutor} class.
 *
 * @since 1.0.0
 */
public class JMeterExecutorTest {

    private static final Log log = LogFactory.getLog(JMeterExecutorTest.class);
    private final String productName = "wso2is-5.3.0";
    private String distributionLocation;

    @BeforeTest
    public void setUp() throws IOException {
        // Unzip distribution
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("distributions");
        Assert.assertNotNull(resource);
        distributionLocation = Paths.get(resource.getPath(), productName).toAbsolutePath().toString();

        Path distributionZipPath = Paths.get(resource.getPath(), productName + ".zip").toAbsolutePath();
        ZipUtil.unzip(distributionZipPath.toString(), resource.getPath());

        // Set file permissions
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            Path shellScriptPath = Paths.get(distributionLocation, "bin", "wso2server.sh")
                    .toAbsolutePath();
            setPermission(shellScriptPath);
        }

        // Start server
        String execFile = SystemUtils.IS_OS_WINDOWS ?
                          Paths.get(distributionLocation, "bin", "wso2server.bat").toAbsolutePath().toString() :
                          Paths.get(distributionLocation, "bin", "wso2server.sh").toAbsolutePath().toString();
        ProcessBuilder processBuilder = new ProcessBuilder(execFile);
        processBuilder.start();

        // Wait till the distribution is started
        Callable<Boolean> serverCallable = () -> {
            try {
                new Socket("localhost", 9443);
            } catch (ConnectException e) {
                log.info("Waiting for " + productName + " server to be started...");
                return false;
            }
            return true;
        };

        Awaitility.given().await()
                .atMost(300000, TimeUnit.MILLISECONDS)
                .until(serverCallable, equalTo(true));
    }

    @DataProvider(name = "invalidJMeterHomePaths")
    public Object[][] invalidJMeterHomePaths() {
        return new String[][]{
                {""}
        };
    }

    @Test(description = "Tests for invalid JMETER_HOME paths.",
          dataProvider = "invalidJMeterHomePaths",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = TestGridConstants.JMETER_HOME + " environment variable not set.")
    public void initTestJMeterHomeInvalid(String jMeterHome) throws TestAutomationException {
        EnvironmentUtil.setEnvironmentVariable(TestGridConstants.JMETER_HOME, jMeterHome);
        JMeterExecutor jMeterExecutor = new JMeterExecutor();
        jMeterExecutor.init("", "");
        EnvironmentUtil.unsetEnvironmentVariable(TestGridConstants.JMETER_HOME);
    }

    @Test(description = "Tests for valid JMETER_HOME paths")
    public void initTestJMeterHomeValid() throws TestAutomationException {
        EnvironmentUtil.setEnvironmentVariable(TestGridConstants.JMETER_HOME, "JMeter Home");
        JMeterExecutor jMeterExecutor = new JMeterExecutor();
        jMeterExecutor.init("", "");
        EnvironmentUtil.unsetEnvironmentVariable(TestGridConstants.JMETER_HOME);
    }

    @Test(description = "Tests for valid JMETER_HOME not set",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "JMETER_HOME environment variable not set.")
    public void initTestJMeterHomeNotSet() throws TestAutomationException {
        JMeterExecutor jMeterExecutor = new JMeterExecutor();
        jMeterExecutor.init("", "");
    }

    @Parameters({"jmeterHome"})
    @Test(description = "Test for executing JMeter")
    public void testExecuteJMeter(String jmeterHome) throws URISyntaxException, TestAutomationException {
        // Set JMeter home
        EnvironmentUtil.setEnvironmentVariable(TestGridConstants.JMETER_HOME, jmeterHome);
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testScript = Paths.get(resource.getPath(), "SolutionPattern22", "Tests", "JMeter", "solution22",
                "src", "test", "jmeter", "Create-Test-Users.jmx").toAbsolutePath().toString();

        // Create open port list
        Port port = createPort(9443, "https");
        List<Port> portList = Collections.singletonList(port);

        // Creat host list
        Host host = createHost("localhost", "wso2is-default", portList);
        List<Host> hostList = Collections.singletonList(host);

        Deployment deployment = createDeployment("single-node", hostList);

        String testLocation = Paths.get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestExecutor testExecutor = new JMeterExecutor();
        testExecutor.init(testLocation, "solution22");
        testExecutor.execute(testScript, deployment);

        // Assert test result file
        Assert.assertTrue(Files.exists(Paths.get(resource.getPath(), "SolutionPattern22", "Tests", "Results",
                "Jmeter", "Create-Test-Users.jmx.xml")));

        // Unset JMeter home
        EnvironmentUtil.unsetEnvironmentVariable(TestGridConstants.JMETER_HOME);
    }

    @Test(description = "Test for testing JMeter execute without init",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "JMeter Executor not initialised properly.\\{ Test Name: null, " +
                                            "Test Location: null, JMeter Home: null\\}")
    public void testNoInit() throws TestAutomationException {
        TestExecutor testExecutor = new JMeterExecutor();
        testExecutor.execute("", new Deployment());
    }

    @Test(description = "Test for testing invalid JMeter files",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "Error occurred when initialising JMeter save service")
    public void testNoJMeterSaveConfig() throws TestAutomationException {
        // Set JMeter home
        EnvironmentUtil.setEnvironmentVariable(TestGridConstants.JMETER_HOME, "JMeter Home");

        TestExecutor testExecutor = new JMeterExecutor();
        testExecutor.init("Test Location", "solution22");
        testExecutor.execute("", new Deployment());

        // Unset JMeter home
        EnvironmentUtil.unsetEnvironmentVariable(TestGridConstants.JMETER_HOME);
    }

    @Parameters({"jmeterHome"})
    @Test(description = "Test for testing invalid JMeter files",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "Error occurred when loading test script.")
    public void testErrorLoadJMeterFile(String jmeterHome) throws TestAutomationException {
        // Set JMeter home
        EnvironmentUtil.setEnvironmentVariable(TestGridConstants.JMETER_HOME, jmeterHome);
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

        // Unset JMeter home
        EnvironmentUtil.unsetEnvironmentVariable(TestGridConstants.JMETER_HOME);
    }

    @Parameters({"jmeterHome"})
    @Test(description = "Test for testing JMeter files as directories",
          expectedExceptions = TestAutomationException.class,
          expectedExceptionsMessageRegExp = "Error occurred when loading test script.")
    public void testJMeterFileIsDirectory(String jmeterHome) throws TestAutomationException {
        // Set JMeter home
        EnvironmentUtil.setEnvironmentVariable(TestGridConstants.JMETER_HOME, jmeterHome);
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

        // Unset JMeter home
        EnvironmentUtil.unsetEnvironmentVariable(TestGridConstants.JMETER_HOME);
    }

    @AfterTest
    public void tearDown() throws IOException, InterruptedException {
        // Get process id
        String processIdFilePath = Paths.get(distributionLocation, "wso2carbon.pid").toAbsolutePath().toString();

        BufferedReader reader = new BufferedReader(new FileReader(processIdFilePath));
        int processID = Integer.parseInt(reader.readLine().trim());

        // Wait till the distribution is stopped
        Process process = SystemUtils.IS_OS_WINDOWS ?
                          Runtime.getRuntime().exec("taskkill /pid " + processID + " /f") :
                          Runtime.getRuntime().exec("kill -9 " + processID);
        process.waitFor();

        // Remove unzipped files
        try {
            FileUtils.deleteDirectory(new File(distributionLocation));
        } catch (IOException e) {
            // Test should not be affected due to this file deletion error.
            log.error("Error occurred in deleting " + distributionLocation, e);
        }
    }

    /**
     * Set file permissions for operating systems that support POSIX.
     *
     * @param filePath path of the file to set permissions
     * @throws IOException thrown when error on setting file permissions
     */
    private void setPermission(Path filePath) throws IOException {
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);

        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OTHERS_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);

        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);

        Files.setPosixFilePermissions(filePath, permissions);
    }

    /**
     * Creates and returns a {@link Deployment} for the given name.
     *
     * @param name  deployment name
     * @param hosts list of hosts for the deployment
     * @return an instance of {@link Deployment}
     */
    private Deployment createDeployment(String name, List<Host> hosts) {
        Deployment deployment = new Deployment();
        deployment.setName(name);
        deployment.setHosts(hosts);
        return deployment;
    }

    /**
     * Creates and returns a {@link Port} instance for the given port number and protocol.
     *
     * @param portNumber port number
     * @param protocol   protocol of the port
     * @return an instance of {@link Port}
     */
    private Port createPort(int portNumber, String protocol) {
        Port port = new Port();
        port.setPortNumber(portNumber);
        port.setProtocol(protocol);
        return port;
    }

    /**
     * Creates and returns a {@link Host} instance for the given IP address, label and ports.
     *
     * @param ip    IP address of the Host
     * @param label label of the Host
     * @param ports ports of the Host
     * @return an instance of {@link Host}
     */
    private Host createHost(String ip, String label, List<Port> ports) {
        Host host = new Host();
        host.setIp(ip);
        host.setPorts(ports);
        host.setLabel(label);
        return host;
    }
}
