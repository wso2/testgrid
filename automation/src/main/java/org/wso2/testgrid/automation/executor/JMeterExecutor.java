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

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.Port;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestCaseUOW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Responsible for performing the tasks related to execution of single JMeter solution.
 *
 * @since 1.0.0
 */
public class JMeterExecutor extends TestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JMeterExecutor.class);
    private String testLocation;
    private String testName;
    private TestScenario testScenario;

    @Override
    public void execute(String script, Deployment deployment) throws TestAutomationException {
        StandardJMeterEngine jMeterEngine = new StandardJMeterEngine();
        if (StringUtil.isStringNullOrEmpty(testName) || StringUtil.isStringNullOrEmpty(testLocation)) {
            throw new TestAutomationException(
                    StringUtil.concatStrings("JMeter Executor not initialised properly.", "{ Test Name: ", testName,
                            ", Test Location: ", testLocation, "}"));
        }
        overrideJMeterConfig(testLocation, deployment); // Override JMeter properties for current deployment.
        //TODO change parameter replacing in jmx files to use JMeter properties file.
        try {
            script = replaceProperties(script);
        } catch (IOException e) {
            throw new TestAutomationException("Error occurred when applying parameters.", e);
        }
        JMeterUtils.initLocale();

        HashTree testPlanTree;
        try {
            testPlanTree = SaveService.loadTree(new File(script));
        } catch (IOException | IllegalArgumentException e) {
            throw new TestAutomationException("Error occurred when loading test script.", e);
        }

        Summariser summariser = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summariser = new Summariser(summariserName);
        }

        Path scriptFileName = Paths.get(script).getFileName();
        if (scriptFileName == null) {
            throw new TestAutomationException(StringUtil.concatStrings("Script file ", script, " cannot be located."));
        }

        JMeterResultCollector resultCollector = new JMeterResultCollector(summariser, testScenario);

        if (testPlanTree.getArray().length == 0) {
            throw new TestAutomationException("JMeter test plan is empty.");
        }
        testPlanTree.add(testPlanTree.getArray()[0], resultCollector);

        // Run JMeter Test
        jMeterEngine.configure(testPlanTree);
        jMeterEngine.run();
        jMeterEngine.exit();

        //Persist all test cases for scenario
        TestCaseUOW testCaseUOW = new TestCaseUOW();
        for (TestCase testCase: resultCollector.getTestCases()) {
            try {
                testCaseUOW.persistTestCase(testCase);
            } catch (TestGridDAOException e) {
                throw new TestAutomationException(StringUtil.concatStrings("Error while persisting test case ",
                        testCase.getName(), "of scenario ", testCase.getTestScenario().getName(), e));
            }
        }

        //delete temp file
        boolean delete = new File(script).delete();
        if (!delete) {
            logger.warn("Failed to delete temporary jmx file : " + script);
        }
    }

    @Override
    public void init(String testLocation, String testName, TestScenario testScenario) throws TestAutomationException {
        this.testName = testName;
        this.testLocation = testLocation;
        this.testScenario = testScenario;

        // Set JMeter home
        String jMeterHome = createTempDirectory(testLocation);
        JMeterUtils.setJMeterHome(jMeterHome);
    }

    /**
     * Creates a temporary directory in the given test location and returns the path of the created temp directory.
     *
     * @param testLocation location of the test scripts
     * @return path of the created temp directory
     * @throws TestAutomationException thrown when error on creating temp directory
     */
    private String createTempDirectory(String testLocation) throws TestAutomationException {
        try {
            Path tempDirectoryPath = Paths.get(testLocation).resolve("temp");
            Path binDirectoryPath = tempDirectoryPath.resolve("bin");
            Files.createDirectories(binDirectoryPath);

            // Copy properties files
            copyResourceFile(binDirectoryPath, "saveservice.properties");
            copyResourceFile(binDirectoryPath, "upgrade.properties");

            tempDirectoryPath.toFile().deleteOnExit();

            // Delete file on exit
            FileUtils.forceDeleteOnExit(new File(tempDirectoryPath.toAbsolutePath().toString()));

            return tempDirectoryPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new TestAutomationException(StringUtil
                    .concatStrings("Error occurred when creating temporary directory in ", testLocation), e);
        }
    }

    /**
     * Copies the given resource file to the given path.
     *
     * @param fileCopyPath path in which the file should be copied to
     * @param fileName     name of the file to be copied
     * @throws TestAutomationException thrown when error on copying the file
     */
    private void copyResourceFile(Path fileCopyPath, String fileName) throws TestAutomationException {
        Path filePath = fileCopyPath.resolve(fileName);
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (!Files.exists(filePath)) {
                Files.copy(inputStream, filePath);
            }
        } catch (IOException e) {
            throw new TestAutomationException(StringUtil
                    .concatStrings("Error occurred when copying file ", filePath.toAbsolutePath()), e);
        }
    }

    /**
     * Overrides the JMeter properties with the properties required for the current deployment.
     *
     * @param testLocation directory location of the tests
     * @param deployment   deployment details of the current pattern
     */

    private void overrideJMeterConfig(String testLocation, Deployment deployment) {
        Path path = Paths.get(testLocation, "resources", "user.properties");
        if (!Files.exists(path)) {
            logger.info("JMeter user.properties file not specified - proceeding with JMeter default properties.");
            return;
        }
        JMeterUtils.loadJMeterProperties(path.toAbsolutePath().toString());
        for (Host host : deployment.getHosts()) {
            JMeterUtils.setProperty(host.getLabel(), host.getIp());
            for (Port port : host.getPorts()) {
                JMeterUtils.setProperty(port.getProtocol(), String.valueOf(port.getPortNumber()));
            }
        }
    }

    /**
     * This method replaces the property values in the jmx files.
     *
     * @param script Path of the jmx file as a String.
     * @throws IOException when there is an error reading the file.
     */
    private String replaceProperties(String script) throws IOException, TestAutomationException {
        Path path = Paths.get(script);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            String jmx = new String(Files.readAllBytes(path), Charset.defaultCharset());
            Properties jMeterProperties = JMeterUtils.getJMeterProperties();
            Enumeration<?> enumeration = jMeterProperties.propertyNames();
            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();
                jmx = jmx.replaceAll("\\$\\{__property\\(" + name + "\\)\\}", JMeterUtils.getProperty(name).trim());
            }
            String[] split = script.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
            StringBuilder buffer = new StringBuilder();
            for (String s : split) {
                if (s.equals(split[split.length - 1])) {
                    buffer.append("tmp-");
                    buffer.append(s);

                } else {
                    buffer.append(s);
                    buffer.append(File.separator);
                }
            }
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(buffer.toString()), Charset.defaultCharset());
            writer.write(jmx);
            writer.flush();
            writer.close();
            return buffer.toString();
        } else {
            throw new TestAutomationException("Error occurred when loading test script.");
        }
    }
}
