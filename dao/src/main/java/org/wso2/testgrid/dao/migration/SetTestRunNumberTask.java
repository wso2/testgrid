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
package org.wso2.testgrid.dao.migration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is responsible for setting the test run number for all test plan records in DB.
 * <p>
 * The relevant change log file is {@code db-changelog-1.0.xml}
 *
 * @since 1.0.0
 */
public class SetTestRunNumberTask implements CustomTaskChange {

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            ProductUOW productUOW = new ProductUOW();
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            List<Product> productList = productUOW.getProducts();

            for (Product product : productList) {
                List<DeploymentPattern> deploymentPatterns = product.getDeploymentPatterns();
                for (DeploymentPattern deploymentPattern : deploymentPatterns) {
                    List<TestPlan> testPlans = new ArrayList<>(deploymentPattern.getTestPlans());

                    // Group test plans by infra parameters
                    Map<String, List<TestPlan>> groupedTestPlans = testPlans.stream()
                            .collect(Collectors.groupingBy(TestPlan::getInfraParameters));

                    for (Map.Entry<String, List<TestPlan>> listEntry : groupedTestPlans.entrySet()) {
                        Comparator<TestPlan> testPlanComparator = (testPlan1, testPlan2) ->
                                testPlan2.getModifiedTimestamp().compareTo(testPlan1.getModifiedTimestamp());
                        listEntry.getValue().sort(testPlanComparator);

                        // Set test run number
                        for (int i = 0; i < listEntry.getValue().size(); i++) {
                            TestPlan testPlan = listEntry.getValue().get(i);
                            testPlan.setTestRunNumber(i + 1);
                            testPlanUOW.persistTestPlan(testPlan);
                        }
                    }
                }
            }
        } catch (TestGridDAOException e) {
            throw new CustomChangeException("Error in setting test run number values for test plans.", e);
        }

    }

    @Override
    public String getConfirmationMessage() {
        return StringUtil.concatStrings("Migration successful, the following alteration were made: \n",
                "\t * Adding `test_run_number` column to the `test_plan` table. \n",
                "\t * Populating data to the `test_run_number` column in `test_plan` table. \n",
                "\t * Removing log_location column from the `test_plan` table.\n");
    }

    @Override
    public void setUp() {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        // No validations
        return null;
    }
}
