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

import org.wso2.testgrid.automation.exception.JTLResultParserException;
import org.wso2.testgrid.automation.exception.ParserInitializationException;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.common.TestScenario;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory to return an appropriate JMeter result parser instance.
 *
 * @since 1.0.0
 */
public class JMeterResultParserFactory extends ResultParserFactory {

    private ServiceLoader<JTLResultParser> parsers = ServiceLoader.load(JTLResultParser.class);

    /**
     * This method returns an instance of {@link ResultParser} to parse the given JMeter result file.
     *
     * @param testScenario {@link TestScenario}  TestScenario object associated with the tests
     * @param testLocation {@link String}        Location of the scenario test directory
     * @return a ResultParser {@link ResultParser} to parse the given JTL file
     * @throws ParserInitializationException {@link ParserInitializationException} when an
     *                                       error occurs while obtaining the instance of the parser
     */
    public Optional<ResultParser> getParser(TestScenario testScenario, String testLocation)
            throws ParserInitializationException {
        for (ResultParser parser : parsers) {
            try {
                if (parser.canParse(testScenario, testLocation)) {
                    Constructor<? extends ResultParser> constructor = parser.getClass()
                            .getConstructor(TestScenario.class, String.class);
                    return Optional.of(constructor.newInstance(testScenario, testLocation));
                }
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new ParserInitializationException(String.format("Error occurred while " +
                        "initializing the Result Parser %s", parser.getClass().toString()), e);
            } catch (ResultParserException e) {
                throw new ParserInitializationException("Error occurred while checking " +
                        "the parser compatibility for Result Parser :" + parser.getClass().toString(), e);
            }
        }
        throw new ParserInitializationException(String.format("Unable to find a matching Parser " +
                "for the testScenario :%sIn test location :%s", testScenario.getName(), testLocation));
    }

    @Override
    public boolean canHandle(String testLocation) {
        //Can parse if there is JTL file generated after the test execution in workspace
        String jtlFile;
        try {
            jtlFile = ResultParserUtil.getJTLFile(testLocation);
        } catch (JTLResultParserException e) {
            return false;
        }
        return jtlFile != null;
    }
}
