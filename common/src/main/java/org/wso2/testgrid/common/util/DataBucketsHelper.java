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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A helper class to get the input and output directories of each
 * testgrid step.
 * <p>
 * A data bucket is a dir location where all the information
 * about the provisioned infrastructure, created deployment etc. can be
 * stored.
 * <p>
 * <ul><li>
 * The infrastructure provisioners should store the IP addresses of
 * each ec2 instance, IP/port of RDS instance, ssh keys etc.
 * </li><li>
 * The deployment creators should store the mgt console urls of each
 * product profile, login credentials among other info.
 * </li><li>
 * The test writers should store the test outputs - ie. jmeter jtl files,
 * surefire-reports
 * </li>
 * </ul>
 */
public class DataBucketsHelper {

    public static final String DATA_BUCKET_OUTPUT_DIR_NAME = "data-bucket";
    public static final String INFRA_OUT_FILE = "infrastructure.properties";
    public static final String DEPL_OUT_FILE = "deployment.properties";
    public static final String TEST_OUT_FILE = "tests.properties";
    public static final String TESTPLAN_PROPERTIES_FILE = "testplan-props.properties";
    private static final Logger logger = LoggerFactory.getLogger(DataBucketsHelper.class);
    private static boolean init = false;

    /**
     * Returns the path of infrastructure outputs (infra-outputs).
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/data-bucket
     * ex.~/.testgrid/jobs/wso2am/builds/two-node-depl_b158e122-78f8-11e8-adc0-fa7ae01bbebc_10/data-bucket
     *
     * @param testPlan test-plan
     * @return The dir location where this step's outputs are stored
     */
    public static Path getOutputLocation(TestPlan testPlan) {
        final Path dataBucketPath = getBuildOutputsDir(testPlan).resolve(DATA_BUCKET_OUTPUT_DIR_NAME);
        if (!init) {
            init(dataBucketPath);
        }
        return dataBucketPath;
    }

    /**
     * Returns the path of infrastructure inputs (infra-inputs).
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/data-bucket
     * ex.~/.testgrid/jobs/wso2am/builds/two-node-depl_b158e122-78f8-11e8-adc0-fa7ae01bbebc_10/data-bucket
     *
     * @param testPlan test-plan
     * @return The dir location where this step's inputs are stored
     */
    public static Path getInputLocation(TestPlan testPlan) {
        final Path dataBucketPath = getBuildOutputsDir(testPlan).resolve(DATA_BUCKET_OUTPUT_DIR_NAME);
        if (!init) {
            init(dataBucketPath);
        }
        return dataBucketPath;
    }

    /**
     * Get the build outputs dir
     *
     * @param testPlan testplan
     * @return Get the build outputs dir
     */
    public static Path getBuildOutputsDir(TestPlan testPlan) {
        String productName = testPlan.getJobName();
        productName = productName == null ? testPlan.getDeploymentPattern().getProduct().getName() : productName;
        String testPlanDirName = TestGridUtil.deriveTestPlanDirName(testPlan);
        String testgridHome = TestGridUtil.getTestGridHomePath();
        return Paths.get(testgridHome, TestGridConstants.TESTGRID_JOB_DIR, productName, TestGridConstants
                .TESTGRID_BUILDS_DIR, testPlanDirName);
    }

    /**
     * create the data bucket dir if it is missing.
     *
     * @param dataBucketPath path
     */
    private static void init(Path dataBucketPath) {
        try {
            if (!Files.exists(dataBucketPath)) {
                Files.createDirectories(dataBucketPath);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
