/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.common;

import org.wso2.testgrid.common.util.StringUtil;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Defines a model object of TestScenario with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = TestScenario.TEST_SCENARIO_TABLE)
public class TestScenario extends AbstractUUIDEntity implements Serializable {

    /**
     * Test plan table name.
     */
    public static final String TEST_SCENARIO_TABLE = "test_scenario";

    /**
     * Column names of the table.
     */
    public static final String NAME_COLUMN = "name";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String STATUS_COLUMN = "status";
    public static final String PRE_SCRIPT_STATUS_COLUMN = "isPreScriptSuccessful";
    public static final String POST_SCRIPT_STATUS_COLUMN = "isPostScriptSuccessful";
    public static final String TEST_PLAN_COLUMN = "testPlan";

    private static final long serialVersionUID = -2666342786241472418L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "is_pre_script_success")
    private boolean isPreScriptSuccessful = false;

    @Column(name = "config_change_set_name")
    private String configChangeSetName;

    @Column(name = "is_post_script_success")
    private boolean isPostScriptSuccessful = false;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = TestPlan.class, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "TESTPLAN_id", referencedColumnName = ID_COLUMN)
    private TestPlan testPlan;

    @OneToMany(mappedBy = "testScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();

    @Transient
    private String dir;

    @Transient
    private List<List<String>> performanceTestResults;

    @Transient
    private List<String> summaryGraphs;

    /**
     * Returns the name of the test scenario.
     *
     * @return test scenario name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the test scenario name.
     *
     * @param name test scenario name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of the test scenario.
     *
     * @return test scenario description
     */
    public String getDescription() {

        return description;
    }

    /**
     * Sets the test scenario description.
     *
     * @param description test scenario description
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Returns the directory of the test scenario.
     *
     * @return test scenario directory
     */
    public String getDir() {

        return dir;
    }

    /**
     * Sets the test scenario directory.
     *
     * @param dir test scenario directory
     */
    public void setDir(String dir) {

        this.dir = dir;
    }

    /**
     * Returns the status of the test scenario.
     *
     * @return test scenario status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the test scenario status.
     *
     * @param status test scenario status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Checks if pre script is successful.
     *
     * @return {@code true} if the pre script is successful, {@code false} otherwise
     */
    public boolean isPreScriptSuccessful() {
        return isPreScriptSuccessful;
    }

    /**
     * Sets the status of the pre script execution.
     *
     * @param isPreScriptSuccessful Status of pre script execution
     */
    public void setIsPreScriptSuccessful(boolean isPreScriptSuccessful) {
        this.isPreScriptSuccessful = isPreScriptSuccessful;
    }

    /**
     * Checks if post script is successful.
     *
     * @return {@code true} if the post script is successful, {@code false} otherwise
     */
    public boolean isPostScriptSuccessful() {
        return isPostScriptSuccessful;
    }

    /**
     * Sets the status of the post script execution.
     *
     * @param isPostScriptSuccessful Status of the post script execution
     */
    public void setIsPostScriptSuccessful(boolean isPostScriptSuccessful) {
        this.isPostScriptSuccessful = isPostScriptSuccessful;
    }

    public String getConfigChangeSetName() {
        return this.configChangeSetName;
    }

    public void setConfigChangeSetName(String configChangeSetName) {
        this.configChangeSetName = configChangeSetName;
    }
    /**
     * Returns the test plan associated with this test scenario.
     *
     * @return test plan associated with this test scenario
     */
    public TestPlan getTestPlan() {
        return testPlan;
    }

    /**
     * Sets the test plan associated with this test scenario.
     *
     * @param testPlan test plan associated with this test scenario
     */
    public void setTestPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    /**
     * Returns the associated test cases list.
     *
     * @return associated test cases list
     */
    public List<TestCase> getTestCases() {
        return testCases;
    }

    /**
     * Sets the associated test cases list.
     *
     * @param testCases associated test cases list
     */
    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    /**
     * Adds a test case to the test case list.
     *
     * @param testCase test case to be added to the list
     */
    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
    }

    /**
     *Returns the performance results data section
     *
     * @return List of List of Strings containing the CSV data
     */
    public List<List<String>> getPerformanceTestResults() {
        return performanceTestResults;
    }

    /**
     *Set the PerformanceTest results data
     *
     * @param performanceTestResults List of List of string containing the performance data
     */
    public void setPerformanceTestResults(List<List<String>> performanceTestResults) {
        this.performanceTestResults = performanceTestResults;
    }

    /**
     *
     * Returns a List of Paths referencing the summary graphs
     *
     * @return List of path objects containing the graphs
     */
    public List<String> getSummaryGraphs() {
        return summaryGraphs;
    }

    /**
     * Set the performance summary graphs list
     *
     * @param summaryGraphs List of Paths referencing the graphs
     */
    public void setSummaryGraphs(List<String> summaryGraphs) {
        this.summaryGraphs = summaryGraphs;
    }

    @Override
    public String toString() {
        String id = this.getId() != null ? this.getId() : "";
        String createdTimestamp = this.getCreatedTimestamp() != null ? this.getCreatedTimestamp().toString() : "";
        String modifiedTimestamp = this.getModifiedTimestamp() != null ? this.getModifiedTimestamp().toString() : "";
        return StringUtil.concatStrings("TestScenario{",
                "id='", id, "\'",
                ", name='", name, "\'",
                ", status='", status, "\'",
                ", createdTimestamp='", createdTimestamp, "\'",
                ", modifiedTimestamp='", modifiedTimestamp, "\'",
                ", testPlan='", testPlan, "\'",
                '}');
    }
}
