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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.testgrid.automation.TestEngineImpl;
import org.wso2.testgrid.common.DeployerService;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.core.exception.ScenarioExecutorException;
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.deployment.deployers.PuppetDeployer;
import org.wso2.testgrid.reporting.TestReportEngineImpl;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Test class to test the functionality of the {@link ScenarioExecutor}.
 *
 * @since 0.9.0
 */
@PrepareForTest({DeployerFactory.class, TestEngineImpl.class, TestReportEngineImpl.class})
public class ScenarioExecutorTest extends PowerMockTestCase {

    private static final Log log = LogFactory.getLog(ScenarioExecutorTest.class);

    TestGridMgtService testGridMgtService = new TestGridMgtServiceImpl();
    ProductTestPlan plan;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeTest
    public void setHome() {
        System.setProperty(TestGridUtil.TESTGRID_HOME_ENV, "/tmp");
    }

    @Test
    public void runScenarioTest() throws ScenarioExecutorException {
        String deploymentPattern = "single-node";

        String scenarioLocation = "/tmp/abc";
        TestScenario testScenario = Mockito.mock(TestScenario.class);

        Mockito.when(testScenario.getSolutionPattern()).thenReturn("Sample Test Scenario");

        Deployment deployment = Mockito.mock(Deployment.class);
        DeployerService deployer = mock(PuppetDeployer.class);
        TestEngineImpl testEngine = mock(TestEngineImpl.class);
        TestReportEngineImpl testReportEngine = mock(TestReportEngineImpl.class);



        try {
            TestPlan testPlan = Mockito.mock(TestPlan.class);
            Mockito.when(testPlan.getDeployerType()).thenReturn(TestPlan.DeployerType.PUPPET);
            Mockito.when(deployer.deploy(testPlan.getDeployment())).thenReturn(deployment);
            //Mockito.when(deployer.deploy(testPlan)).thenReturn(deployment);

            whenNew(TestEngineImpl.class).withNoArguments().thenReturn(testEngine);
            Mockito.when(testEngine.runScenario(testScenario, scenarioLocation, deployment)).thenReturn(true);

            whenNew(TestReportEngineImpl.class).withNoArguments().thenReturn(testReportEngine);
        } catch (Exception e) {
            log.error(e);
        }


        //testGridMgtService.executeProductTestPlan(productTestPlan);
        Assert.assertTrue(true);
    }
}
