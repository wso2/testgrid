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

package org.wso2.testgrid.reporting.summary;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.reporting.BaseClass;

import java.util.List;
import java.util.Map;

/**
 *  Tests @{@link InfrastructureSummaryReporter}.
 *
 */
public class InfrastructureSummaryReporterTest extends BaseClass {

    @BeforeMethod
    public void init() throws Exception {
        super.init();

    }

    @Test(dataProvider = "testPlanInputsNum")
    public void testGetSummaryTable(String testPlanInputsNum) throws Exception {

        List<TestPlan> testPlans = getTestPlansFor(testPlanInputsNum);
        final InfrastructureSummaryReporter summaryReporter = new InfrastructureSummaryReporter();
        final Map<String, InfrastructureBuildStatus> summaryTable = summaryReporter.getSummaryTable(testPlans);

        if (testPlanInputsNum.equals("02")) {
            Assert.assertEquals(summaryTable.size(), 3);
            final InfrastructureBuildStatus buildStatus = summaryTable.values().iterator().next();
            Assert.assertEquals(buildStatus.getUnknownInfra().size(), 0, "Summary with unknown status found. " +
                    buildStatus.getUnknownInfra());
            final List<List<InfrastructureParameter>> failedInfra01 = buildStatus.getFailedInfra();
            final List<InfrastructureParameter> unassociatedFailedInfra = buildStatus.getUnassociatedFailedInfra();

            Assert.assertEquals(unassociatedFailedInfra.size(), buildStatus.getFailedInfra().size(), "All failed infra "
                    + "should be unassociated infras.");
            Assert.assertEquals(buildStatus.getFailedInfra().size(), 1);
            Assert.assertEquals(unassociatedFailedInfra.size(), 1);
            Assert.assertEquals(unassociatedFailedInfra.get(0).getName(), "CentOS", "Summary table's "
                    + "failed infra result validation failed.");

            final List<InfrastructureParameter> failedAssociatedInfras = failedInfra01.get(0);
            Assert.assertEquals(failedAssociatedInfras.size(), 1);
            InfrastructureParameter failedInfra = failedAssociatedInfras.get(0);
            Assert.assertEquals(failedInfra.getName(), "CentOS",
                    "Could not detect the actual reason for test failures.");

        }
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

}
