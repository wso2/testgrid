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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.exception.JTLResultParserException;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestScenario;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

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

    @Override
    public void parseResults() throws JTLResultParserException {
        boolean failureMsgElement = false;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        String scenarioResultFile = ResultParserUtil.getJTLFile(this.testLocation);
        String testScenarioName = testScenario.getName();
        try (InputStream inputStream = new FileInputStream(scenarioResultFile)) {
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

