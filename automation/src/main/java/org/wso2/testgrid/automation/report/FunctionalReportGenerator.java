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
package org.wso2.testgrid.automation.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.exception.ReportGeneratorException;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ScenarioConfig;

/**
 * Report Generator implementation for Factional Tests.
 *
 * @since 1.0.0
 */
public class FunctionalReportGenerator extends ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(FunctionalReportGenerator.class);

    public FunctionalReportGenerator(TestPlan testPlan) {
        super(testPlan);
    }

    public FunctionalReportGenerator() {
    }

    @Override
    public boolean canGenerateReport(TestPlan testPlan) {

        boolean canGenerate = true;
        for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
            canGenerate = canGenerate && TestGridConstants.TEST_TYPE_FUNCTIONAL.equals(scenarioConfig.getTestType());
        }
        return canGenerate;
    }

    @Override
    public void generateReport() throws ReportGeneratorException {
        TestPlan testPlan = this.getTestPlan();
        if (testPlan != null) {
            logger.warn("Functional test report generation not implemented yet!");
        } else {
            throw new ReportGeneratorException(String.format("Report generator %s is not correctly " +
                            "initialized with a TestPlan", this.getClass().toString()));
        }
    }
}

