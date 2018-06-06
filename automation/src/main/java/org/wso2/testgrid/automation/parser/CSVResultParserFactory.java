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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
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
 * Factory implementation that returns a {@link ResultParser} that is compatible with parsing CSV .
 *
 * @since 1.0.0
 */
public class CSVResultParserFactory extends ResultParserFactory {

    private ServiceLoader<CSVResultParser> parsers = ServiceLoader.load(CSVResultParser.class);

    @Override
    public Optional<ResultParser> getParser(TestScenario testScenario, String testLocation)
            throws ParserInitializationException, ResultParserException {

        for (CSVResultParser parser : parsers) {
            try {
                if (parser.canParse(testScenario, testLocation)) {
                    Constructor<? extends ResultParser> constructor = parser.getClass()
                            .getConstructor(TestScenario.class, String.class);
                    return Optional.of(constructor.newInstance(testScenario, testLocation));
                }
            } catch (IllegalAccessException | InstantiationException
                    | NoSuchMethodException | InvocationTargetException e) {
                throw new ParserInitializationException(String.format("Error occurred while initializing " +
                                "the result parser %s", parser.getClass().toString()), e);
            }
        }
        throw new ResultParserException(String.format(" Unable to find a matching Result Parser for " +
                "scenario%s in the location%s", testScenario.getName(), testLocation));
    }

    @Override
    public boolean canHandle(String testLocation) {
        String jtlFile;
        try {
            jtlFile = ResultParserUtil.getJTLFile(testLocation);
        } catch (JTLResultParserException e) {
            return true;
        }
        return jtlFile == null;
    }
}
