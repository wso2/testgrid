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
 */
package org.wso2.testgrid.common;

/**
 * This defines the possible phases of a Test-Plan.
 *
 */
public enum TestPlanPhase {
    PREPARATION_STARTED("PREPARATION_STARTED", 0),
    PREPARATION_ERROR("PREPARATION_ERROR", 5),
    PREPARATION_SUCCEEDED("PREPARATION_SUCCEEDED", 10),

    INFRA_PHASE_STARTED("INFRA_PHASE_STARTED", 20),
    INFRA_PHASE_ERROR("INFRA_PHASE_ERROR", 25),
    INFRA_PHASE_SUCCEEDED("INFRA_PHASE_SUCCEEDED", 30),

    DEPLOY_PHASE_STARTED("DEPLOY_PHASE_STARTED", 40),
    DEPLOY_PHASE_ERROR("DEPLOY_PHASE_ERROR", 45),
    DEPLOY_PHASE_SUCCEEDED("DEPLOY_PHASE_SUCCEEDED", 50),

    TEST_PHASE_STARTED("TEST_PHASE_STARTED", 60),
    TEST_PHASE_INCOMPLETE("TEST_PHASE_INCOMPLETE", 62),
    TEST_PHASE_ERROR("TEST_PHASE_ERROR", 65),
    TEST_PHASE_SUCCEEDED("TEST_PHASE_SUCCEEDED", 70);

    private final String testPlanPhase;
    private final int value;
    TestPlanPhase(String testPlanPhase, int value) {
        this.testPlanPhase = testPlanPhase;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.testPlanPhase;
    }

    /**
     * Get the number assigned to the given phase.
     * This can be used for comparison purposes.
     * For ex. we can determine whether a given test-plan has completed
     * infra stage if the #getValue >= 30.
     *
     * @return the number associated with the current phase.
     */
    public int getValue() {
        return this.value;
    }
}


