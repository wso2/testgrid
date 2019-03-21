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
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.common.TestScenario;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CSV Result parser implementation related to parsing Performance testing results.
 *
 * @since 1.0.0
 */
public class PerformanceTestCSVParser extends ResultParser {

    private static final String RESULT_FILE = "summary.csv";
    private static final String CSV_EXTENTION = ".csv";
    private static final String PNG_EXTENTION = ".png";
    private static final String RESULT_LOCATION = "results";

    /**
     * This constructor is used to create a {@link PerformanceTestCSVParser} object with the
     * scenario details.
     *  @param testScenario TestScenario to be parsed
     * @param testLocation location of the test artifacts
     */
    public PerformanceTestCSVParser(TestScenario testScenario, Path testLocation) {
        super(testScenario, testLocation, new String[] {"*" + CSV_EXTENTION, "*" + PNG_EXTENTION});
    }
    
    @Override
    public void parseResults() throws CSVResultParserException {
        //this parser reads the csv file and then set the data to the scenario
        Path workspace = this.testResultsLocation.resolve(RESULT_LOCATION);
        File file = workspace.resolve(RESULT_FILE).toFile();
        List<List<String>> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)
                , StandardCharsets.UTF_8));) {
            String line = "";
            while (null != (line = reader.readLine())) {
                data.add(Arrays.asList(line.split(",")));
            }
            //read graphs
            ArrayList<String> imageFiles = new ArrayList<>();
            Files.newDirectoryStream(workspace, entry -> entry.toString().endsWith(PNG_EXTENTION))
                    .forEach(path -> imageFiles.add(path.toString()));
            this.testScenario.setPerformanceTestResults(data);
            this.testScenario.setSummaryGraphs(imageFiles);
        } catch (FileNotFoundException e) {
            throw new CSVResultParserException("The result file is not present in the scenario workspace ", e);
        } catch (IOException e) {
            throw new CSVResultParserException("Error occurred while parsing the csv file", e);
        }
    }

    @Override
    public void archiveResults() throws ResultParserException {
        //TODO implement performance test artifact persisting
    }
}

