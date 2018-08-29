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
 *
 */

package org.wso2.testgrid.reporting.model.email;

import java.util.List;

/**
 * Model class representing the test-case result section in summarized email-report.
 */
public class TestCaseResultSection {

    private String testCaseName;
    private String testCaseDescription;
    private String testCaseExecutedOS;
    private String testCaseExecutedJDK;
    private String testCaseExecutedDB;
    int rowSpan = -1;

    List<InfraCombination> infraCombinations;

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getTestCaseDescription() {
        return testCaseDescription;
    }

    public void setTestCaseDescription(String testCaseDescription) {
        this.testCaseDescription = testCaseDescription;
    }

    public String getTestCaseExecutedOS() {
        return testCaseExecutedOS;
    }

    public void setTestCaseExecutedOS(String testCaseExecutedOS) {
        this.testCaseExecutedOS = testCaseExecutedOS;
    }

    public String getTestCaseExecutedJDK() {
        return testCaseExecutedJDK;
    }

    public void setTestCaseExecutedJDK(String testCaseExecutedJDK) {
        this.testCaseExecutedJDK = testCaseExecutedJDK;
    }

    public String getTestCaseExecutedDB() {
        return testCaseExecutedDB;
    }

    public void setTestCaseExecutedDB(String testCaseExecutedDB) {
        this.testCaseExecutedDB = testCaseExecutedDB;
    }

    public List<InfraCombination> getInfraCombinations() {
        return infraCombinations;
    }

    public void setInfraCombinations(List<InfraCombination> infraCombinations) {
        this.infraCombinations = infraCombinations;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }
}
