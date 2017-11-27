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
package org.wso2.testgrid.automation.executor;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.Visualizer;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestAutomationUOW;

/**
 * This class is responsible for listening to JMeter tests and persisting the status of the tests after running each
 * of them.
 *
 * @since 1.0.0
 */
public class JMeterTestListener implements Visualizer {

    @Override
    public void add(SampleResult result) {
        try {
            String testCaseId = result.getSampleLabel(); // Test case ID is set to the sample label
            TestCase.Status status = result.isSuccessful() ?
                                     TestCase.Status.TESTCASE_COMPLETED :
                                     TestCase.Status.TESTCASE_ERROR;
            TestAutomationUOW testAutomationUOW = new TestAutomationUOW();
            testAutomationUOW.updateTestCase(testCaseId, status, result.getResponseMessage());
        } catch (TestGridDAOException e) {
            throw new RuntimeException(StringUtil.concatStrings("Error occurred when updating test case."), e);
        }
    }

    @Override
    public boolean isStats() {
        return false;
    }
}
