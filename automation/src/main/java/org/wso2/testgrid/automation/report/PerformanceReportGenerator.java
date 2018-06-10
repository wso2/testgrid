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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.testgrid.automation.report;

import org.wso2.testgrid.automation.exception.ReportGeneratorException;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.reporting.PerformanceResultProcessor;
import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.TestReportEngine;
import org.wso2.testgrid.reporting.model.performance.PerformanceReport;
import org.wso2.testgrid.reporting.model.performance.ScenarioSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * ReportGenerator implementation for the PerformanceTests
 *
 * @since 1.0.0
 */
public class PerformanceReportGenerator extends ReportGenerator {

    private static final String TEST_TYPE_PERFORMANCE = "PERFORMANCE";

    public PerformanceReportGenerator(TestPlan testPlan) {
        super(testPlan);
    }

    public PerformanceReportGenerator() {
    }

    @Override
    public boolean canGenerateReport(TestPlan testPlan) {
        return TEST_TYPE_PERFORMANCE.equals(testPlan.getScenarioConfig().getTestType());
    }

    @Override
    public void generateReport() throws ReportGeneratorException {
        TestPlan testPlan = this.getTestPlan();
        List<ScenarioSection> scenarioSections = new ArrayList<>();
        PerformanceResultProcessor processor = new PerformanceResultProcessor();
        if (testPlan != null) {
            for (TestScenario testScenario : testPlan.getTestScenarios()) {
                ScenarioSection scenarioSection = processor.processScenario(testScenario
                        , testPlan.getResultFormatter());
                scenarioSections.add(scenarioSection);
            }
            String productName = testPlan.getDeploymentPattern().getProduct().getName();
            Set<Map.Entry<String, String>> entries = testPlan.getResultFormatter().getReportStructure().entrySet();
            try {
                PerformanceReport performanceReport = new PerformanceReport(productName, entries, scenarioSections);
                TestReportEngine engine = new TestReportEngine();
                engine.generatePerformanceReport(performanceReport, testPlan.getTestScenarios());
            } catch (ReportingException e) {
                throw new ReportGeneratorException(String.format("Error occured while genearating " +
                                "per test plan report for %s", productName), e);
            }
        } else {
            throw new ReportGeneratorException(String.format("Report generator %s is not correctly" +
                            " initialized with a TestPlan", this.getClass().toString()));
        }
    }
}
