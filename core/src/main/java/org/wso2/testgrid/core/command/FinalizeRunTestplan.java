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
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.logging.plugins.LogFilePathLookup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resolves the invalid statuses caused by any failures.
 */
public class FinalizeRunTestplan implements Command {

    private static final Logger logger = LoggerFactory.getLogger(RunTestPlanCommand.class);
    private static final String TIME_UNIT = "HOUR";
    private static final String TIME_DURATION = "24";

    @Option(name = "--product",
            usage = "Product Name",
            aliases = { "-p" },
            required = true)
    private String productName = "";

    @Option(name = "--file",
            usage = "Test Plan File",
            aliases = { "-f" })
    private String testPlanYamlFilePath = "";

    @Option(name = "--workspace",
            usage = "Workspace Of The Job",
            aliases = { "-w" })
    private String workspace = "";

    private TestPlanUOW testPlanUOW;
    private ProductUOW productUOW;
    private List<TestPlan> testPlans;

    public FinalizeRunTestplan() {
        testPlanUOW = new TestPlanUOW();
        productUOW = new ProductUOW();
    }

    @Override
    public void execute() throws CommandExecutionException {

        LogFilePathLookup.setLogFilePath(
                TestGridUtil.deriveTestGridLogFilePath(productName, TestGridConstants.TESTGRID_LOG_FILE_NAME));
        try {
            if (Paths.get(testPlanYamlFilePath).toFile().exists()) {
                //Read test plan id from test-plan yaml file
                TestPlan testPlan = FileUtil.readYamlFile(testPlanYamlFilePath, TestPlan.class);
                Optional<TestPlan> testPlanEntity = testPlanUOW.getTestPlanById(testPlan.getId());
                if (testPlanEntity.isPresent()) {
                    testPlans = new ArrayList<>();
                    testPlans.add(testPlanEntity.get());
                } else {
                    testPlans = testPlanUOW.getTestPlansOlderThan(TIME_DURATION, TIME_UNIT);
                }

            } else {
                testPlans = testPlanUOW.getTestPlansOlderThan(TIME_DURATION, TIME_UNIT);
            }

            logger.info("Finalizing test plan status...");
            boolean isExistsFailedScenarios = false;
            for (TestPlan testPlan : testPlans) {
                //Set statuses of scenarios
                for (TestScenario testScenario : testPlan.getTestScenarios()) {
                    switch (testScenario.getStatus()) {
                    case PENDING:
                        testScenario.setStatus(Status.DID_NOT_RUN);
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
                    testPlan.setStatus(Status.DID_NOT_RUN);
                    persistTestPlan(testPlan);
                    break;
                case RUNNING:
                    if (isExistsFailedScenarios) {
                        testPlan.setStatus(Status.FAIL);
                        isExistsFailedScenarios = false;
                    } else {
                        testPlan.setStatus(Status.INCOMPLETE);
                    }
                    persistTestPlan(testPlan);
                    break;
                default:
                    break;
                }
            }
            updateProductStatus();
        } catch (IOException e) {
            logger.error("Error occurred while trying to read " + testPlanYamlFilePath);
        } catch (TestGridDAOException e) {
            logger.error("Error while fetching test plan from database.");
        } catch (TestGridException e) {
            logger.error("Error occured while updating the product status.");
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

    /**
     * Update the last success timestamp of the product build or last failure timestamp of the product build
     * by considering status of test plans.
     *
     */
    private void updateProductStatus() throws TestGridException {
        Path source = Paths.get(workspace + "/test-plans");
        String productId;
        Boolean isCompleteBuild = true;
        try (Stream<Path> stream = Files.list(source).filter(Files::isRegularFile)) {
            List<Path> paths = stream.sorted().collect(Collectors.toList());
            for (Iterator<Path> iterator = paths.iterator(); iterator.hasNext(); ) {
                Path path = iterator.next();
                if (!path.toFile().exists()) {
                    throw new TestGridException(
                            "Test Plan File doesn't exist. File path is " + path.toAbsolutePath().toString());
                }
                TestPlan testPlan = FileUtil.readYamlFile(path.toAbsolutePath().toString(), TestPlan.class);
                Optional<TestPlan> testPlanEntity = testPlanUOW.getTestPlanById(testPlan.getId());
                if (testPlanEntity.isPresent()) {
                    if (Status.FAIL.equals(testPlanEntity.get().getStatus())) {
                        productId = testPlanEntity.get().getDeploymentPattern().getProduct().getId();
                        productUOW.updateProductStatusTimestamp(Status.FAIL, productId);
                        break;
                    } else if ((Status.DID_NOT_RUN.equals(testPlanEntity.get().getStatus()) || Status.INCOMPLETE
                            .equals(testPlanEntity.get().getStatus())) && isCompleteBuild) {
                        isCompleteBuild = false;
                    }
                } else {
                    throw new TestGridException(
                            "Test Plan doesn't exist in the TG database. Test Plan id: " + testPlan.getId());
                }
                if (!iterator.hasNext() && isCompleteBuild) {
                    productId = testPlanEntity.get().getDeploymentPattern().getProduct().getId();
                    productUOW.updateProductStatusTimestamp(Status.SUCCESS, productId);
                }
            }
        } catch (TestGridDAOException e) {
            logger.error("Error occured when updating the product table of TG");
        } catch (IOException e) {
            logger.error("Error occured when reading a test plan yaml file");
        }
    }
}
