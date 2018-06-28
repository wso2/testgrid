/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.testgrid.automation.parser;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.exception.JTLResultParserException;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import static org.wso2.testgrid.common.TestGridConstants.SCENARIO_RESULTS_FILTER_PATTERN;

/**
 * Parser implementation for parsing JMeter Functional Test result file.
 *
 * @since 1.0.0
 */
public class FunctionalTestResultParser extends ResultParser {

    private static final Logger logger = LoggerFactory.
            getLogger(FunctionalTestResultParser.class.getName());
    private static final long serialVersionUID = -5244808712889913949L;

    private static final String HTTP_SAMPLE_ELEMENT = "httpSample";
    private static final String SAMPLE_ELEMENT = "sample";
    private static final String FAILURE_MESSAGE_ELEMENT = "failureMessage";
    private static final String TEST_NAME_ATTRIBUTE = "lb";
    private static final String TEST_SUCCESS_ATTRIBUTE = "s";

    /**
     * This constructor creates a {@link FunctionalTestResultParser} object with the
     * test scenario and test location.
     *
     * @param testScenario The TestScenario to be parsed
     * @param testLocation The location of the test artifacts
     */
    public FunctionalTestResultParser(TestScenario testScenario, String testLocation) {
        super(testScenario, testLocation);
    }

    /**
     * Here, you pass the results, find out all the test cases that has
     * executed. Then, these test cases are inserted into the db as child
     * items of {@link TestScenario} via {@link #persistResults()}.
     *
     * Workflow:
     * <ul><li>
     *  1. build a test case for each httpSample or sample element found in JTL.
     * </li><li>
     *  2. set the failure state to true/false along with message
     * </li><li>
     *  3. Add the test case into the test scenario.
     * </li></ul>
     * @throws JTLResultParserException result parser error
     */
    @Override
    public void parseResults() throws JTLResultParserException {
        boolean failureMsgElement = false;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        String[] scenarioResultFiles = ResultParserUtil.getJTLFiles(this.testLocation);
        String testScenarioName = testScenario.getName();

        if (scenarioResultFiles.length == 0) {
            logger.warn(StringUtil.concatStrings("Unable to locate jtl files for the scenario : '",
                    testScenarioName, "' , in path : ", this.testLocation));
            return;
        }

        for (String jtlFile : scenarioResultFiles) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(testLocation, jtlFile))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Parsing scenario-results file of the TestScenario : '"
                            + testScenarioName + "' using the FunctionalTestResultParser");
                }

                XMLEventReader eventReader = factory.createXMLEventReader(inputStream);
                TestCase testCase = null;
                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    switch (event.getEventType()) {
                        case XMLStreamConstants.START_ELEMENT:
                            StartElement startElement = event.asStartElement();
                            String elementName = startElement.getName().getLocalPart();
                            if (HTTP_SAMPLE_ELEMENT.equalsIgnoreCase(elementName) ||
                                    SAMPLE_ELEMENT.equalsIgnoreCase(elementName)) {
                                testCase = this.buildTestCase(startElement);
                            } else if (FAILURE_MESSAGE_ELEMENT.equalsIgnoreCase(elementName)) {
                                failureMsgElement = true;
                            }
                            break;
                        case XMLStreamConstants.CHARACTERS:
                            Characters characters = event.asCharacters();
                            if (failureMsgElement) {
                                testCase.setFailureMessage(characters.getData());
                                failureMsgElement = false;
                            }
                            break;
                        case XMLStreamConstants.END_ELEMENT:
                            EndElement endElement = event.asEndElement();
                            String nodeName = endElement.getName().getLocalPart();
                            if (HTTP_SAMPLE_ELEMENT.equalsIgnoreCase(nodeName) ||
                                    SAMPLE_ELEMENT.equalsIgnoreCase(nodeName)) {
                                this.testScenario.addTestCase(testCase);
                            }
                            break;
                        default:
                            break;
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("End parsing scenario-results file of the TestScenario : '" + testScenarioName +
                            "' using the FunctionalTestResultParser");
                }
            } catch (XMLStreamException e) {
                throw new JTLResultParserException("Unable to parse the scenario-results file of TestScenario :" +
                        testScenarioName, e);
            } catch (FileNotFoundException e) {
                throw new JTLResultParserException("Unable to locate the scenario-results file.", e);
            } catch (IOException e) {
                throw new JTLResultParserException("Unable to close the input stream of scenario results file of " +
                        "the TestScenario : " + testScenarioName, e);
            }
        }
    }

    @Override
    public void persistResults() throws ResultParserException {
        try {
            List<String> files = FileUtil.getFilesOnDirectory(this.testLocation, SCENARIO_RESULTS_FILTER_PATTERN);
            if (!files.isEmpty()) {
                String zipFilePath = TestGridUtil.deriveScenarioArtifactPath(testScenario,
                        testScenario.getDir() + TestGridConstants.TESTGRID_COMPRESSED_FILE_EXT);
                for (String filePath : files) {
                    File file = new File(filePath);
                    File destinationFile = new File(
                            TestGridUtil.deriveScenarioArtifactPath(this.testScenario, file.getName()));
                    FileUtils.copyFile(file, destinationFile);
                }
                FileUtil.compressFiles(files, zipFilePath);
            }
        } catch (IOException | TestGridException e) {
            throw new ResultParserException("Error occurred while persisting scenario test-results." +
                    "Scenario ID: " + testScenario.getId() + ", Scenario Directory: " + testScenario.getDir(), e);
        }
    }

    private TestCase buildTestCase(StartElement sampleElement) {
        TestCase testCase = new TestCase();
        testCase.setTestScenario(this.testScenario);
        Iterator<Attribute> attributes = sampleElement.getAttributes();

        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (TEST_NAME_ATTRIBUTE.equals(attribute.getName().getLocalPart())) {
                testCase.setName(attribute.getValue());
            } else if (TEST_SUCCESS_ATTRIBUTE.equals(attribute.getName().getLocalPart())) {
                testCase.setSuccess(Boolean.valueOf(attribute.getValue()));
            }
        }
        return testCase;
    }
}

