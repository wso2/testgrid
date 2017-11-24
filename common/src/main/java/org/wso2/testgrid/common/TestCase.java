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

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Defines a model object of TestCase with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = TestCase.TEST_CASE_TABLE)
public class TestCase extends AbstractUUIDEntity implements Serializable {

    /**
     * Test plan table name.
     */
    public static final String TEST_CASE_TABLE = "test_case";

    /**
     * Column names of the table.
     */
    public static final String NAME_COLUMN = "test_name";
    public static final String START_TIMESTAMP_COLUMN = "start_timestamp";
    public static final String MODIFIED_TIMESTAMP_COLUMN = "modified_timestamp";
    public static final String STATUS_COLUMN = "status";
    public static final String LOG_LOCATION_COLUMN = "log_location";
    public static final String FAILURE_MESSAGE_COLUMN = "failure_message";
    public static final String TEST_SCENARIO_COLUMN = "TESTSCENARIO_id";

    private static final long serialVersionUID = -1947567322771472903L;

    @Column(name = NAME_COLUMN, nullable = false)
    private String name;

    @Column(name = START_TIMESTAMP_COLUMN, nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT '0000-00-00 00:00:00'")
    private Timestamp startTimestamp;

    @Column(name = MODIFIED_TIMESTAMP_COLUMN, nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp modifiedTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = STATUS_COLUMN, nullable = false)
    private Status status;

    @Column(name = LOG_LOCATION_COLUMN)
    private String logLocation;

    @Column(name = FAILURE_MESSAGE_COLUMN)
    private String failureMessage;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = TestScenario.class)
    @PrimaryKeyJoinColumn(name = TEST_SCENARIO_COLUMN, referencedColumnName = "id")
    private TestScenario testScenario;

    /**
     * Returns the name of the test case.
     *
     * @return test case name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the test case.
     *
     * @param name test case name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the start timestamp of the test case.
     *
     * @return test case start timestamp
     */
    public Timestamp getStartTimestamp() {
        return new Timestamp(startTimestamp.getTime());
    }

    /**
     * Sets the start timestamp of the test case.
     *
     * @param startTimestamp test case start timestamp
     */
    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = new Timestamp(startTimestamp.getTime());
    }

    /**
     * Returns the modified timestamp of the test case.
     *
     * @return modified test case timestamp
     */
    public Timestamp getModifiedTimestamp() {
        return new Timestamp(modifiedTimestamp.getTime());
    }

    /**
     * Sets the modified timestamp of the test case.
     *
     * @param modifiedTimestamp modified test case timestamp
     */
    public void setModifiedTimestamp(Timestamp modifiedTimestamp) {
        this.modifiedTimestamp = new Timestamp(modifiedTimestamp.getTime());
    }

    /**
     * Returns the status of the test case.
     *
     * @return test case status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the test case.
     *
     * @param status test case status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the log location of the test case.
     *
     * @return test case log location
     */
    public String getLogLocation() {
        return logLocation;
    }

    /**
     * Sets the test case log location.
     *
     * @param logLocation test case log location
     */
    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * Returns the failure message of the test case.
     *
     * @return test case failure message
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Sets the failure message of the test case.
     *
     * @param failureMessage test case failure message
     */
    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    /**
     * Returns the test scenario for the test case.
     *
     * @return test scenario for the test case
     */
    public TestScenario getTestScenario() {
        return testScenario;
    }

    /**
     * Sets the test scenario for the test case.
     *
     * @param testScenario test scenario for the test case
     */
    public void setTestScenario(TestScenario testScenario) {
        this.testScenario = testScenario;
    }

    /**
     * This defines the possible statuses of the {@link TestCase}.
     *
     * @since 1.0.0
     */
    public enum Status {

        /**
         * Test case is being executed.
         */
        TESTCASE_PENDING("TESTCASE_PENDING"),

        /**
         * Test case is being executed.
         */
        TESTCASE_EXECUTION("TESTCASE_EXECUTION"),

        /**
         * There was an error when executing the test case.
         */
        TESTCASE_ERROR("TESTCASE_ERROR"),

        /**
         * Test case execution has completed.
         */
        TESTCASE_COMPLETED("TESTCASE_COMPLETED");

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
}
