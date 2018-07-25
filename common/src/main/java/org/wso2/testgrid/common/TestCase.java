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
    public static final String IS_SUCCESS_COLUMN = "isSuccess";
    public static final String FAILURE_MESSAGE_COLUMN = "failureMessage";
    public static final String TEST_SCENARIO_COLUMN = "testScenario";

    private static final long serialVersionUID = -1947567322771472903L;

    @Column(name = "test_name", nullable = false)
    private String name;

    @Column(name = "is_success", nullable = false)
    private boolean isSuccess;

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
     * Returns whether the test is successful or failed.
     *
     * @return {@code true} if the test case is successful, {@code false} otherwise
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Sets whether the test is successful or failed.
     *
     * @param success whether the test is successful or failed
     */
    public void setSuccess(boolean success) {
        isSuccess = success;
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
        String id = this.getId() != null ? this.getId() : "";
        return StringUtil.concatStrings("TestCase{",
                "id='", id,
                ", name='", name, "\'",
                ", isSuccess='", isSuccess, "\'",
                ", failureMessage='", failureMessage, "\'",
                ", testScenario='", testScenario.getName(), "\'",
                '}');
    }
}
