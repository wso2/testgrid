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
package org.wso2.carbon.testgrid.reporting;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.testgrid.reporting.beans.Result;
import org.wso2.carbon.testgrid.reporting.util.TestEnvironmentUtil;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

/**
 * Test class to test the functionality of the {@link TestReportEngine}.
 *
 * @since 1.0.0
 */
public class TestReportEngineTest {

    private static final String TEST_GRID_HOME_ENV_KEY = "TEST_GRID_HOME";

    @BeforeClass
    public void beforeTest() {
        // Set required environment variables
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("results");
        Assert.assertNotNull(resource);

        Path path = new File(resource.getFile()).toPath();

        TestEnvironmentUtil.setEnvironmentVariable(TEST_GRID_HOME_ENV_KEY, path.toAbsolutePath().toString());

        // TODO: Assert
    }

    @Test
    public void generateReportTest() throws ReportingException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("results/jmeteroutput.csv");
        Assert.assertNotNull(resource);

        Path path = new File(resource.getFile()).toPath();

        TestReportEngine testReportEngine = new TestReportEngine();
        testReportEngine.generateReport(path, Result.class);
    }

    @AfterClass
    public void afterTest() {
        // Unset required environment variables
        TestEnvironmentUtil.unsetEnvironmentVariable(TEST_GRID_HOME_ENV_KEY);
    }
}
