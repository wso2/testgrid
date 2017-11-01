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

package org.wso2.carbon.testgrid.automation.file;

import org.wso2.carbon.testgrid.automation.beans.Test;
import org.wso2.carbon.testgrid.automation.exceptions.TestReaderException;
import org.wso2.carbon.testgrid.automation.file.common.TestReader;

import java.util.List;

/**
 * This class is responsible for reading testNG tests for TestGrid framework.
 */
public class TestNGTestReader implements TestReader {

    @Override
    public List<Test> readTests(String testLocation) throws TestReaderException {
        return null;
    }
}
