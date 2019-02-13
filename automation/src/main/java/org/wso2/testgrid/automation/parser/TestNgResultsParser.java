/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.testgrid.automation.parser;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import static org.wso2.testgrid.common.TestGridConstants.TEST_RESULTS_SCENARIO_DIR;

/**
 * Surefire reports parser implementation related to parsing testng integration
 * test results.
 * <p>
 * List of file and dir names that'll be archived are 'surefire-reports',
 * and 'automation.log' currently.
 *
 * @since 1.0.0
 */
public class TestNgResultsParser extends ResultParser {

    public static final String RESULTS_INPUT_FILE = "testng-results.xml";
    public static final String RESULTS_TEST_SUITE_FILE = "TEST-TestSuite.xml";
    private static final String[] ARCHIVABLE_FILES = new String[] { "surefire-reports", "automation.log" };
    private static final Logger logger = LoggerFactory.getLogger(TestNgResultsParser.class);
    private static final String TEST_CASE = "testcase";
    private static final String MESSAGE = "message";
    private static final String FAILED = "failure";
    private static final String SKIPPED = "skipped";
    private static final int ERROR_LINE_LIMIT = 2;

    /**
     * This constructor is used to create a {@link TestNgResultsParser} object with the
     * scenario details.
     *
     * @param testScenario TestScenario to be parsed
     * @param testLocation location of the test artifacts
     */
    public TestNgResultsParser(TestScenario testScenario, String testLocation) {
        super(testScenario, testLocation);
    }

    /**
     * <testcase name="testExecute" classname="org.wso2.testgrid.core.command.RunTestPlanCommandTest"
     * time="0.105"/>
     * <testcase name="testGetCombinations" classname="org.wso2.testgrid.infrastructure
     * .InfrastructureCombinationsProviderTest" time="0.023"/>
     * <testcase name="testValidateAddAPIsWithDifferentCase" classname="org.wso2.am.integration.tests
     * .other.APIMANAGER3226APINameWithDifferentCaseTestCase" time="0">
     * <skipped/>
     * <system-out><![CDATA[INFO  [org.wso2.carbon.automation.engine.testlisteners
     * .TestManagerListener] - =================== Running the test method org.wso2.am.integration.tests.other
     * .APIMANAGER3226APINameWithDifferentCaseTestCase.testValidateAddAPIsWithDifferentCase ===================
     * ]]></system-out>
     * </testcase>
     * <testcase name="destroy" classname="org.wso2.am.integration.tests.other
     * .APIMANAGER3226APINameWithDifferentCaseTestCase" time="9.081">
     * <failure message="Unable to get API - echo. Error: Read timed out" type="org.wso2.am.integration
     * .test.utils.APIManagerIntegrationTestException">org.wso2.am.integration.test.utils
     * .APIManagerIntegrationTestException: Unable to get API - echo. Error: Read timed out
     * at java.net.SocketInputStream.socketRead0(Native Method)
     * at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
     * at java.net.SocketInputStream.read(SocketInputStream.java:171)
     * at java.net.SocketInputStream.read(SocketInputStream.java:141)
     * at org.apache.http.impl.io.SessionInputBufferImpl.streamRead(SessionInputBufferImpl.java:136)
     * at org.apache.http.impl.io.SessionInputBufferImpl.fillBuffer(SessionInputBufferImpl.java:152)
     * </failure>
     * </testcase>
     * one test testcase element == one testgrid testcase.
     *
     * @throws ResultParserException parsing error
     */
    @Override
    public void parseResults() throws ResultParserException {
        Path dataBucket = DataBucketsHelper.getOutputLocation(testScenario.getTestPlan());
        dataBucket = Paths.get(dataBucket.toString(), TestGridConstants.TEST_RESULTS_DIR,
                testScenario.getOutputDir(), TEST_RESULTS_SCENARIO_DIR, testScenario.getName());
        logger.info("Path: " + dataBucket.toString());
        logger.info(testScenario.getName());
        Set<Path> inputFiles = getResultInputFiles(dataBucket);

        Path outputLocation = DataBucketsHelper.getOutputLocation(testScenario.getTestPlan());
        outputLocation = Paths.get(outputLocation.toString(), TestGridConstants.TEST_RESULTS_DIR,
                testScenario.getOutputDir(), testScenario.getName());

        logger.info("Found TEST-TestSuite.xml result files at: " + inputFiles.stream().map
                (outputLocation::relativize).collect(Collectors.toSet()));
        for (Path resultsFile : inputFiles) {
            try (final InputStream stream = Files.newInputStream(resultsFile, StandardOpenOption.READ)) {
                logger.info("Processing results file: " + outputLocation.relativize(resultsFile));
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "File content: " + new String(Files.readAllBytes(resultsFile), StandardCharsets.UTF_8));
                }

                final XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(stream);
                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                        StartElement startElement = event.asStartElement();
                        if (startElement.getName().getLocalPart().equals("testcase")) {
                            final String classNameStr = getClassName(startElement);
                            List<TestCase> testCases = getTestCasesFor(classNameStr, eventReader);
                            if (logger.isDebugEnabled()) {
                                logger.debug(String.format("Found %s test cases in class '%s'", testCases.size(),
                                        classNameStr));
                            }
                            testCases.stream().forEachOrdered(tc -> testScenario.addTestCase(tc));
                        }
                    }
                }
                //start processing duplicate testcase names
                List<TestCase> testCases = testScenario.getTestCases();
                Map<String, TestCase> finalTestCases = new HashMap<>();
                for (TestCase testCase : testCases) {
                    int suffix = 1;
                    String testCaseName = testCase.getName();
                    while (finalTestCases.containsKey(testCase.getName())) {
                        testCase.setName(StringUtil
                                .concatStrings(testCaseName, "#data_provider_", suffix));
                        suffix++;
                    }
                    finalTestCases.put(testCase.getName(), testCase);
                }
                logger.info(String.format("Found total of %s test cases. %s test cases has failed.", testScenario
                                .getTestCases().size(),
                        testScenario.getTestCases().stream().filter(tc -> Status.FAIL.equals(tc.getStatus())).count()));
            } catch (IOException | XMLStreamException e) {
                logger.error("Error while parsing testng-results.xml at " + resultsFile + " for " +
                        testScenario.getName(), e);
            }
        }
    }

    /**
     * Read the name attribute from the classElement input.
     *
     * @param classElement the class element
     * @return the name attribute
     */
    private String getClassName(StartElement classElement) {
        String classNameStr = "unknown";
        final Iterator attributes = classElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute att = (Attribute) attributes.next();
            if (att.getName().getLocalPart().equals("classname")) {
                String[] split = att.getValue().split("\\.");
                //get the class name from fully qualified class name
                classNameStr = split[split.length - 1];
            }
            if (att.getName().getLocalPart().equals("name")) {
                classNameStr = StringUtil
                        .concatStrings(classNameStr, "#", att.getValue());
            }
        }
        return classNameStr;
    }

    /**
     * Searches the child elements of class element for test-methods where
     * status == !PASS.
     *
     * @param classNameStr class name
     * @param eventReader  XMLEventReader
     * @return true if all test-methods has PASS status, false otherwise.
     * @throws XMLStreamException {@link XMLStreamException}
     */
    private List<TestCase> getTestCasesFor(String classNameStr, XMLEventReader eventReader) throws XMLStreamException {
        List<TestCase> testCases = new ArrayList<>();
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.getEventType() == XMLStreamConstants.END_ELEMENT && TEST_CASE
                    .equals(event.asEndElement().getName().getLocalPart())) {
                TestCase testCase = buildTestCase(classNameStr, Status.SUCCESS, "");
                testCases.add(testCase);
                break;
            }
            if (event.getEventType() == XMLStreamConstants.START_ELEMENT && SKIPPED
                    .equals(event.asStartElement().getName().getLocalPart())) {
                TestCase testCase = buildTestCase(classNameStr, Status.SKIP, "Test Skipped");
                testCases.add(testCase);
                break;
            }
            if (event.getEventType() == XMLStreamConstants.START_ELEMENT && FAILED
                    .equals(event.asStartElement().getName().getLocalPart())) {
                String failureMessage = "unknown";
                Iterator attributes = event.asStartElement().getAttributes();
                failureMessage = readFailureMessage(eventReader);
                while (attributes.hasNext()) {
                    Attribute attribute = (Attribute) attributes.next();
                    if (MESSAGE.equals(attribute.getName().getLocalPart())) {
                        failureMessage = attribute.getValue();
                        break;
                    }
                }
                TestCase testCase = buildTestCase(classNameStr, Status.FAIL, failureMessage);
                testCases.add(testCase);
                break;
            }
        }
        return testCases;
    }

    /**
     * Reads the text content data inside the <failure></failure> element and builds the
     * error message.
     *
     * @param eventReader XMLEventReader
     * @return error message
     * @throws XMLStreamException when theres an error reading the XML events
     */
    private String readFailureMessage(XMLEventReader eventReader) throws XMLStreamException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ERROR_LINE_LIMIT; i++) {
            XMLEvent xmlEvent = eventReader.nextEvent();
            if (xmlEvent.isCharacters()) {
                builder.append(xmlEvent.asCharacters().getData());
            } else {
                break;
            }
        }
        return builder.toString();
    }

    private TestCase buildTestCase(String className, Status isSuccess, String failureMessage) {
        TestCase testCase = new TestCase();
        testCase.setTestScenario(this.testScenario);
        testCase.setName(className);
        testCase.setSuccess(isSuccess);
        testCase.setFailureMessage(failureMessage);
        return testCase;
    }

    /**
     * Searches the provided path for files named "TEST-TestSuite.xml",
     * and returns the list of paths.
     *
     * @param dataBucket the data bucket folder where build artifacts are located.
     * @return list of paths of TEST-TestSuite.xml.
     */
    private Set<Path> getResultInputFiles(Path dataBucket) {
        try {
            final Stream<Path> ls = Files.list(dataBucket);
            final Set<Path> files = ls.collect(Collectors.toSet());
            final Set<Path> inputFiles = new HashSet<>();
            for (Path file : files) {
                final Path fileName = file.getFileName();
                if (Files.isDirectory(file)) {
                    final Set<Path> anInputFilesList = getResultInputFiles(file);
                    inputFiles.addAll(anInputFilesList);
                } else if (RESULTS_TEST_SUITE_FILE.equals(fileName.toString())) {
                    inputFiles.add(file);
                }
            }
            return inputFiles;
        } catch (IOException e) {
            logger.error("Error while reading " + RESULTS_TEST_SUITE_FILE + " in " + dataBucket, e);
            return Collections.emptySet();
        }
    }

    /**
     * Persist the ARCHIVABLE_FILES into the test-scenario artifact dir.
     * These will eventually get uploaded to S3 via jenkins pipeline.
     *
     * @throws ResultParserException if a parser error occurred.
     */
    @Override
    public void archiveResults() throws ResultParserException {
        try {
            int maxDepth = 100;
            final Path outputLocation = DataBucketsHelper.getTestOutputsLocation(testScenario.getTestPlan());
            final Set<Path> archivePaths = Files.find(outputLocation, maxDepth,
                    (path, att) -> Arrays.stream(ARCHIVABLE_FILES).anyMatch(f -> f.equals
                            (path.getFileName().toString()))).collect(Collectors.toSet());

            logger.info("Found results paths at " + outputLocation + ": " + archivePaths.stream().map
                    (outputLocation::relativize).collect(Collectors.toSet()));
            if (!archivePaths.isEmpty()) {
                Path artifactPath = TestGridUtil.getTestScenarioArtifactPath(testScenario);
                if (!Files.exists(artifactPath)) {
                    Files.createDirectories(artifactPath);
                }
                logger.info("Artifact path: " + artifactPath.toString());
                for (Path filePath : archivePaths) {
                    File file = filePath.toFile();
                    File destinationFile = new File(
                            TestGridUtil.deriveScenarioArtifactPath(this.testScenario, file.getName()));
                    if (file.isDirectory()) {
                        FileUtils.copyDirectory(file, destinationFile);
                    } else {
                        FileUtils.copyFile(file, destinationFile);
                    }
                }
                Path zipFilePath = artifactPath.resolve(testScenario.getName() + TestGridConstants
                        .TESTGRID_COMPRESSED_FILE_EXT);
                Files.deleteIfExists(zipFilePath);
                FileUtil.compress(artifactPath.toString(), zipFilePath.toString());
                logger.info("Created the results archive: " + zipFilePath);
            } else {
                logger.info("Could not create results archive. No archived files with names: " + Arrays.toString
                        (ARCHIVABLE_FILES) + " were found at " + outputLocation + ".");
            }
        } catch (IOException e) {
            throw new ResultParserException("Error occurred while persisting scenario test-results." +
                    "Scenario ID: " + testScenario.getId(), e);
        }

    }
}
