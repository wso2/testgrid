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
import org.wso2.carbon.testgrid.common.Database;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.OperatingSystem;
import org.wso2.carbon.testgrid.common.ProductTestPlan;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.common.exception.TestReportEngineException;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class to test the functionality of the {@link TestReportEngineImpl}.
 *
 * @since 1.0.0
 */
public class TestReportEngineTest {

    @Test
    public void generateReportTest() throws TestReportEngineException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("results");
        Assert.assertNotNull(resource);

        String scenarioLocation = new File(resource.getFile()).toPath().toAbsolutePath().toString();
        TestScenario testScenario = Mockito.mock(TestScenario.class);
        Mockito.when(testScenario.getSolutionPattern()).thenReturn("Sample Test Scenario");

        List<TestScenario> testScenarios = new ArrayList<>();
        testScenarios.add(testScenario);

        TestPlan testPlan = Mockito.mock(TestPlan.class);
        Infrastructure infrastructure = Mockito.mock(Infrastructure.class);
        OperatingSystem operatingSystem = Mockito.mock(OperatingSystem.class);
        Mockito.when(operatingSystem.getName()).thenReturn("Ubuntu");
        Mockito.when(operatingSystem.getVersion()).thenReturn("17.04");
        Database database = Mockito.mock(Database.class);
        Mockito.when(database.getEngine()).thenReturn(Database.DatabaseEngine.MYSQL);
        Mockito.when(database.getVersion()).thenReturn("5.7");
        Mockito.when(infrastructure.getDatabase()).thenReturn(database);
        Mockito.when(infrastructure.getClusterType()).thenReturn(Infrastructure.ClusterType.K8S);
        Mockito.when(infrastructure.getInstanceType()).thenReturn(Infrastructure.InstanceType.DOCKER_CONTAINERS);
        Mockito.when(infrastructure.getProviderType()).thenReturn(Infrastructure.ProviderType.OPENSTACK);
        Mockito.when(infrastructure.getOperatingSystem()).thenReturn(operatingSystem);
        testPlan.setInfrastructure(infrastructure);

        Mockito.when(testPlan.getName()).thenReturn("Sample Test Plan");
        Mockito.when(testPlan.getDescription()).thenReturn("Test plan description");
        Mockito.when(testPlan.getInfrastructure()).thenReturn(infrastructure);
        Mockito.when(testPlan.getDeploymentPattern()).thenReturn("Single Node Deployment");
        Mockito.when(testPlan.getDeployerType()).thenReturn(TestPlan.DeployerType.PUPPET);
        Mockito.when(testPlan.getStatus()).thenReturn(TestPlan.Status.SCENARIO_EXECUTION_COMPLETED);
        Mockito.when(testPlan.getTestScenarios()).thenReturn(testScenarios);
        Mockito.when(testPlan.getHome()).thenReturn(scenarioLocation);

        List<TestPlan> testPlans = new ArrayList<>();
        testPlans.add(testPlan);

        ProductTestPlan productTestPlan = Mockito.mock(ProductTestPlan.class);
        Mockito.when(productTestPlan.getProductName()).thenReturn("WSO2 Identity Server");
        Mockito.when(productTestPlan.getProductVersion()).thenReturn("5.4.0");
        Mockito.when(productTestPlan.getHomeDir()).thenReturn(scenarioLocation);
        Mockito.when(productTestPlan.getTestPlans()).thenReturn(testPlans);

        TestReportEngineImpl testReportEngineImpl = new TestReportEngineImpl();
        testReportEngineImpl.generateReport(productTestPlan);

        String fileName = productTestPlan.getProductName() + "-" + productTestPlan.getProductVersion() + "-" +
                          productTestPlan.getCreatedTimeStamp() + ".html";
        Path reportPathLocation = Paths.get(scenarioLocation)
                .resolve(fileName);

        File reportFile = new File(reportPathLocation.toAbsolutePath().toString());
        Assert.assertTrue(reportFile.exists());
        Assert.assertTrue(reportFile.isFile());
    }
}
