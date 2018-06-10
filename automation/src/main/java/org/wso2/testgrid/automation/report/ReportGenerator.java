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

import org.wso2.testgrid.automation.exception.ReportGeneratorException;
import org.wso2.testgrid.common.TestPlan;

/**
 * This class is the abstraction of report generation functionality.
 *
 * @since 1.0.0
 */
public abstract class ReportGenerator {

    private TestPlan testPlan;

    /**
     * Initialize the report generator with a test plan
     * @param testPlan TestPlan object
     */
    public ReportGenerator(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    /**
     * Default constructor is required because the it is being used by the Iterator of ServiceLoader
     * classes
     */
    ReportGenerator() {}

    /**
     * This method must be overriden to indicate wheather the subclass can generate a report for the given
     * TestPlan
     *
     * @param testPlan TestPlan object with results
     * @return true if it si compatible, false otherwise
     */
    public abstract boolean canGenerateReport(TestPlan testPlan);

    /**
     * This method must be overriden by the subclass to include the main report generation logic
     *
     * @throws ReportGeneratorException if there is an error with generating the report
     */
    public abstract void generateReport() throws ReportGeneratorException;

    /**
     * Getter method for TestPlan
     *
     * @return the test plan object
     */
    public TestPlan getTestPlan() {
        return testPlan;
    }

    /**
     * Setter method for TestPlan
     *
     * @param testPlan the TestPlan to be used
     */
    public void setTestPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

}
