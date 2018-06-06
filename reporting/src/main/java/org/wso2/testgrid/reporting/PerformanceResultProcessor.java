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
package org.wso2.testgrid.reporting;

import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.LambdaExceptionUtils;
import org.wso2.testgrid.reporting.model.performance.Column;
import org.wso2.testgrid.reporting.model.performance.ColumnHeader;
import org.wso2.testgrid.reporting.model.performance.DataSection;
import org.wso2.testgrid.reporting.model.performance.DividerSection;
import org.wso2.testgrid.reporting.model.performance.PerformanceTable;
import org.wso2.testgrid.reporting.model.performance.ResultFormatter;
import org.wso2.testgrid.reporting.model.performance.ScenarioSection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * This class contains the functionalities required to process the CSV result file and genreate the
 * Report data model.
 *
 * @since 1.0.0
 */
public class PerformanceResultProcessor {

    private static final String RESULT_FILE = "summary.csv";
    private static final String GRAPH_EXTENSION = ".png";
    private static final String IMAGES_FOLDER = "img";


    /**
     * This method goes through to find the summary csv file and then process it to generate the report sections
     * for the performance report.
     *
     * @param testScenarios   List of TestScenario objects thaw was executed to produce the results
     * @param workspace       The location where artifacts are present
     * @param resultFormatter {@link ResultFormatter} object defining the structure required
     * @return List of {@link ScenarioSection} objects that contain the data for the report
     * @throws ReportingException when there is an error during the process
     */
    public List<ScenarioSection> processWorkspace(List<TestScenario> testScenarios, Path workspace
            , ResultFormatter resultFormatter) throws ReportingException {

        List<ScenarioSection> scenarioSections = new ArrayList<>();
        for (TestScenario testScenario : testScenarios) {
            File file = workspace.resolve(RESULT_FILE).toFile(); // TODO add scenario speceific folder
            //Read the csv file line by line and store as a List of List of Strings
            String line = "";
            List<List<String>> data = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)
                    , StandardCharsets.UTF_8));) {

                Optional<String> topicString = Optional.of(reader.readLine());
                if (topicString.isPresent()) {
                    List<String> topics = Arrays.asList(topicString.get().split(","));
                    while (null != (line = reader.readLine())) {
                        data.add(Arrays.asList(line.split(",")));
                    }
                    //Create a new scenario section per test scenario
                    ScenarioSection scenarioSection = new ScenarioSection();
                    scenarioSection.setScenarioName(testScenario.getName());
                    scenarioSection.setDescription(testScenario.getDescription());
                    //get the index of the primary divider from topic list
                    int indexOf = topics.indexOf(resultFormatter.getPrimaryDivider());
                    //
                    Stack<String> dividers = new Stack<>();
                    dividers.addAll(resultFormatter.getDividers());
                    //divide the data in to groups with primary divider
                    List<String> primarySections = data.stream()
                            .map(strings -> strings.get(indexOf))
                            .distinct()
                            .collect(Collectors.toList());
                    //for each section fill the data
                    primarySections.forEach(primarySection -> {
                        Stack<String> cloneOfDividers = (Stack<String>) dividers.clone();
                        DividerSection primary = new DividerSection();
                        primary.setData(resultFormatter.getPrimaryDivider() + " : " + primarySection);
                        List<List<String>> section = data.stream()
                                .filter(strings -> strings.get(indexOf).equals(primarySection))
                                .collect(Collectors.toList());
                        ;
                        DividerSection sectionData = getSectionData(cloneOfDividers, section, topics
                                , resultFormatter);
                        primary.setChildSections(Collections.singletonList(sectionData));
                        scenarioSection.setDividerSection(primary);
                    });
                    List<String> imageList = getImagesList(workspace).stream()
                            .map(path -> path.getFileName().toString())
                            .collect(Collectors.toList());
                    scenarioSection.setChartsList(imageList);
                    scenarioSections.add(scenarioSection);
                } else {
                    throw new ReportingException("Null value encountered while reading the CSV topics list ");
                }

            } catch (IOException e) {
                throw new ReportingException("Error occurred while copying the Report assets to " +
                        "destination", e);
            }
        }
        return scenarioSections;
    }

    /**
     * This method returns the {@link DividerSection} data model after recursively processing the dividers.
     *
     * @param dividers        Stack of dividers
     * @param section         the list of data that needs to be processed into sections
     * @param topics          the topic list from csv data file
     * @param resultFormatter ResultFormatter file that defines the data table structure
     * @return a DividerSection object populated with data
     */
    private DividerSection getSectionData(Stack<String> dividers, List<List<String>> section,
                                          List<String> topics, ResultFormatter resultFormatter) {
        //create a copy of divider stack because this method will be called recursively
        Stack<String> copyOfDividers = (Stack<String>) dividers.clone();
        if (!copyOfDividers.isEmpty()) {
            //if this is the last divider in the list it is a data section containing tables
            boolean isDataSection = (copyOfDividers.size() == 1);
            String divider = copyOfDividers.pop();
            //filter distinct sections of data for each divider
            int index = topics.indexOf(divider);
            List<String> dividerSubSections = section.stream()
                    .map(strings -> strings.get(index))
                    .distinct()
                    .collect(Collectors.toList());

            if (isDataSection) {
                DividerSection dividerSection = new DividerSection();
                dividerSection.setData(divider);
                //create a new data section
                DataSection dataSection = new DataSection();
                List<PerformanceTable> performanceTableList = new ArrayList<>();
                //create performance data tables for the last divider section
                dividerSubSections.forEach(s -> {
                    List<List<String>> collect1 = section.stream()
                            .filter(strings -> strings.get(index).equals(s))
                            .collect(Collectors.toList());
                    PerformanceTable table = createPerformanceTable(topics, collect1, resultFormatter);
                    table.setDescription(s);
                    performanceTableList.add(table);
                });
                dataSection.setPerformanceTableList(performanceTableList);
                dataSection.setDescription(divider);
                dividerSection.setChildSections(null);
                dividerSection.setDataSection(dataSection);
                return dividerSection;
            } else {
                //if there are more divider sections it means data needs to be further divided
                List<DividerSection> dividerSections = new ArrayList<>();
                dividerSubSections.forEach(s -> {
                    List<List<String>> collect1 = section.stream()
                            .filter(strings -> strings.get(index).equals(s))
                            .collect(Collectors.toList());
                    //recursively set divider sections
                    DividerSection dividerSection = this.getSectionData(copyOfDividers, collect1, topics
                            , resultFormatter);
                    dividerSection.setData(s);
                    dividerSections.add(dividerSection);
                });
                DividerSection dividerSection = new DividerSection();
                dividerSection.setData(divider);
                dividerSection.setChildSections(dividerSections);
                dividerSection.setDataSection(null);
                return dividerSection;
            }
        }
        return null;
    }

    /**
     * Creates the performance table given the data and formatter data.
     *
     * @param topics          topic list of csv data
     * @param data            the data that needs to be applied to the table
     * @param resultFormatter {@link ResultFormatter} defining the structure of table
     * @return PerformanceTable instance with data populated
     */
    private PerformanceTable createPerformanceTable(List<String> topics, List<List<String>> data,
                                                    ResultFormatter resultFormatter) {
        int depth = 1;
        int colspan = 1;
        //get table structure from result formatter
        List<Column> table = resultFormatter.getTable();
        List<String> effectiveColumnLIst = new ArrayList<>();

        PerformanceTable performanceTable = new PerformanceTable();
        //fill a stack with the table data
        Stack<Column> tableStack = new Stack<>();
        tableStack.addAll(table);

        //find the column list of leaf table columns
        while (!tableStack.empty()) {
            Column column = tableStack.pop();
            if (Column.COLUMN.equals(column.getType())) {
                effectiveColumnLIst.add(column.getName());
            } else if (Column.COMMON_COLUMN.equals(column.getType())) {
                //save the depth of the whole table
                depth++;
                List<Column> columns = column.getColumns();
                if (columns != null) {
                    tableStack.addAll(columns);
                }
            }
        }
        //Preserve the original order of columns
        Collections.reverse(effectiveColumnLIst);
        //transform the column list to list with indexes
        List<Integer> indexList = effectiveColumnLIst.stream()
                .map(topics::indexOf)
                .collect(Collectors.toList());
        //filter the table data
        List<List<String>> tableData = data.stream()
                .map(strings -> indexList.stream()
                        .map(strings::get).collect(Collectors.toList()))
                .collect(Collectors.toList());
        performanceTable.setBodyData(tableData);
        //create header data
        List<List<ColumnHeader>> headerData = new ArrayList<>();
        while (depth >= 1) {
            int finalDepth = depth;
            //keep the next header row data
            List<Column> nextLevel = new ArrayList<>();
            List<ColumnHeader> headerList = new ArrayList<>();
            table.forEach(columnHeader -> {
                if (Column.COLUMN.equals(columnHeader.getType())) {
                    ColumnHeader header = new ColumnHeader();
                    header.setName(columnHeader.getName());
                    header.setRowSpan(finalDepth);
                    header.setColSpan(colspan);
                    headerList.add(header);
                } else if (Column.COMMON_COLUMN.equals(columnHeader.getType())) {
                    ColumnHeader header = new ColumnHeader();
                    header.setName(columnHeader.getName());
                    header.setColSpan(columnHeader.getColumns().size());
                    header.setRowSpan(finalDepth - 1);
                    headerList.add(header);
                    nextLevel.addAll(columnHeader.getColumns());
                }
            });
            headerData.add(new ArrayList<>(headerList));
            headerList.clear();
            table.clear();
            table.addAll(nextLevel);
            depth--;
        }
        performanceTable.setHeaderData(headerData);
        return performanceTable;
    }

    /**
     * This method copies the assets of report to the relevant location.
     *
     * @param workspace     location of the assets
     * @param reportDirPath target location where the Report is generated
     * @throws IOException thrown when there is an error copying files
     */
    void copyReportAssets(Path workspace, Path reportDirPath) throws IOException {
        Path assetDir = reportDirPath.resolve(IMAGES_FOLDER);
        ArrayList<Path> imageList = getImagesList(workspace);
        if (!Files.exists(assetDir)) {
            Files.createDirectories(assetDir);
        }
        imageList.forEach(LambdaExceptionUtils.rethrowConsumer(path -> {
            Files.copy(path, assetDir.resolve(Paths.get(path.getFileName().toString())));
        }));
    }

    /**
     * This method returns a list of image files that are in the workspace. it is useful to find the graphs that
     * are generated in the tests.
     *
     * @param workspace location of the images
     * @return List paths representing the images
     * @throws IOException thrown when there is an error reading the files
     */
    private ArrayList<Path> getImagesList(Path workspace) throws IOException {
        ArrayList<Path> imageFiles = new ArrayList<>();
        Files.newDirectoryStream(workspace, entry -> entry.toString().endsWith(GRAPH_EXTENSION))
                .forEach(imageFiles::add);
        return imageFiles;
    }
}
