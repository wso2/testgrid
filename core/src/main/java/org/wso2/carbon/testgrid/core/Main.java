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

package org.wso2.carbon.testgrid.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;
import org.wso2.carbon.testgrid.common.exception.TestGridException;

import java.io.File;
import java.io.IOException;

/**
 * Created by harshan on 10/31/17.
 */
public class Main {

    private TestConfiguration getTestConfig() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(System.getenv(TestGridUtil.TESTGRID_HOME_ENV) + File.separator + "testConfig.json");
        try {
            return mapper.readValue(file, TestConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        TestGridMgtService testGridMgtService = new TestGridMgtServiceImpl();
        try {
            TestPlan plan = testGridMgtService.addTestPlan(new Main().getTestConfig());
            testGridMgtService.executeTestPlan(plan);
        } catch (TestGridException e) {
            e.printStackTrace();
        }
    }
}
