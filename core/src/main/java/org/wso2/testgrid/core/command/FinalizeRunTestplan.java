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
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.ScenarioConfig;
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
    private static final String DEFAULT_INTERVAL = "24";

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
    private List<TestPlan> testPlans = new ArrayList<>();

    public FinalizeRunTestplan() {
        testPlanUOW = new TestPlanUOW();
        productUOW = new ProductUOW();
    }

    @Override
    public void execute() throws CommandExecutionException {

        LogFilePathLookup.setLogFilePath(
                TestGridUtil.deriveTestGridLogFilePath(productName, TestGridConstants.TESTGRID_LOG_FILE_NAME));
        logger.info("Finalizing test plan status...");
        try {
            if (Paths.get(testPlanYamlFilePath).toFile().exists()) {
                //Read test plan id from test-plan yaml file
                TestPlan testPlan = FileUtil.readYamlFile(testPlanYamlFilePath, TestPlan.class);
                Optional<TestPlan> testPlanEntity = testPlanUOW.getTestPlanById(testPlan.getId());
                if (testPlanEntity.isPresent()) {
                    testPlans = new ArrayList<>();
                    testPlans.add(testPlanEntity.get());
                } else {
                    logger.error("No test plans with id: " + testPlan.getId() + " found in the database.");
                    return;
                }
            } else {
                String interval = ConfigurationContext.getProperty(
                        ConfigurationContext.ConfigurationProperties.FINALIZE_RUN_TESTPLAN_INTERVAL);
                if (interval != null) {
                    testPlans = testPlanUOW.getTestPlansOlderThan(interval, TIME_UNIT);
                }
                testPlans = testPlanUOW.getTestPlansOlderThan(DEFAULT_INTERVAL, TIME_UNIT);
                logger.info("Found " + testPlans.size() + " test plans to finalize statuses.");
                for (int i = 0; i < testPlans.size(); i++) {
                    logger.info((i + 1) + ". " + testPlans.get(i).getId());
                }

            }

            boolean isExistsFailedScenarios = false;
            for (TestPlan testPlan : testPlans) {
                for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
                    switch (scenarioConfig.getStatus()) {
                        case PENDING:
                            scenarioConfig.setStatus(Status.DID_NOT_RUN);
                            break;
                        case RUNNING:
                            scenarioConfig.setStatus(Status.ERROR);
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
                if (testPlan.getStatus().equals(TestPlanStatus.RUNNING)) {
                    testPlan.setStatus(TestPlanStatus.ERROR);
                    //Change the test-plan phase to currentPhase's ERROR stage
                    //Ex:  INFRA_PHASE_STARTED will be updated to INFRA_PHASE_ERROR
                    TestPlanPhase testPlanPhase = testPlan.getPhase();
                    if (testPlanPhase != null) {
                        String testPlanPhaseStr = testPlan.getPhase().toString();
                        String phaseName = (testPlanPhaseStr.substring(0, testPlanPhaseStr.lastIndexOf('_')));
                        testPlan.setPhase(TestPlanPhase.valueOf(phaseName + "_ERROR"));
                        //todo: if failed before beginning of new one (then need to set new phase's ERROR phase).
                    } else {
                        //Assume the phase is null because the job has aborted/failed before setting the first phase.
                        testPlan.setPhase(TestPlanPhase.PREPARATION_ERROR);
                    }
                    logger.info("=============##### FINALIZED TestPlan Result ####==========");
                    logger.info("TestPlan:" + testPlan.getId());
                    logger.info("Status:" + testPlan.getStatus());
                    logger.info("Phase:" + testPlan.getPhase());
                } else {
                    logger.info("=============##### TestPlan Result ####==========");
                    logger.info("TestPlan:" + testPlan.getId());
                    logger.info("Status:" + testPlan.getStatus());
                    logger.info("Phase:" + testPlan.getPhase());
                }
                persistTestPlan(testPlan);
            }
            updateProductStatus();
        } catch (IOException e) {
            logger.error("Error occurred while trying to read " + testPlanYamlFilePath, e);
        } catch (TestGridDAOException e) {
            logger.error("Error while fetching test plan from database.", e);
        } catch (TestGridException e) {
            logger.error("Error occured while updating the product status.", e);
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
        Path source = Paths.get(workspace, "test-plans");
        String productId;
        Boolean isCompleteBuild = true;
        if (!Files.exists(source)) {
            logger.debug("Test-plans dir does not exist. Nothing to finalize. Dir: " + source);
            return;
        }
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
                    if (TestPlanStatus.FAIL.equals(testPlanEntity.get().getStatus())) {
                        productId = testPlanEntity.get().getDeploymentPattern().getProduct().getId();
                        productUOW.updateProductStatusTimestamp(Status.FAIL, productId);
                        break;
                    } else if (TestPlanStatus.ERROR.equals(testPlanEntity.get().getStatus()) && isCompleteBuild) {
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
            logger.error("Error occured when updating the product table of TG", e);
        } catch (IOException e) {
            logger.error("Error occured when reading a test plan yaml file", e);
        }
    }
}
