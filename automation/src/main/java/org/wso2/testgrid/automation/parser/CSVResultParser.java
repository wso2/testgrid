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

import org.wso2.testgrid.automation.exception.CSVResultParserException;
import org.wso2.testgrid.common.TestScenario;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Result parser abstraction to handle CSV results
 *
 * @since 1.0.0
 */
public abstract class CSVResultParser extends ResultParser {

    static final String RESULT_LOCATION = "results";

    CSVResultParser(TestScenario testScenario, String testLocation) {
        super(testScenario, testLocation);
    }

    CSVResultParser() {

    }

    @Override
    public boolean canParse(TestScenario testScenario, String testLocation) throws CSVResultParserException {
        testLocation = testLocation.concat(File.separator).concat(RESULT_LOCATION);
        return Files.exists(Paths.get(testLocation)) && Files.isDirectory(Paths.get(testLocation));
    }
}
