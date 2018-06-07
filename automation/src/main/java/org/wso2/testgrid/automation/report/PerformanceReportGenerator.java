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
import org.wso2.testgrid.automation.parser.CSVResultParser;
import org.wso2.testgrid.common.TestPlan;


/**
 *
 */
public class PerformanceReportGenerator extends ReportGenerator{

    private static final String TEST_TYPE_PERFORMANCE = "PERFORMANCE";

    PerformanceReportGenerator(TestPlan testPlan){
        super(testPlan);
    }

    PerformanceReportGenerator(){}

    @Override
    public boolean canGenerateReport(TestPlan testPlan) {
        return TEST_TYPE_PERFORMANCE.equals(testPlan.getScenarioConfig().getTestType());
    }

    @Override
    public void generateReport() throws ReportGeneratorException {
        TestPlan testPlan = this.getTestPlan();
        if(testPlan != null) {

            //parse data

            //crete performance report data model
            //render report
            //save report


        }else{
            throw new ReportGeneratorException(String.format("Report generator %s is not correctly initialized with a TestPlan",
                    this.getClass().toString()));
        }
    }
}
