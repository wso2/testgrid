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

package org.wso2.testgrid.reporting.model.email;

import org.wso2.testgrid.common.InfraCombination;

import java.util.ArrayList;
import java.util.List;

/**
 * This defines the possible statuses of an entity.
 *
 * @since 1.0.0
 */
public class TestFailureSummary {
    private String testCaseName;
    private String testCaseDescription;
    private List<InfraCombination> infraCombinations = new ArrayList<>();

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

    public List<InfraCombination> getInfraCombinations() {
        return infraCombinations;
    }

    public void setInfraCombinations(List<InfraCombination> infraCombinations) {
        this.infraCombinations = infraCombinations;
    }
}
