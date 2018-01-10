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

package org.wso2.testgrid.test.base;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;

/**
 * Base Class for integration tests.
 *
 * @since 1.0
 */
public class IntegrationTestBase {

    /**
     * Init the environment for integration tests.
     *
     * @throws IOException
     */
    @BeforeSuite public void initEnvironment() throws IntegrationTestException {
        Utils.initialize();
    }

    /**
     * Cleans the environment after test execution
     * @throws IOException
     */
    @AfterSuite public void cleanEnvironment() throws IOException {
        // Clean the environment if required
    }
}
