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

import org.wso2.testgrid.common.Column;
import org.wso2.testgrid.common.ResultFormat;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.reporting.model.performance.ColumnHeader;
import org.wso2.testgrid.reporting.model.performance.DataSection;
import org.wso2.testgrid.reporting.model.performance.DividerSection;
import org.wso2.testgrid.reporting.model.performance.PerformanceTable;
import org.wso2.testgrid.reporting.model.performance.ScenarioSection;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * This class contains the functionalities required to process the CSV result file and genreate the
 * Report data model.The {@link ResultFormat} defines the processing logic for each scenario result
 * The processed output is used to create {@link org.wso2.testgrid.reporting.model.performance.PerformanceReport}.
 *
 * @since 1.0.0
 */
public class PerformanceResultProcessor {

    private static final String RESULT_FILE = "summary.csv";
    private static final String GRAPH_EXTENSION = ".png";
    private static final String IMAGES_FOLDER = "img";

    /**
     * This method returns the {@link ScenarioSection} that is processed from the TestScenario object
     * ScenarioSection maps to a single test scenario in the tests that was executed.The data is being
     * processed so that the {@link ResultFormat} structure is maintained. Each scenario will be divided
     * in to several sections as defined in the format, each such section will be added to the scenario
     * as a {@link DividerSection} . These sections in turn can have child Divider sections or
     * {@link DataSection} which contains the {@link PerformanceTable}
     *
     * @param testScenario TestScenario being processed
     * @param resultFormat ResultFormatter containing the format data
     * @return ScenarioSection object that is included in the report
     */
    public ScenarioSection processScenario(TestScenario testScenario, ResultFormat resultFormat) {
        List<List<String>> data = testScenario.getPerformanceTestResults();
        ScenarioSection scenarioSection = new ScenarioSection();
        scenarioSection.setScenarioName(testScenario.getName());
        scenarioSection.setDescription(testScenario.getDescription());
        List<String> topics = data.remove(0);
        //Create a new scenario section per test scenario
        scenarioSection.setScenarioName(testScenario.getName());
        scenarioSection.setDescription(testScenario.getDescription());
        //get the index of the primary divider from topic list
        int indexOf = topics.indexOf(resultFormat.getPrimaryDivider());

        Stack<String> dividers = new Stack<>();
        dividers.addAll(resultFormat.getDividers());
        //divide the data in to groups with primary divider
        List<String> primarySections = data.stream()
                .map(strings -> strings.get(indexOf))
                .distinct()
                .collect(Collectors.toList());
        //for each section fill the data
        primarySections.forEach(primarySection -> {
            Stack<String> cloneOfDividers = (Stack<String>) dividers.clone();
            DividerSection primary = new DividerSection();
            primary.setData(resultFormat.getPrimaryDivider() + " : " + primarySection);
            List<List<String>> section = data.stream()
                    .filter(strings -> strings.get(indexOf).equals(primarySection))
                    .collect(Collectors.toList());
            DividerSection sectionData = getSectionData(cloneOfDividers, section, topics
                    , resultFormat);
            primary.setChildSections(Collections.singletonList(sectionData));
            scenarioSection.setDividerSection(primary);
        });
        scenarioSection.setChartsList(testScenario.getSummaryGraphs()
                .stream().map(s -> Paths.get(s)).map(path -> path.getFileName().toString())
                .collect(Collectors.toList()));
        return scenarioSection;
    }

    /**
     * This method returns the {@link DividerSection} data model after recursively processing the dividers.
     *
     * @param dividers        Stack of dividers
     * @param section         the list of data that needs to be processed into sections
     * @param topics          the topic list from csv data file
     * @param resultFormat ResultFormatter file that defines the data table structure
     * @return a DividerSection object populated with data
     */
    private DividerSection getSectionData(Stack<String> dividers, List<List<String>> section,
                                          List<String> topics, ResultFormat resultFormat) {
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
                    PerformanceTable table = createPerformanceTable(topics, collect1, resultFormat);
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
                            , resultFormat);
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
     * Creates the performance table given the data and formatter data.This table is needed to model
     * the performance result table in the mustache template, and the data is divided by the
     * {@link ResultFormat} definition.
     *
     * @param topics          topic list of csv data
     * @param data            the data that needs to be applied to the table
     * @param resultFormat {@link ResultFormat} defining the structure of table
     * @return PerformanceTable instance with data populated
     */
    private PerformanceTable createPerformanceTable(List<String> topics, List<List<String>> data,
                                                    ResultFormat resultFormat) {
        int depth = 1;
        int colspan = 1;
        //get table structure from result formatter
        List<Column> table = resultFormat.getTable();
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
        List<List<org.wso2.testgrid.reporting.model.performance.ColumnHeader>> headerData = new ArrayList<>();
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

}

