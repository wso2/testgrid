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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.DataBucketsHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Surefire reports parser implementation related to parsing testng integration
 * test results.
 *
 * @since 1.0.0
 */
public class TestNgResultsParser extends ResultParser {

    public static final String RESULTS_INPUT_FILE = "testng-results.xml";
    private static final Logger logger = LoggerFactory.getLogger(TestNgResultsParser.class);
    private static final String TOTAL = "total";
    private static final String FAILED = "failed";
    private static final String PASSED = "passed";
    private static final String SKIPPED = "skipped";
    private final XMLInputFactory factory = XMLInputFactory.newInstance();

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
     * <pre>
     * <testng-results skipped="2" failed="1" total="17" passed="14">
     *  <reporter-output>
     *  </reporter-output>
     *  <suite name="apim-automation-tests-suite-1"
     *          duration-ms="41210" started-at="2018-06-28T10:16:25Z" finished-at="2018-06-28T10:17:06Z">
     *      <test name="apim-startup-tests" duration-ms="15825"
     *          started-at="2018-06-28T10:16:50Z" finished-at="2018-06-28T10:17:06Z">
     *      <class name="org.wso2.am.integration.tests.server.mgt.APIMgtServerStartupTestCase">
     *      <test-method status="PASS" signature="setEnvironment()" name="setEnvironment" is-config="true"
     *          duration-ms="98" started-at="2018-06-28T10:16:24Z" finished-at="2018-06-28T10:16:25Z">
     *      </test-method>
     *      <test-method status="PASS" signature="testVerifyLogs()" name="testVerifyLogs" duration-ms="600"
     *          started-at="2018-06-28T10:16:50Z" description="verify server startup errors"
     *          finished-at="2018-06-28T10:16:51Z">
     *      </test-method>
     *      <test-method status="PASS" signature="disconnectFromOSGiConsole()" name="disconnectFromOSGiConsole"
     *          is-config="true" duration-ms="1" started-at="2018-06-28T10:17:01Z" finished-at="2018-06-28T10:17:01Z">
     *      </test-method>
     *      </class>
     *   </suite>
     *  </testng-results>
     * </pre>
     * <p>
     * one test class == one testgrid testcase.
     *
     * @throws ResultParserException parsing error
     */
    @Override
    public void parseResults() throws ResultParserException {
        final Path dataBucket = DataBucketsHelper.getOutputLocation(testScenario.getTestPlan());
        Set<Path> inputFiles = getResultInputFiles(dataBucket);

        for (Path resultsFile : inputFiles) {
            try (final InputStream stream = Files.newInputStream(resultsFile, StandardOpenOption.READ)) {
                final XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(stream);

                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                        StartElement startElement = event.asStartElement();
                        if (startElement.getName().getLocalPart().equals("class")) {
                            final String classNameStr = getClassName(startElement);
                            final boolean failed = hasFailedTestMethods(eventReader);
                            final TestCase testCase = buildTestCase(classNameStr, failed);
                            testScenario.addTestCase(testCase);
                        }
                    }
                }

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
            if (att.getName().getLocalPart().equals("name")) {
                classNameStr = att.getValue();
            }
        }
        return classNameStr;
    }

    /**
     * Searches the child elements of class element for test-methods where
     * status == !PASS.
     *
     * @param eventReader XMLEventReader
     * @return true if all test-methods has PASS status, false otherwise.
     * @throws XMLStreamException {@link XMLStreamException}
     */
    private boolean hasFailedTestMethods(XMLEventReader eventReader) throws XMLStreamException {
        boolean hasFailedTestMethods = false;
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.getEventType() == XMLStreamConstants.END_ELEMENT &&
                    event.asEndElement().getName().getLocalPart().equals("class")) {
                break;
            }
            if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                final StartElement element = event.asStartElement();
                if (element.getName().getLocalPart().equals("test-method")) {
                    Iterable<Attribute> testMethodAttrs = element::getAttributes; //todo
                    hasFailedTestMethods = StreamSupport.stream(testMethodAttrs.spliterator(), false)
                            .anyMatch(att -> att.getName().getLocalPart().equals("status")
                                    && !att.getValue().equals("PASS"));
                }
            }
        }
        return hasFailedTestMethods;
    }

    private TestCase buildTestCase(String className, boolean failed) {
        TestCase testCase = new TestCase();
        testCase.setTestScenario(this.testScenario);
        testCase.setName(className);
        testCase.setSuccess(!failed);
        return testCase;
    }

    /**
     *
     * Searches the provided path for files named "testng-results.xml",
     * and returns the list of paths.
     *
     * @param dataBucket the data bucket folder where build artifacts are located.
     * @return list of paths of testng-results.xml.
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
                } else if (RESULTS_INPUT_FILE.equals(fileName.toString())) {
                    inputFiles.add(file);
                }
            }

            return inputFiles;
        } catch (IOException e) {
            logger.error("Error while reading " + RESULTS_INPUT_FILE + " in " + dataBucket, e);
            return Collections.emptySet();
        }
    }

    @Override
    public void persistResults() throws ResultParserException {
        //TODO implement
    }
}

