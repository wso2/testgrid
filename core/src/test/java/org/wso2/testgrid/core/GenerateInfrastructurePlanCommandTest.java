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
 *
 */

package org.wso2.testgrid.core;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import org.wso2.testgrid.automation.TestEngineImpl;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.core.command.GenerateInfrastructurePlanCommand;
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.reporting.TestReportEngineImpl;

import java.io.File;
import java.io.IOException;

/**
 * Test class to test the functionality of the {@link ScenarioExecutor}.
 *
 * @since 0.9.0
 */
@PrepareForTest({DeployerFactory.class, TestEngineImpl.class, TestReportEngineImpl.class})
@PowerMockIgnore({"javax.management.*"})
public class GenerateInfrastructurePlanCommandTest extends PowerMockTestCase {

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeTest
    public void setHome() {
        System.setProperty(TestGridUtil.TESTGRID_HOME_ENV, "/tmp");
    }

    @Test
    public void runGenerateInfraPlanTest() throws TestGridException, IOException {

        String outputPath = "target/output.json";
        GenerateInfrastructurePlanCommand cmd = new GenerateInfrastructurePlanCommand();
        cmd.setTemplateLocation("src/test/resources/infra-template.yaml");
        cmd.setOutputFileName(outputPath);
        cmd.setInputParameters("DatabaseEngine=MySQL DatabaseVersion=5.6 JDK=JDK8");
        cmd.execute();

        String output = Files.readFile(new File(outputPath));
        String expectedOutput = Files.readFile(new File("src/test/resources/expected-output-infra-plan.yaml"));

        Assert.assertTrue(output.contains("MySQL"));
        Assert.assertTrue(output.contains("5.6"));
        Assert.assertFalse(output.contains("#_DatabaseEngine_#"));
        Assert.assertFalse(output.contains("#_DatabaseVersion_#"));

        Assert.assertEquals(output, expectedOutput, "Generated infra plan is not correct.");
   }
}
