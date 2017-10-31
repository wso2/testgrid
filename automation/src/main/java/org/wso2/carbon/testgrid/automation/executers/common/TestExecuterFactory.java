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

package org.wso2.carbon.testgrid.automation.executers.common;

import org.wso2.carbon.testgrid.automation.executers.JmeterExecuter;
import org.wso2.carbon.testgrid.automation.executers.TestNgExecuter;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;

/**
 *This class creates the Specefic executer for the test type.
 */
public class TestExecuterFactory {

    /**
     * This method returns the specefic test executer.
     * @param testTypeJmeter Test Type
     * @return the specific TestExecuter
     */
    public static TestExecuter getTestExecutor(String testTypeJmeter) {
        switch (testTypeJmeter) {
            case TestGridConstants.TEST_TYPE_JMETER:
                return new JmeterExecuter();
            case TestGridConstants.TEST_TYPE_TESTNG:
                return new TestNgExecuter();
            default:
                return null;

        }
    }
}
