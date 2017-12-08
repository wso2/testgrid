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

import org.wso2.carbon.config.annotation.Element;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
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
    public static final String STATUS_COLUMN = "status";
    public static final String NAME_COLUMN = "name";
    public static final String TEST_PLAN_COLUMN = "testPlan";
    public static final String PRE_SCRIPT_STATUS_COLUMN = "isPreScriptSuccessful";
    public static final String POST_SCRIPT_STATUS_COLUMN = "isPostScriptSuccessful";
    public static final String TEST_ENGINE_COLUMN = "testEngine";

    private static final long serialVersionUID = -2666342786241472418L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "name", nullable = false)
    @Element(description = "name of the solution pattern which is covered by this test scenario")
    private String name;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = TestPlan.class, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "TESTPLAN_id", referencedColumnName = ID_COLUMN)
    private TestPlan testPlan;

    @OneToMany(mappedBy = "testScenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases;

    @Column(name = "is_pre_script_success")
    @Element(description = "holds the status true if pre script is successful")
    private boolean isPreScriptSuccessful = false;

    @Column(name = "is_post_script_success")
    @Element(description = "holds the status true if post script is successful")
    private boolean isPostScriptSuccessful = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_engine", nullable = false)
    @Element(description = "holds the test engine type (i.e. JMETER, TESTNG)")
    private TestEngine testEngine;

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
     * Checks if pre script is successful.
     *
     * @return {@code true} of the pre script is successful, {@code false} otherwise
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
     * @return @return {@code true} of the post script is successful, {@code false} otherwise
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

    /**
     * Returns the test engine in which the test scenario should be executed.
     *
     * @return test scenario executing test engine
     */
    public TestEngine getTestEngine() {
        return testEngine;
    }

    /**
     * Sets the test engine in which the test scenario should be executed.
     *
     * @param testEngine test engine in which the test scenario should be executed
     */
    public void setTestEngine(TestEngine testEngine) {
        this.testEngine = testEngine;
    }

    /**
     * Sets the test engine in which the test scenario should be executed.
     *
     * @param testEngine test engine in which the test scenario should be executed
     */
    public void setTestEngine(String testEngine) {
        this.testEngine = TestEngine.valueOf(testEngine.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return "TestScenario{" +
               "id='" + this.getId() + '\'' +
               ", status=" + status +
               ", name='" + name + '\'' +
               ", testPlan=" + testPlan +
               ", testEngine=" + testEngine +
               '}';
    }

    /**
     * This defines the possible statuses of the {@link TestScenario}.
     *
     * @since 1.0.0
     */
    public enum Status {

        /**
         * Planned to execute.
         */
        TEST_SCENARIO_PENDING("TEST_SCENARIO_PENDING"),

        /**
         * Executing in TestEngine.
         */
        TEST_SCENARIO_RUNNING("TEST_SCENARIO_RUNNING"),

        /**
         * Execution completed.
         */
        TEST_SCENARIO_COMPLETED("TEST_SCENARIO_COMPLETED"),

        /**
         * Execution error.
         */
        TEST_SCENARIO_ERROR("TEST_SCENARIO_ERROR");

        private final String status;

        /**
         * Sets the status of the test case.
         *
         * @param status test case status
         */
        Status(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return this.status;
        }
    }

    /**
     * This defines the TestEngines in which the TestScenario can be executed.
     *
     * @since 1.0.0
     */
    public enum TestEngine {

        /**
         * Defines the JMeter TestEngine.
         */
        JMETER("JMETER"),

        /**
         * Defines the TestNg TestEngine.
         */
        TESTNG("TESTNG"),

        /**
         * Defines the Selenium TestEngine.
         */
        SELENIUM("SELENIUM");

        private final String testEngine;

        /**
         * Sets the test engine for the test scenario.
         *
         * @param testEngine test engine for the test scenario
         */
        TestEngine(String testEngine) {
            this.testEngine = testEngine;
        }

        @Override
        public String toString() {
            return this.testEngine;
        }
    }
}
