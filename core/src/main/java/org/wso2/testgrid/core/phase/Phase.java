/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.testgrid.core.phase;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.core.util.JsonPropFileUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;


/**
 * This class is represents abstract Phase which will include common methods of each Infra, Deploy, Test phases.
 */
public abstract class Phase {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final JsonPropFileUtil jsonpropFileEditor = new JsonPropFileUtil();
    private static final int LINE_LENGTH = 72;

    private TestPlanUOW testPlanUOW;
    private TestScenarioUOW testScenarioUOW;

    private TestPlan testPlan;

    /**
     * This is the basic method of a Phase. A test-plan can be executed along the phase by passing it into this.
     *
     * @param testPlan test-plan needed to be executed
     * @return updated test-plan after the execution of phase
     */
    public TestPlan execute(TestPlan testPlan) {
        if (testPlanUOW == null) {
            testPlanUOW = new TestPlanUOW();
        }
        if (testScenarioUOW == null) {
            testScenarioUOW = new TestScenarioUOW();
        }
        this.testPlan = testPlan;
        printMessageWithTestPlanProgress("Start of " + getClass().getSimpleName());
        if (verifyPrecondition()) {
             executePhase();
        }
        return testPlan;
    }

    /**
     * Each phase will have pre-conditions that should be satisfied in order to start the phase.
     * (ex: For starting the deploy-phase, the infra-phase should be successfully executed)
     * All those validations should be done within this method.
     *
     * @return if preconditions are verified successfully or not
     */
    abstract boolean verifyPrecondition();

    /**
     *
     */
    abstract void executePhase();

    void persistTestPlanProgress(TestPlanPhase phase, TestPlanStatus status) {
        logger.info("Updating testplan status " + testPlan.getStatus() + " --> " + status + " and " +
                "and phase " + testPlan.getPhase() + " --> " + phase);
        try {
            testPlan.setPhase(phase);
            testPlan.setStatus(status);
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }

    void persistTestPlanPhase(TestPlanPhase phase) {
        logger.info("Updating testplan phase " + testPlan.getPhase() + " --> " + phase);
        try {
            testPlan.setPhase(phase);
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }

    /**
     * Persists the test plan with the status.
     *
     * @param status   the status to set
     */
    void persistTestPlanStatus(TestPlanStatus status) {
        logger.info("Updating testplan status " + testPlan.getStatus() + " --> " + status);
        try {
            testPlan.setStatus(status);
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }

    /**
     * Persists the test plan to the database.
     */
    void persistTestPlan() {
        try {
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }

    TestPlan getTestPlan() {
        return this.testPlan;
    }

    TestScenarioUOW getTestScenarioUOW() {
        return this.testScenarioUOW;
    }

    public void setTestPlanUOW(TestPlanUOW testPlanUOW) {
        this.testPlanUOW = testPlanUOW;
    }

    public void setTestScenarioUOW(TestScenarioUOW testScenarioUOW) {
        this.testScenarioUOW = testScenarioUOW;
    }

    /**
     * Log a message
     * @param message message needs to be logged
     */
    void printMessage(String message) {
        logger.info("");
        logger.info(StringUtils.repeat("-", LINE_LENGTH));
        logger.info(message);
        logger.info(StringUtils.repeat("-", LINE_LENGTH));
        logger.info("");
    }

    /**
     * Log a message with the
     * @param message
     */
    private void printMessageWithTestPlanProgress(String message) {
        logger.info("");
        logger.info("----------------------" + message + "--------------------------");
        logger.info("TestPlan Status: " + testPlan.getStatus().toString());
        logger.info("TestPlan Phase: " + testPlan.getPhase().toString());
        logger.info("---------------------------------------------------------------");
        logger.info("");
    }

}
