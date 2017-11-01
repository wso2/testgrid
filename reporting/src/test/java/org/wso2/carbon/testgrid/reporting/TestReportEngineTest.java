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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.testgrid.common.TestScenario;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class to test the functionality of the {@link TestReportEngine}.
 *
 * @since 1.0.0
 */
public class TestReportEngineTest {

    @Test
    public void generateReportTest() throws ReportingException {
        String reportFileName = "WSO2IS-5.4.0.html";
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("results");
        Assert.assertNotNull(resource);

        String scenarioLocation = new File(resource.getFile()).toPath().toAbsolutePath().toString();
        TestScenario testScenario = Mockito.mock(TestScenario.class);
        Mockito.when(testScenario.getScenarioLocation()).thenReturn(scenarioLocation);

        TestReportEngine testReportEngine = new TestReportEngine();
        testReportEngine.generateReport(testScenario);

        Path reportPathLocation = Paths.get(scenarioLocation)
                .resolve("Tests")
                .resolve("Results")
                .resolve(reportFileName);

        File reportFile = new File(reportPathLocation.toAbsolutePath().toString());
        Assert.assertTrue(reportFile.exists());
        Assert.assertTrue(reportFile.isFile());
    }
}
