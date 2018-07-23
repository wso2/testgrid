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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.dao.dto;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

/**
 * Defines a model object of TestPlan with required attributes.
 *
 * @since 1.0.0
 */
@SqlResultSetMapping(name = "TestCaseFailureResult", classes = {
        @ConstructorResult(targetClass = TestCaseFailureResultDTO.class, columns = { @ColumnResult(name = "testName"),
                @ColumnResult(name = "failureMessage"),
                @ColumnResult(name = "infraParameters") }) })
public class TestCaseFailureResultDTO {

    private String testName;
    private String failureMessage;
    private String infraParameters;

    public TestCaseFailureResultDTO(String testname, String failureMessage, String infraParameters) {
        this.testName = testname;
        this.failureMessage = failureMessage;
        this.infraParameters = infraParameters;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public String getInfraParameters() {
        return infraParameters;
    }

    public void setInfraParameters(String infraParameters) {
        this.infraParameters = infraParameters;
    }
}
