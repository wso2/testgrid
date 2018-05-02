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
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestScenario;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Parser implementation for parsing JMeter Functional Test result file.
 */
public class FunctionalTestResultParser extends JMeterResultParser {

    private static final Logger logger = LoggerFactory.
             getLogger(FunctionalTestResultParser.class.getName());
    private static final long serialVersionUID = -5244808712889913949L;

    private static final String HTTP_SAMPLE_ELEMENT = "httpSample";
    private static final String SAMPLE_ELEMENT = "sample";
    private static final String FAILURE_MESSAGE_ELEMENT = "failureMessage";
    private static final String TEST_NAME_ATTRIBUTE = "lb";
    private static final String TEST_STATUS_ATTRIBUTE = "ec";
    private static final String TEST_STATUS_FAIL = "1";

    public FunctionalTestResultParser(TestScenario testScenario, String testLocation) {
        super(testScenario, testLocation);
    }

    @Override
    public boolean canParse(String nodeName) {
        return HTTP_SAMPLE_ELEMENT.equals(nodeName) || SAMPLE_ELEMENT.equals(nodeName);
    }

    @Override
    public void parseResults() throws JMeterResultParserException {
        boolean failureMsgElement = false;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        InputStream inputStream = null;

        String scenarioResultFile = JMeterParserUtil.getJTLFile(this.testLocation);
        String testScenarioName = testScenario.getName();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Parsing scenario-results file : '" + testScenarioName + "' using the " +
                        "FunctionalTestResultParser");
            }

            inputStream = new FileInputStream(scenarioResultFile);
            XMLEventReader eventReader = factory.createXMLEventReader(inputStream);
            TestCase testCase = null;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch(event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String elementName = startElement.getName().getLocalPart();
                        if (HTTP_SAMPLE_ELEMENT.equalsIgnoreCase(elementName) ||
                                SAMPLE_ELEMENT.equalsIgnoreCase(elementName)) {
                            testCase = this.addAttributes(startElement);
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
                logger.debug("End parsing scenario-results file of TestScenario : '" + testScenarioName +
                        "' using the FunctionalTestResultParser");
            }
        } catch (XMLStreamException e) {
            throw new JMeterResultParserException("Unable to parse the scenario-results file of TestScenario :" +
                    testScenarioName, e);
        } catch (FileNotFoundException e) {
            throw new JMeterResultParserException("Unable to locate the scenario-results file.", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new JMeterResultParserException("Unable to close the input stream of scenario results file of " +
                        "TestScenario : " + testScenarioName, e);
            }
        }
    }

    private TestCase addAttributes(StartElement startElement) {
        TestCase testCase = new TestCase();
        testCase.setTestScenario(this.testScenario);
        Iterator<Attribute> attributes = startElement.getAttributes();

        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (TEST_NAME_ATTRIBUTE.equals(attribute.getName().getLocalPart())) {
                testCase.setName(attribute.getValue());
            } else if (TEST_STATUS_ATTRIBUTE.equals(attribute.getName().getLocalPart())) {
                if (TEST_STATUS_FAIL.equals(attribute.getValue())) {
                    testCase.setSuccess(false);
                } else {
                    testCase.setSuccess(true);
                }
            }
        }
        return testCase;
    }
}
