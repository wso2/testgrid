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


import org.wso2.testgrid.common.TestScenario;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Factory to return an appropriate JMeter result parser instance.
 *
 * @since 1.0.0
 */
public class JMeterResultParserFactory {

    /**
     * This method returns an instance of {@link JMeterResultParser} to parse the given JMeter result file.
     *
     * @param testScenario {@link TestScenario}  TestScenario object associated with the tests
     * @param testLocation {@link String}        Location of the scenario test directory
     * @return a JMeterResultParser {@link JMeterResultParser} to parse the given JTL file
     * @throws JMeterResultParserException {@link JMeterResultParserException} when an error occurs while obtaining the
     * instance of the parser
     */
    public static Optional<JMeterResultParser> getParser(TestScenario testScenario, String testLocation) throws
            JMeterResultParserException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        InputStream inputStream = null;
        String testScenarioName = testScenario.getName();
        try {
            String scenarioResultFile = JMeterParserUtil.getJTLFile(testLocation);
            inputStream = new FileInputStream(scenarioResultFile);
            XMLEventReader eventReader = factory.createXMLEventReader(inputStream);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch(event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();
                        FunctionalTestResultParser functionalTestResultParser =
                                new FunctionalTestResultParser(testScenario, testLocation);
                        if (functionalTestResultParser.canParse(qName)) {
                            return Optional.of(functionalTestResultParser);
                        }
                        break;
                }
            }

        } catch (XMLStreamException e) {
            throw new JMeterResultParserException("Unable to parse the scenario-results file of the test scenario : " +
                    "'" + testScenarioName + "'", e);
        } catch (FileNotFoundException e) {
            throw new JMeterResultParserException("Unable to locate the scenario-results file of the test scenario : " +
                    "'" + testScenarioName + "'", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new JMeterResultParserException("Unable to close the input stream of the" +
                            " scenario-results file for test scenario : '" + testScenarioName + "'", e);
                }
            }
        }
        return Optional.empty();
    }
}
