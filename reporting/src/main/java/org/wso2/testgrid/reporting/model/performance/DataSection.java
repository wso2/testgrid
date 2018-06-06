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
package org.wso2.testgrid.reporting.model.performance;

import java.util.List;

/**
 * Model class representing the data section of the {@link PerformanceReport}.
 *
 * @since 1.0.0
 *
 */
public class DataSection {

    private String scenarioName;
    private String description;
    private List<PerformanceTable> performanceTableList;

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PerformanceTable> getPerformanceTableList() {
        return performanceTableList;
    }

    public void setPerformanceTableList(List<PerformanceTable> performanceTableList) {
        this.performanceTableList = performanceTableList;
    }
}
