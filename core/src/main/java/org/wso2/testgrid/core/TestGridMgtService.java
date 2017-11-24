/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.testgrid.core;

import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.TestGridConfigurationException;
import org.wso2.testgrid.common.exception.TestGridException;

import java.nio.file.Path;

/**
 * This defines the contract of TestGridMgtService in which will serve as the main entry point to the TestGrid
 * Framework.
 */
public interface TestGridMgtService {

    /**
     * This method checks whether the system is configured properly to run TestGrid framework.
     *
     * @return true if the environment is configured properly.
     * @throws TestGridException If something goes wrong while adding the ProductTestPlan.
     */
    boolean isEnvironmentConfigured() throws TestGridConfigurationException;

    /**
     * This method adds a TestPlan to the TestGrid framework.
     *
     *
     * @param  product the product which TestGrid is executing.
     * @param  productVersion the product version which TestGrid is executing.
     * @return the status of the operation (success/failure)
     * @throws TestGridException If something goes wrong while adding the ProductTestPlan.
     */
    ProductTestPlan createProduct(String product, String productVersion, String repository) throws
            TestGridException;

    /**
     * This includes the persistence logic to store the overall information of
     * product/version/channel combination in a database.
     *
     * Persistence only happens if the above combination does not exist in the db.
     *
     * This method does not generate nor run test plan.
     *
     * @param productTestPlan test plan
     * @throws TestGridException if exception
     */
    void persistProduct(ProductTestPlan productTestPlan) throws
            TestGridException;

    /**
     *
     * This method generates TestPlan object model that from the given input parameters.
     *
     * @param testPlanPath  location of the yaml file.
     * @param testRepoDir   deployment repo directory.
     * @param infraRepoDir  infrastructure repo directory.
     * @param testRunDir    extracted location of the deployment repo
     * @return TestPlan object model
     * @throws TestGridException if an error occurred during test plan generation.
     */
     TestPlan generateTestPlan(Path testPlanPath, String testRepoDir, String infraRepoDir, String
            testRunDir) throws TestGridException;

    /**
     * This method persists the test plan across the stages of test execution process.
     *
     * @param testPlan        the test plan we need to persist
     * @param productTestPlan the product test plan DTO that contain the information u need.
     */
    void persistSingleTestPlan(TestPlan testPlan, ProductTestPlan productTestPlan) throws TestGridException;


    /**
     * This method retruves the productTestPlan for the given combination.
     *
     * @param productName Name of the Product.
     * @param productVersion Version of the Product.
     * @param channel Channel information.
     */
    void getProductTestPlan(String productName,String productVersion, String channel) throws TestGridException;

    /**
     * This method triggers the execution of a ProductTestPlan.
     *
     *
     * @param testPlan
     * @param  productTestPlan an instance of ProductTestPlan which should be executed.
     * @return the status of the operation (success/failure)
     * @throws TestGridException If something goes wrong while executing the ProductTestPlan.
     */
    boolean executeTestPlan(TestPlan testPlan, ProductTestPlan productTestPlan) throws TestGridException;

    /**
     * This method aborts the execution of a ProductTestPlan.
     *
     * @param  productTestPlan An instance of ProductTestPlan which should be aborted.
     * @return the status of the operation (success/failure)
     * @throws TestGridException If something goes wrong while aborting the execution of the TestPlan.
     */
    boolean abortTestPlan(ProductTestPlan productTestPlan) throws TestGridException;

    /**
     * This method fetches the status of a ProductTestPlan.
     *
     * @param  productTestPlan An instance of TestPlan which should be monitored.
     * @return the status of the TestPlan (success/failure)
     * @throws TestGridException If something goes wrong while checking the status of the ProductTestPlan.
     */
    ProductTestPlan.Status getStatus(ProductTestPlan productTestPlan) throws TestGridException;

}
