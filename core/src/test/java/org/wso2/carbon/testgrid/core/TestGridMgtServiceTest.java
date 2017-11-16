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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.testgrid.automation.TestEngineImpl;
import org.wso2.carbon.testgrid.common.Database;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.ProductTestPlan;
import org.wso2.carbon.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.carbon.testgrid.common.exception.TestGridConfigurationException;
import org.wso2.carbon.testgrid.common.exception.TestGridException;
import org.wso2.carbon.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.carbon.testgrid.common.util.EnvironmentUtil;
import org.wso2.carbon.testgrid.infrastructure.InfrastructureProviderFactory;
import org.wso2.carbon.testgrid.reporting.TestReportEngineImpl;

import java.io.IOException;
import java.net.URL;

/**
 * Test class to test the functionality of the {@link TestGridMgtServiceImpl}.
 *
 * @since 1.0.0
 */
@PrepareForTest({InfrastructureProviderFactory.class, TestGridUtil.class, TestEngineImpl.class,
        TestReportEngineImpl.class, TestPlanExecutor.class})
public class TestGridMgtServiceTest extends PowerMockTestCase {

    private static final Log log = LogFactory.getLog(TestGridMgtServiceTest.class);

    private static final String WSO2_PRODUCT = "WSO2 Identity Server";
    private static final String PRODUCT_VERSION = "5.4.0";
    private static final long TIME_STAMP = 12345L;
    private static final String GIT_REPO = "https://github.com/sameerawickramasekara/test-grid-is-resources.git";
    private static final String DEPLOYMENT_PATTERN = "single-node";
    private ProductTestPlan productTestPlan;

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
        Assert.assertTrue(new TestGridMgtServiceImpl().isEnvironmentConfigured());
    }

    @Test
    public void createTestDirectoryTest() throws TestGridException, IOException {
        String path = TestGridUtil.createTestDirectory(WSO2_PRODUCT, PRODUCT_VERSION, TIME_STAMP).get();
        Assert.assertNotNull(path);
        Assert.assertTrue(path.endsWith(TIME_STAMP + ""));
    }

    @Test (dependsOnMethods = {"isEnvironmentConfiguredTest", "createTestDirectoryTest"})
    public void parseProductTestPlanTest() throws TestGridException, IOException, GitAPIException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        PowerMockito.mockStatic(TestGridUtil.class);
        Mockito.when(TestGridUtil.createTestDirectory(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(java.util.Optional.ofNullable(resource.getPath()));
        Mockito.when(TestGridUtil.cloneRepository(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(resource.getPath());
        productTestPlan = new TestGridMgtServiceImpl()
                .addProductTestPlan(WSO2_PRODUCT, PRODUCT_VERSION, GIT_REPO);
        Assert.assertNotNull(productTestPlan);
        Assert.assertTrue(WSO2_PRODUCT.equals(productTestPlan.getProductName()));
        Assert.assertTrue(PRODUCT_VERSION.equals(productTestPlan.getProductVersion()));
        Assert.assertTrue(ProductTestPlan.Status.PLANNED.equals(productTestPlan.getStatus()));
        Assert.assertTrue(productTestPlan.getTestPlans().size() == 2);

        Assert.assertNotNull(productTestPlan.getInfrastructureMap().contains("two-node"));
        Infrastructure infrastructure = productTestPlan.getInfrastructureMap().get(DEPLOYMENT_PATTERN);
        Assert.assertNotNull(infrastructure);
        Assert.assertTrue(DEPLOYMENT_PATTERN.equals(infrastructure.getName()));
        Assert.assertTrue("K8S".equals(infrastructure.getClusterType().name()));
        Assert.assertTrue("OPENSTACK".equals(infrastructure.getProviderType().name()));
        Assert.assertTrue("DOCKER_CONTAINERS".equals(infrastructure.getInstanceType().name()));
        Assert.assertNotNull(infrastructure.getDatabase());
        Assert.assertTrue(Database.DatabaseEngine.MYSQL.equals(infrastructure.getDatabase().getEngine()));
        Assert.assertTrue("5.7".equals(infrastructure.getDatabase().getVersion()));
        Assert.assertNotNull(infrastructure.getOperatingSystem());
        Assert.assertTrue("Ubuntu".equals(infrastructure.getOperatingSystem().getName()));
        Assert.assertTrue("17.04".equals(infrastructure.getOperatingSystem().getVersion()));
        Assert.assertNotNull(infrastructure.getSecurityProperties());
        Assert.assertNotNull(infrastructure.getScripts());
        Assert.assertTrue(infrastructure.getScripts().size() == 2);
    }

    @Test (dependsOnMethods = "parseProductTestPlanTest")
    public void executeProductTestPlanTestWithUnsupportedProvider() throws UnsupportedProviderException,
            TestGridException, InfrastructureProviderInitializationException {
        PowerMockito.mockStatic(InfrastructureProviderFactory.class);
        Mockito.when(InfrastructureProviderFactory.getInfrastructureProvider(Mockito.anyObject()))
                .thenThrow(new UnsupportedProviderException());
        Assert.assertTrue(new TestGridMgtServiceImpl().executeProductTestPlan(productTestPlan));
    }

//    @Test
//    public void executeProductTestPlanTest() throws TestGridException, UnsupportedProviderException,
//            InfrastructureProviderInitializationException, TestGridInfrastructureException,
//            TestAutomationEngineException, TestGridDeployerException {
//
//
//        InfrastructureProvider provider = Mockito.mock(OpenStackProvider.class);
//
//        DeployerService deployer = mock(PuppetDeployer.class);
//        TestEngineImpl testEngine = mock(TestEngineImpl.class);
//        TestReportEngineImpl testReportEngine = mock(TestReportEngineImpl.class);
//
//        PowerMockito.mockStatic(InfrastructureProviderFactory.class);
//
//        Mockito.when(InfrastructureProviderFactory.getInfrastructureProvider(Mockito.anyObject())).
// thenReturn(provider);
//        Mockito.when(provider.createInfrastructure(Mockito.anyObject(), Mockito.anyObject())).
//                thenReturn(Mockito.mock(Deployment.class));
//        Mockito.when(provider.removeInfrastructure(Mockito.anyObject(), Mockito.anyObject())).thenReturn(true);
//        Mockito.when(provider.canHandle(Mockito.anyObject())).thenReturn(true);
//
//        Mockito.when(deployer.deploy(Mockito.anyObject())).thenReturn(Mockito.mock(Deployment.class));
//        Mockito.when(testEngine.runScenario(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).
//                thenReturn(true);
//
//        try {
//            PowerMockito.whenNew(DeployerServiceImpl.class).withNoArguments().thenReturn(deployer);
//            PowerMockito.whenNew(TestEngineImpl.class).withNoArguments().thenReturn(testEngine);
//            PowerMockito.whenNew(TestReportEngineImpl.class).withNoArguments().thenReturn(testReportEngine);
//        } catch (Exception e) {
//            log.error(e);
//        }
//
//        Assert.assertTrue(new TestGridMgtServiceImpl().executeProductTestPlan(productTestPlan));
//    }

//    @Test (expectedExceptions = TestGridException.class)
//    public void executeProductTestPlanTestWithReportingException() throws UnsupportedProviderException,
//            TestGridException, InfrastructureProviderInitializationException {
//        String deploymentPattern = "single-node";
//
//        String scenarioLocation = "/tmp/abc";
//        TestScenario testScenario = Mockito.mock(TestScenario.class);
//        Mockito.when(testScenario.getSolutionPattern()).thenReturn("Sample Test Scenario");
//
//        List<TestScenario> testScenarios = new ArrayList<>();
//        testScenarios.add(testScenario);
//
//        TestPlan testPlan = Mockito.mock(TestPlan.class);
//
//        Infrastructure infrastructure = Mockito.mock(Infrastructure.class);
//        infrastructure.setName(deploymentPattern);
//        OperatingSystem operatingSystem = Mockito.mock(OperatingSystem.class);
//        Mockito.when(operatingSystem.getName()).thenReturn("Ubuntu");
//        Mockito.when(operatingSystem.getVersion()).thenReturn("17.04");
//        Database database = Mockito.mock(Database.class);
//        Mockito.when(database.getEngine()).thenReturn(Database.DatabaseEngine.MYSQL);
//        Mockito.when(database.getVersion()).thenReturn("5.7");
//        Mockito.when(infrastructure.getDatabase()).thenReturn(database);
//        Mockito.when(infrastructure.getClusterType()).thenReturn(Infrastructure.ClusterType.K8S);
//        Mockito.when(infrastructure.getInstanceType()).thenReturn(Infrastructure.InstanceType.DOCKER_CONTAINERS);
//        Mockito.when(infrastructure.getProviderType()).thenReturn(Infrastructure.ProviderType.OPENSTACK);
//        Mockito.when(infrastructure.getOperatingSystem()).thenReturn(operatingSystem);
//        Mockito.when(infrastructure.getName()).thenReturn(deploymentPattern);
//
//        Mockito.when(testPlan.getName()).thenReturn("Sample Test Plan");
//        Mockito.when(testPlan.getDescription()).thenReturn("Test plan description");
//        Mockito.when(testPlan.getDeploymentPattern()).thenReturn(deploymentPattern);
//        Mockito.when(testPlan.getTestScenarios()).thenReturn(testScenarios);
//        Mockito.when(testPlan.getHome()).thenReturn(scenarioLocation);
//        Mockito.when(testPlan.getInfraRepoDir()).thenReturn(scenarioLocation);
//        Mockito.when(testPlan.getTestRepoDir()).thenReturn(scenarioLocation);
//        Mockito.when(testPlan.getDeployerType()).thenReturn(TestPlan.DeployerType.PUPPET);
//        Mockito.when(testPlan.getStatus()).thenReturn(TestPlan.Status.SCENARIO_EXECUTION_COMPLETED);
//
//        CopyOnWriteArrayList<TestPlan> testPlans = new CopyOnWriteArrayList<>();
//        testPlans.add(testPlan);
//
//        ConcurrentHashMap<String, Infrastructure> infrastructureMap = new ConcurrentHashMap<>();
//        infrastructureMap.put(infrastructure.getName(), infrastructure);
//
//        ProductTestPlan productTestPlan = Mockito.mock(ProductTestPlan.class);
//        productTestPlan.setInfrastructureMap(infrastructureMap);
//        Mockito.when(productTestPlan.getProductName()).thenReturn("WSO2 Identity Server");
//        Mockito.when(productTestPlan.getProductVersion()).thenReturn("5.4.0");
//        Mockito.when(productTestPlan.getHomeDir()).thenReturn(scenarioLocation);
//        Mockito.when(productTestPlan.getTestPlans()).thenReturn(testPlans);
//        Mockito.when(productTestPlan.getInfrastructure(deploymentPattern)).thenReturn(infrastructure);
//
//        PowerMockito.mockStatic(InfrastructureProviderFactory.class);
//        Mockito.when(InfrastructureProviderFactory.getInfrastructureProvider(infrastructure))
//                .thenThrow(new UnsupportedProviderException());
//
//        TestReportEngineImpl testReportEngine = mock(TestReportEngineImpl.class);
//        try {
//            PowerMockito.whenNew(TestReportEngineImpl.class).withNoArguments().thenReturn(testReportEngine);
//            Mockito.doThrow(new TestReportEngineException()).when(testReportEngine).
// generateReport(Mockito.anyObject());
//        } catch (Exception e) {
//            log.error(e);
//        }
//
//        new TestGridMgtServiceImpl().executeProductTestPlan(productTestPlan);
//    }
}
