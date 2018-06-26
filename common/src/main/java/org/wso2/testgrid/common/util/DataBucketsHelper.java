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

package org.wso2.testgrid.common.util;

import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DataBucketsHelper {

    public static final String INFRA_INPUT_DIR = "infra-inputs";
    public static final String INFRA_OUTPUT_DIR = "infra-outputs";
    public static final String DEPL_OUTPUT_DIR = "deploy-outputs";
    public static final String TEST_OUTPUT_DIR = "test-outputs";

    /**
     * Returns the path of infrastructure outputs (infra-outputs).
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/infra-outputs
     * ex.~/.testgrid/jobs/wso2am/builds/two-node-depl_b158e122-78f8-11e8-adc0-fa7ae01bbebc_10/infra-outputs
     *
     * @param testPlan test-plan
     * @return The dir location where this step's outputs are stored
     */
    public static Path getInfrastructureOutputLocation(TestPlan testPlan) {
        return getBuildOutputsDir(testPlan).resolve(INFRA_OUTPUT_DIR);
    }

    /**
     * Returns the path of infrastructure outputs (infra-outputs).
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/deploy-outputs
     *
     * @param testPlan test-plan
     * @return The dir location where this step's outputs are stored
     */
    public static Path getDeploymentOutputLocation(TestPlan testPlan) {
        return getBuildOutputsDir(testPlan).resolve(DEPL_OUTPUT_DIR);
    }

    /**
     * Returns the path of infrastructure outputs (infra-outputs).
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/test-outputs
     *
     * @param testPlan test-plan
     * @return The dir location where this step's outputs are stored
     */
    public static Path getTestOutputLocation(TestPlan testPlan) {
        return getBuildOutputsDir(testPlan).resolve(TEST_OUTPUT_DIR);
    }

    /**
     * Returns the path of infrastructure inputs (infra-inputs).
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/infra-inputs
     * ex.~/.testgrid/jobs/wso2am/builds/two-node-depl_b158e122-78f8-11e8-adc0-fa7ae01bbebc_10/infra-inputs
     *
     * @param testPlan test-plan
     * @return The dir location where this step's inputs are stored
     */
    public static Path getInfrastructureInputLocation(TestPlan testPlan) {
        return getBuildOutputsDir(testPlan).resolve(INFRA_INPUT_DIR);
    }

    /**
     * Returns the path of depl inputs (infra-outputs).
     * In reality, deployment-inputs-location == infrastructure-outputs-location
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/infra-outputs
     *
     * @param testPlan test-plan
     * @return The dir location where this step's inputs are stored
     */
    public static Path getDeploymentInputLocation(TestPlan testPlan) {
        return getInfrastructureOutputLocation(testPlan);
    }

    /**
     * Returns the path of test inputs (depl-outputs).
     * In reality, test-inputs-location == depl-outputs-location
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/depl-outputs
     *
     * @param testPlan test-plan
     * @return The dir location where this step's inputs are stored
     */
    public static Path getTestInputLocation(TestPlan testPlan) {
        return getDeploymentOutputLocation(testPlan);
    }

    /**
     * Get the build outputs dir
     *
     * @param testPlan testplan
     * @return Get the build outputs dir
     */
    private static Path getBuildOutputsDir(TestPlan testPlan) {
        String productName = testPlan.getDeploymentPattern().getProduct().getName();
        String testPlanDirName = TestGridUtil.deriveTestPlanDirName(testPlan);
        return Paths.get(TestGridConstants.TESTGRID_JOB_DIR, productName, TestGridConstants.TESTGRID_BUILDS_DIR,
                testPlanDirName);
    }

}
