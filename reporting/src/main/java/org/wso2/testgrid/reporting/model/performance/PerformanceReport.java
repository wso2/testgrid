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

import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing the Performance report.
 *
 * @since 1.0.0
 *
 */
public class PerformanceReport {

    private static final String  SCENARIO_SECTION_KEY = "scenarioSection";
    private static final String SCENARIO_MUSTACHE = "scenario_section.mustache";

    private String productName;
    private List<ScenarioSection> scenarioSections;
    private Map<String, String> reportContent;
    private String parsedScenarioString;

    /**
     * This constructor will create an instance and render the sub sections in the report.
     *
     * @param productName name of the Product
     * @param reportContent the report contents that are passed through the result formatter
     * @param scenarioSections List of {@link ScenarioSection} of the report
     * @throws ReportingException when there is an error creating the report
     */
    public PerformanceReport(String productName, Map<String, String> reportContent,
                             List<ScenarioSection> scenarioSections)
            throws ReportingException {
        this.productName = productName;
        this.reportContent = reportContent;
        this.scenarioSections = scenarioSections;

        //render the scenario sections
        Map<String, Object> scenarioMap = new HashMap<>();
        scenarioMap.put(SCENARIO_SECTION_KEY, scenarioSections);
        Renderable scenarioRenderer = RenderableFactory.getRenderable(SCENARIO_MUSTACHE);
        this.parsedScenarioString = scenarioRenderer.render(SCENARIO_MUSTACHE, scenarioMap);
    }

    public List<ScenarioSection> getScenarioSections() {
        return scenarioSections;
    }

    public void setScenarioSections(List<ScenarioSection> scenarioSections) {
        this.scenarioSections = scenarioSections;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setReportContent(Map<String, String> reportContent) {
        this.reportContent = reportContent;
    }

    public Map<String, String> getReportContent() {
        return reportContent;
    }

    public String getParsedScenarioString() {
        return parsedScenarioString;
    }

    public void setParsedScenarioString(String parsedScenarioString) {
        this.parsedScenarioString = parsedScenarioString;
    }
}

