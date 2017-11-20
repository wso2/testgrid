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
import org.wso2.testgrid.common.exception.TestGridConfigurationException;
import org.wso2.testgrid.common.exception.TestGridException;

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
     * @param  product the product which TestGrid is executing.
     * @param  productVersion the product version which TestGrid is executing.
     * @param  repository GIT repository url of the Product tests.
     * @return the status of the operation (success/failure)
     * @throws TestGridException If something goes wrong while adding the ProductTestPlan.
     */
    ProductTestPlan addProductTestPlan(String product, String productVersion, String repository) throws
            TestGridException;

    /**
     * This method triggers the execution of a ProductTestPlan.
     *
     * @param  productTestPlan an instance of ProductTestPlan which should be executed.
     * @return the status of the operation (success/failure)
     * @throws TestGridException If something goes wrong while executing the ProductTestPlan.
     */
    boolean executeProductTestPlan(ProductTestPlan productTestPlan) throws TestGridException;

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
