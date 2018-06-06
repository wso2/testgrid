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
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.LambdaExceptionUtils;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * CSV Result parser implementation related to parsing Performance testing results.
 *
 * @since 1.0.0
 */
public class PerformanceTestCSVParser extends CSVResultParser {

    private static final String RESULT_FILE = "summary.csv";
    private static final String CSV_EXTENTION = ".csv";
    private static final String PNG_EXTENTION = ".png";

    public PerformanceTestCSVParser(TestScenario testScenario, String testLocation) {
        super(testScenario, testLocation);
    }

    public PerformanceTestCSVParser() {}

    @Override
    public boolean canParse(TestScenario testScenario, String testLocation) throws CSVResultParserException {
        boolean canParse = super.canParse(testScenario, testLocation);
        testLocation = testLocation.concat(File.separator).concat(CSVResultParser.RESULT_LOCATION).
                concat(File.separator).concat(RESULT_FILE);
        return canParse && Files.exists(Paths.get(testLocation)) && Files.isRegularFile(Paths.get(testLocation));
    }


    @Override
    public void parseResults() throws CSVResultParserException {
        //This result parser will copy the required test artifacts to the TestPlan workspace to be used by the
        //Report generation module.
        this.testLocation = testLocation.concat(File.separator).concat(CSVResultParser.RESULT_LOCATION);
        try {
            Path testRunWorkspace = Paths.get(TestGridUtil.getTestGridHomePath()).
                    resolve(TestGridUtil.getTestRunWorkspace(testScenario.getTestPlan()));
            Path path1 = Paths.get(this.testLocation);

            ArrayList<Path> copyingFiles = new ArrayList<>();
            Files.newDirectoryStream(path1, entry -> entry.toString()
                    .endsWith(CSV_EXTENTION)).forEach(copyingFiles::add);
            Files.newDirectoryStream(path1, entry -> entry.toString()
                    .endsWith(PNG_EXTENTION)).forEach(copyingFiles::add);

            //Copy Files to the test run workspace
            copyingFiles.forEach(LambdaExceptionUtils.rethrowConsumer(path -> {
                Files.copy(path, testRunWorkspace.resolve(Paths.get(path.getFileName().toString())));
            }));

        } catch (TestGridException e) {
            throw new CSVResultParserException("Error occurred while obtaining the Test run " +
                    "workspace from test plan ", e);
        } catch (IOException e) {
            throw new CSVResultParserException("Error occurred while copying the test artifacts ", e);
        }
    }
}
