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
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
    public static final String NAME_COLUMN = "name";
    public static final String START_TIMESTAMP_COLUMN = "startTimestamp";
    public static final String MODIFIED_TIMESTAMP_COLUMN = "modifiedTimestamp";
    public static final String IS_TEST_SUCCESS_COLUMN = "isTestSuccess";
    public static final String LOG_LOCATION_COLUMN = "logLocation";
    public static final String FAILURE_MESSAGE_COLUMN = "failureMessage";
    public static final String TEST_SCENARIO_COLUMN = "testScenario";

    private static final long serialVersionUID = -1947567322771472903L;

    @Column(name = "test_name", nullable = false)
    private String name;

    @Column(name = "start_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp startTimestamp;

    @Column(name = "modified_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp modifiedTimestamp;

    @Column(name = "is_test_success", nullable = false)
    private boolean isTestSuccess;

    @Column(name = "log_location")
    private String logLocation;

    @Column(name = "failure_message", length = 20000)
    private String failureMessage;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = TestScenario.class, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "TESTSCENARIO_id", referencedColumnName = "id")
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
     * Returns whether the test is successful or failed.
     *
     * @return {@code true} if the test case is successful, {@code false} otherwise
     */
    public boolean isTestSuccess() {
        return isTestSuccess;
    }

    /**
     * Sets whether the test is successful or failed.
     *
     * @param testSuccess whether the test is successful or failed
     */
    public void setTestSuccess(boolean testSuccess) {
        isTestSuccess = testSuccess;
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

    @Override
    public String toString() {
        return StringUtil.concatStrings("TestCase{",
                "id='", this.getId(),
                ", name='", name, "\'",
                ", startTimestamp='", startTimestamp, "\'",
                ", modifiedTimestamp='", modifiedTimestamp, "\'",
                ", isTestSuccess='", isTestSuccess, "\'",
                ", logLocation='", logLocation, "\'",
                ", failureMessage='", failureMessage, "\'",
                '}');
    }
}
