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

import org.wso2.testgrid.common.exception.TestReportEngineException;

/**
 * This interface defines the contract of the TestReportEngine where the result generation should happen based on
 * executed automation results.
 */
public interface TestReportEngine {

    /**
     * Generates a test report based on the given test plan.
     *
     * @param productTestPlan test plan to generate the test report
     * @throws TestReportEngineException thrown when reading the results from files or when writing the test report
     * to file
     */
    void generateReport(ProductTestPlan productTestPlan) throws TestReportEngineException;
}
