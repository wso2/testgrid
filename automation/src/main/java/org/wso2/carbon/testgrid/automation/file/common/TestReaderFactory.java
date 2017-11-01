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

package org.wso2.carbon.testgrid.automation.file.common;

import org.wso2.carbon.testgrid.automation.file.JmeterTestReader;
import org.wso2.carbon.testgrid.automation.file.TestNGTestReader;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;

/**
 * The factory class to get a Test reader implementation.
 */
public class TestReaderFactory {
    /**
     * This method returns the TestReader implementation of the given type.
     *
     * @param testType Type of the tests as a String.
     * @return object of TestReader.
     */
    public static TestReader getTestReader(String testType) {
        switch (testType) {
            case TestGridConstants.TEST_TYPE_JMETER:
                return new JmeterTestReader();

            case TestGridConstants.TEST_TYPE_TESTNG:
                return new TestNGTestReader();

            default:
                return null;
        }
    }
}
