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

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.testgrid.automation.TestEngineImpl;
import org.wso2.carbon.testgrid.common.Database;
import org.wso2.carbon.testgrid.common.DeployerService;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.InfrastructureProvider;
import org.wso2.carbon.testgrid.common.OperatingSystem;
import org.wso2.carbon.testgrid.common.ProductTestPlan;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.common.exception.DeployerInitializationException;
import org.wso2.carbon.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.carbon.testgrid.common.exception.TestAutomationEngineException;
import org.wso2.carbon.testgrid.common.exception.TestGridConfigurationException;
import org.wso2.carbon.testgrid.common.exception.TestGridDeployerException;
import org.wso2.carbon.testgrid.common.exception.TestGridException;
import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.carbon.testgrid.common.exception.UnsupportedDeployerException;
import org.wso2.carbon.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.carbon.testgrid.common.util.EnvironmentUtil;
import org.wso2.carbon.testgrid.deployment.DeployerFactory;
import org.wso2.carbon.testgrid.deployment.deployers.PuppetDeployer;
import org.wso2.carbon.testgrid.infrastructure.InfrastructureProviderFactory;
import org.wso2.carbon.testgrid.infrastructure.providers.OpenStackProvider;
import org.wso2.carbon.testgrid.reporting.TestReportEngineImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Test class to test the functionality of the {@link TestGridMgtServiceImpl}.
 *
 * @since 0.9.0
 */
@PrepareForTest({InfrastructureProviderFactory.class, DeployerFactory.class, TestEngineImpl.class,
        TestReportEngineImpl.class, TestPlanExecutor.class})
@PowerMockIgnore({"javax.management.*"})
public class TestGridMgtServiceTest extends PowerMockTestCase {

    TestGridMgtService testGridMgtService = new TestGridMgtServiceImpl();
    ProductTestPlan plan;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeTest
    public void setHome() {
        EnvironmentUtil.setEnvironmentVariable(TestGridUtil.TESTGRID_HOME_ENV, "/tmp");
    }

    @Test
    public void isEnvironmentConfiguredTest() throws TestGridConfigurationException {
        Assert.assertTrue(testGridMgtService.isEnvironmentConfigured());
    }

    @Test
    public void addProductTestPlanTest() throws TestGridException {
        String repo = "https://github.com/sameerawickramasekara/test-grid-is-resources.git";
        String product = "WSO2_Identity_Server";
        String productVersion = "5.3.0";
        plan = testGridMgtService.addProductTestPlan(product, productVersion, repo);
        Assert.assertNotNull(plan);
        Assert.assertTrue(product.equalsIgnoreCase(plan.getProductName()));
        Assert.assertTrue(productVersion.equalsIgnoreCase(plan.getProductVersion()));
        Assert.assertNotNull(plan.getCreatedTimeStamp());
        Assert.assertNotNull(plan.getHomeDir());
        Assert.assertTrue(ProductTestPlan.Status.PLANNED.equals(plan.getStatus()));
        Assert.assertNotNull(plan.getInfrastructureMap());
        Assert.assertTrue(plan.getTestPlans().size() > 0);
        Assert.assertNotNull(plan.getTestPlans().get(0).getName());
        Assert.assertTrue(plan.getTestPlans().get(0).getTestScenarios().size() > 0);
    }

    @Test
    public void executeProductTestPlanTest()
            throws TestGridException, UnsupportedProviderException, InfrastructureProviderInitializationException,
            TestGridInfrastructureException, TestAutomationEngineException, TestGridDeployerException,
            DeployerInitializationException, UnsupportedDeployerException {
        String deploymentPattern = "single-node";

        String scenarioLocation = "/tmp/abc";
        TestScenario testScenario = Mockito.mock(TestScenario.class);
        Mockito.when(testScenario.getSolutionPattern()).thenReturn("Sample Test Scenario");

        List<TestScenario> testScenarios = new ArrayList<>();
        testScenarios.add(testScenario);

        TestPlan testPlan = Mockito.mock(TestPlan.class);

        Infrastructure infrastructure = Mockito.mock(Infrastructure.class);
        infrastructure.setName(deploymentPattern);
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
        Mockito.when(infrastructure.getName()).thenReturn(deploymentPattern);

        Deployment deployment = Mockito.mock(Deployment.class);

        Mockito.when(testPlan.getName()).thenReturn("Sample Test Plan");
        Mockito.when(testPlan.getDescription()).thenReturn("Test plan description");
        Mockito.when(testPlan.getDeploymentPattern()).thenReturn(deploymentPattern);
        Mockito.when(testPlan.getDeployerType()).thenReturn(TestPlan.DeployerType.PUPPET);
        Mockito.when(testPlan.getStatus()).thenReturn(TestPlan.Status.INFRASTRUCTURE_READY);
        Mockito.when(testPlan.getTestScenarios()).thenReturn(testScenarios);
        Mockito.when(testPlan.getHome()).thenReturn(scenarioLocation);
        Mockito.when(testPlan.getInfraRepoDir()).thenReturn(scenarioLocation);
        Mockito.when(testPlan.getTestRepoDir()).thenReturn(scenarioLocation);
        Mockito.when(testPlan.getDeployment()).thenReturn(deployment);

        CopyOnWriteArrayList<TestPlan> testPlans = new CopyOnWriteArrayList<>();
        testPlans.add(testPlan);

        ConcurrentHashMap<String, Infrastructure> infrastructureMap = new ConcurrentHashMap<>();
        infrastructureMap.put(infrastructure.getName(), infrastructure);

        ProductTestPlan productTestPlan = Mockito.mock(ProductTestPlan.class);
        productTestPlan.setInfrastructureMap(infrastructureMap);
        Mockito.when(productTestPlan.getProductName()).thenReturn("WSO2 Identity Server");
        Mockito.when(productTestPlan.getProductVersion()).thenReturn("5.4.0");
        Mockito.when(productTestPlan.getHomeDir()).thenReturn(scenarioLocation);
        Mockito.when(productTestPlan.getTestPlans()).thenReturn(testPlans);
        Mockito.when(productTestPlan.getInfrastructure(deploymentPattern)).thenReturn(infrastructure);
        Mockito.when(productTestPlan.getStatus()).thenReturn(ProductTestPlan.Status.COMPLETED);

        InfrastructureProvider provider = Mockito.mock(OpenStackProvider.class);

        DeployerService deployer = mock(PuppetDeployer.class);
        TestEngineImpl testEngine = mock(TestEngineImpl.class);
        TestReportEngineImpl testReportEngine = mock(TestReportEngineImpl.class);

        PowerMockito.mockStatic(InfrastructureProviderFactory.class);

        Mockito.when(InfrastructureProviderFactory.getInfrastructureProvider(infrastructure)).thenReturn(provider);
        Mockito.when(provider.createInfrastructure(infrastructure, scenarioLocation)).thenReturn(deployment);
        Mockito.when(provider.removeInfrastructure(infrastructure, scenarioLocation)).thenReturn(true);
        Mockito.when(provider.canHandle(infrastructure)).thenReturn(true);
        Mockito.when(provider.removeInfrastructure(infrastructure, scenarioLocation)).thenReturn(true);

        Mockito.when(deployer.deploy(testPlan.getDeployment())).thenReturn(deployment);
        Mockito.when(testEngine.runScenario(testScenario, scenarioLocation, deployment)).thenReturn(true);

        try {
            PowerMockito.whenNew(TestEngineImpl.class).withNoArguments().thenReturn(testEngine);
            PowerMockito.whenNew(TestReportEngineImpl.class).withNoArguments().thenReturn(testReportEngine);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new TestGridMgtServiceImpl().executeProductTestPlan(productTestPlan);
        Assert.assertTrue(true);
    }
}
