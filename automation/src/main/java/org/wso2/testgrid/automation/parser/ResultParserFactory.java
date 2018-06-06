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

import org.wso2.testgrid.automation.exception.ParserFactoryInitializationException;
import org.wso2.testgrid.automation.exception.ParserInitializationException;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.common.TestScenario;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Abstract factory class that defines the common functionalities of factory implementations
 *
 * @since 1.0.0
 */
public abstract class ResultParserFactory {

    private static ServiceLoader<ResultParserFactory> factories = ServiceLoader.load(ResultParserFactory.class);

    /**
     * This static method is used to get the appropriate {@link ResultParserFactory} implementation from the subclasses
     * The Java service loader is used here.
     *
     * @param testLocation The location of test execution containing the result artifacts
     * @return the appropriate {@link ResultParserFactory} implementation
     * @throws ParserFactoryInitializationException throws when there is an error finding the ParserFactory
     */
    public static ResultParserFactory getFactory(String testLocation) throws ParserFactoryInitializationException {
        for (ResultParserFactory factory : factories) {
            if (factory.canHandle(testLocation)) {
                try {
                    return factory.getClass().newInstance();
                } catch (InstantiationException e) {
                    throw new ParserFactoryInitializationException("Error occurred while instantiating " +
                            "the parser factory " +
                            ": \n" + factory.getClass().toString(), e);
                } catch (IllegalAccessException e) {
                    throw new ParserFactoryInitializationException("Error occurred while instantiating " +
                            "due to illegal access of" +
                            ": \n" + factory.getClass().toString(), e);
                }
            }
        }

        throw new ParserFactoryInitializationException("Unable to find a supported Result Parser Factory " +
                "for the given result location " +
                ": \n" + testLocation);
    }

    /**
     * This method should return the appropriate {@link ResultParser} implementation that can handle
     * the given test scenario and the artifacts produced in the test location after the test execution.
     *
     * @param testScenario TestScenario object associated with the test
     * @param testLocation The location of the test execution
     * @return Returns a {@link ResultParser} implementation that could parse the given Test scenario
     * @throws ResultParserException         thrown when there is no matching result parser available
     * @throws ParserInitializationException thrown when there is an error when initializing the matching result parser
     */
    public abstract Optional<ResultParser> getParser(TestScenario testScenario, String testLocation) throws
            ResultParserException, ParserInitializationException;

    /**
     * This method must be overridden in subclass implementations to indicate if the artifacts in a given
     * location can be parsed from the current implementation.
     *
     * @param testLocation location of the test execution containing the result artifacts.
     * @return true if it can be parsed
     */
    public abstract boolean canHandle(String testLocation);
}
