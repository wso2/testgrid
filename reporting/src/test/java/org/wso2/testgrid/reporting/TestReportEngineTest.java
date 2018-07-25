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

package org.wso2.testgrid.reporting;

import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

/**
 * Tests the summary log that gets printed at the end.
 */
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*" })
public class TestReportEngineTest extends BaseClass {

    private static final Logger logger = LoggerFactory.getLogger(TestReportEngineTest.class);

    @Test(dataProvider = "testPlanInputsNum")
    public void generateEmailReport(String testNum) throws Exception {
        logger.info("---- Running " + testNum);

        List<TestPlan> testPlans = getTestPlansFor(testNum);

        when(testPlanUOW.getLatestTestPlans(Matchers.any(Product.class)))
                .thenReturn(testPlans);
        when(testPlanUOW.getTestPlanById("abc")).thenReturn(Optional.of(testPlans.get(0)));
        when(testPlanUOW.getTestPlanById(Matchers.anyString())).thenAnswer(
                inv -> {
                    final String testPlanId = inv.getArgumentAt(0, String.class);
                    if (testPlanId.equals("abc")) {
                        return Optional.of(testPlans.get(0));
                    }
                    return Optional.of(testPlans.get(Integer.valueOf(testPlanId)));
                });
        when(testPlanUOW.getCurrentStatus(product)).thenReturn(Status.FAIL);

        final TestReportEngine testReportEngine = new TestReportEngine(testPlanUOW,
                new EmailReportProcessor(testPlanUOW));
        final Optional<Path> path = testReportEngine.generateEmailReport(product, productDir.toString());
        Assert.assertTrue(path.isPresent(), "Email report generation has failed. File path is empty.");
        logger.info("email report file: " + path.get());
    }

}
