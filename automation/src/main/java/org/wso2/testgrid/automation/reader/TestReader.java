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

package org.wso2.testgrid.automation.reader;

import org.wso2.testgrid.automation.Test;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.common.TestScenario;

import java.util.List;

/**
 * Interface defining the behavior for test readers.
 *
 * @since 1.0.0
 */
public interface TestReader {

    /**
     * Returns a list of {@link Test} instances for the given test scenario and test location.
     *
     * @param testLocation locations in which the tests resides
     * @param scenario     test scenario associated with the tests
     * @return a list of {@link Test} instances for the given test scenario and test location
     * @throws TestAutomationException thrown when error on reading tests
     */
    List<Test> readTests(String testLocation, TestScenario scenario) throws TestAutomationException;
}
