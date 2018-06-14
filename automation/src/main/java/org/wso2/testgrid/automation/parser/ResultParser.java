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

import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.common.TestScenario;

/**
 * Defines the contract for result parser implementations.
 *
 * @since 1.0.0
 */
public abstract class ResultParser {

    protected TestScenario testScenario;
    protected String testLocation;

    /**
     * Superclass implementation holds the variable values and this constructor must be called to set them
     *
     * @param testScenario TestScenario associated with the current test
     * @param testLocation Location of the tests
     */
    ResultParser(TestScenario testScenario, String testLocation) {
        this.testScenario = testScenario;
        this.testLocation = testLocation;
    }

    /**
     * This method will parse the JMeter result file.
     *
     * @throws ResultParserException {@link ResultParserException} when an error occurs while parsing the
     *                               results
     */
    public abstract void parseResults() throws ResultParserException;

    public abstract void persistResults() throws ResultParserException;
}

