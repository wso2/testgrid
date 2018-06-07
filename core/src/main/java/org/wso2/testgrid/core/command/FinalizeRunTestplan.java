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
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.logging.plugins.LogFilePathLookup;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private TestPlanUOW testPlanUOW;
    private List<TestPlan> testPlans;

    public FinalizeRunTestplan() {
        testPlanUOW = new TestPlanUOW();
    }

    @Override
    public void execute() throws CommandExecutionException {

        LogFilePathLookup.setLogFilePath(
                TestGridUtil.deriveTestGridLogFilePath(productName, TestGridConstants.TESTGRID_LOG_FILE_NAME));

        if (Paths.get(testPlanYamlFilePath).toFile().exists()) {
            //Read test plan id from test-plan yaml file
            try {
                TestPlan testPlan = FileUtil.readYamlFile(testPlanYamlFilePath, TestPlan.class);
                Optional<TestPlan> testPlanEntity = testPlanUOW.getTestPlanById(testPlan.getId());
                if (testPlanEntity.isPresent()) {
                    testPlans = new ArrayList<>();
                    testPlans.add(testPlanEntity.get());
                } else {
                    testPlans = testPlanUOW.getTestPlansOlderThan(TIME_DURATION, TIME_UNIT);
                }
            } catch (IOException e) {
                logger.error("Error occurred while trying to read " + testPlanYamlFilePath);
            } catch (TestGridDAOException e) {
                logger.error("Error while fetching test plan from database.");
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
