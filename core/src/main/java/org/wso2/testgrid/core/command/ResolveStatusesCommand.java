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
package org.wso2.testgrid.core.command;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

import java.util.List;

/**
 * Resolves the invalid statuses caused by any failures.
 */
public class ResolveStatusesCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(RunTestPlanCommand.class);

    @Option(name = "--product",
            usage = "Product Name",
            aliases = { "-p" },
            required = true)
    private String productName = "";

    private ProductUOW productUOW;
    private TestPlanUOW testPlanUOW;

    public ResolveStatusesCommand() {
        productUOW = new ProductUOW();
        testPlanUOW = new TestPlanUOW();
    }

    @Override
    public void execute() throws CommandExecutionException {

        List<TestPlan> testPlans = testPlanUOW.getLatestTestPlans(getProduct(productName));
        boolean isExistsFailedScenarios = false;
        for (TestPlan testPlan : testPlans) {
            //Set statuses of scenarios
            for (TestScenario testScenario : testPlan.getTestScenarios()) {
                switch (testScenario.getStatus()) {
                    case PENDING:
                        testScenario.setStatus(Status.DIDNT_RUN);
                        break;
                    case RUNNING:
                        testScenario.setStatus(Status.ERROR);
                        break;
                    case SUCCESS:
                        break;
                    case FAIL:
                        isExistsFailedScenarios = true;
                        break;
                    default:
                        break;
                }
            }
            //Set statuses of testplans
            switch (testPlan.getStatus()) {
                case PENDING:
                    testPlan.setStatus(Status.ERROR);
                    persistTestPlan(testPlan);
                    break;
                case RUNNING:
                    if (isExistsFailedScenarios) {
                        testPlan.setStatus(Status.FAIL);
                    } else {
                        testPlan.setStatus(Status.INCOMPLETE);
                    }
                    persistTestPlan(testPlan);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Returns the product for the given parameters.
     *
     * @param productName product name
     * @return an instance of {@link Product} for the given parameters
     * @throws CommandExecutionException thrown when error on retrieving product
     */
    private Product getProduct(String productName)
            throws CommandExecutionException {
        try {
            return productUOW.getProduct(productName).orElseThrow(() -> new CommandExecutionException(
                    StringUtil.concatStrings("Product not found for {product name: ", productName, "}")));
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred when initialising DB transaction.", e);
        }
    }

    /**
     * Persists the test plan with the changed status.
     *
     * @param testPlan TestPlan object to persist
     */
    private void persistTestPlan(TestPlan testPlan) {
        try {
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }
}
