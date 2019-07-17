/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.testgrid.core.command;

import org.apache.commons.io.FileUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.executor.ShellTestExecutor;
import org.wso2.testgrid.automation.executor.TestExecutorFactory;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.TestgridYaml;
import org.wso2.testgrid.common.infrastructure.DefaultInfrastructureTypes;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.ScenarioExecutor;
import org.wso2.testgrid.core.TestPlanExecutor;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestCaseUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;
import org.wso2.testgrid.infrastructure.InfrastructureCombinationsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.testng.Assert.assertTrue;

@PrepareForTest({ StringUtil.class, TestExecutorFactory.class })
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*" })
public class RunTestPlanCommandTest extends PowerMockTestCase {

    private static final Logger logger = LoggerFactory.getLogger(RunTestPlanCommandTest.class);
    private static final String TESTGRID_HOME = Paths.get("target", "testgrid-home").toString();
    private static final String infraParamsString = "{\"operating_system\":\"ubuntu_16.04\"}";
    private static final String TESTPLAN_ID = "TP_1";
    private static final String DEFAULT_SCHEDULE = "manual";

    @InjectMocks
    private RunTestPlanCommand runTestPlanCommand;

    @Mock
    private InfrastructureCombinationsProvider infrastructureCombinationsProvider;

    @Mock
    private ProductUOW productUOW;
    @Mock
    private DeploymentPatternUOW deploymentPatternUOW;
    @Mock
    private TestPlanUOW testPlanUOW;
    @Mock
    private TestScenarioUOW testScenarioUOW;
    @Mock
    private TestCaseUOW testCaseUOW;

    private Product product;
    private TestPlan testPlan;
    private DeploymentPattern deploymentPattern;
    private TestPlanExecutor testPlanExecutor;
    private ScenarioExecutor scenarioExecutor;
    private String actualTestPlanFileLocation;
    private String workspaceDir;

    @BeforeMethod
    public void init() throws Exception {
        final String randomStr = StringUtil.generateRandomString(5);
        String productName = "wso2-" + randomStr;
        actualTestPlanFileLocation = Paths.get("target", "testgrid-home", TestGridConstants.TESTGRID_JOB_DIR,
                productName, TestGridConstants.PRODUCT_TEST_PLANS_DIR, "test-plan-01.yaml").toString();
        workspaceDir = Paths.get(TestGridUtil.getTestGridHomePath(), TestGridConstants.TESTGRID_JOB_DIR,
                productName).toString();
        runTestPlanCommand = new RunTestPlanCommand(productName, actualTestPlanFileLocation, workspaceDir);
        System.setProperty(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY, TESTGRID_HOME);

        this.product = new Product();
        product.setName(productName);

        this.deploymentPattern = new DeploymentPattern();
        deploymentPattern.setName("default-" + randomStr);
        deploymentPattern.setProduct(product);

        this.testPlan = new TestPlan();
        testPlan.setId(TESTPLAN_ID);
        testPlan.setTestRunNumber(1);
        testPlan.setDeploymentPattern(deploymentPattern);
        testPlan.setInfraParameters(infraParamsString);
        testPlan.setPhase(TestPlanPhase.INFRA_PHASE_SUCCEEDED);
        testPlan.setStatus(TestPlanStatus.RUNNING);
        testPlan.setDeployerType(TestPlan.DeployerType.SHELL);
        testPlan.setScenarioTestsRepository(Paths.get(workspaceDir, "/workspace/scenarioTests").toString());
        testPlan.setInfrastructureRepository(Paths.get(workspaceDir, "/workspace/infrastructure").toString());
        testPlan.setDeploymentRepository(Paths.get(workspaceDir, "/workspace/deployment").toString());
        testPlan.setKeyFileLocation(Paths.get(workspaceDir, "/workspace/testkey.pem").toString());
        testPlan.setWorkspace(workspaceDir);

        when(testScenarioUOW.persistTestScenario(any(TestScenario.class))).thenAnswer(invocation -> invocation
                .getArguments()[0]);

        scenarioExecutor = new ScenarioExecutor(testScenarioUOW, testCaseUOW);
        testPlanExecutor = new TestPlanExecutor(scenarioExecutor, testPlanUOW, testScenarioUOW);
        testPlanExecutor = spy(testPlanExecutor);

        MockitoAnnotations.initMocks(this);
    }

    @Test(dataProvider = "getJobConfigData")
    public void testExecute(String jobConfigFile) throws Exception {
        doMock();

        GenerateTestPlanCommand generateTestPlanCommand = new GenerateTestPlanCommand(product.getName(),
                jobConfigFile, infrastructureCombinationsProvider, productUOW,
                deploymentPatternUOW, testPlanUOW);
        generateTestPlanCommand.execute();

        Path actualTestPlanPath = Paths.get(actualTestPlanFileLocation);
        assertTrue(Files.exists(actualTestPlanPath));
        copyWorkspaceArtifacts();
        runTestPlanCommand.execute();

        Path testRunDirectory = DataBucketsHelper.getBuildOutputsDir(testPlan);
        assertTrue(Files.exists(testRunDirectory),
                "The test-run dir does not exist: " + testRunDirectory.toString());

        final Path infrastructurePath = Paths.get(testPlan.getInfrastructureRepository());
        assertTrue(Files.exists(infrastructurePath), "Infrastructure provision script failed to run. Dir does not "
                + "exist: " + infrastructurePath.toString());
    }

    private void copyWorkspaceArtifacts() throws IOException {
        String resourceDir = Paths.get("./src", "test", "resources", "workspace").toString();
        String destinationDir = Paths.get(workspaceDir, "/workspace").toString();
        FileUtils.copyDirectory(new File(resourceDir), new File(destinationDir));
        logger.info("Copying necessary artifacts to directory: " + destinationDir);
        resourceDir = Paths.get("src", "test", "resources", "workspace", "data-bucket").toString();
        destinationDir = Paths.get(workspaceDir, "/data-bucket").toString();
        FileUtils.copyDirectory(new File(resourceDir), new File(destinationDir));
        logger.info("Copying necessary artifacts to directory: " + destinationDir);
    }

    private void doMock() throws TestGridDAOException, TestAutomationException {
        spy(StringUtil.class);
        when(StringUtil.generateRandomString(anyInt())).thenReturn("");
        PowerMockito.mockStatic(TestExecutorFactory.class);
        when(TestExecutorFactory.getTestExecutor(any())).thenReturn(new ShellTestExecutor());

        InfrastructureParameter param = new InfrastructureParameter("ubuntu_16.04", DefaultInfrastructureTypes
                .OPERATING_SYSTEM, "{}", true);
        InfrastructureCombination comb1 = new InfrastructureCombination(param);
        when(infrastructureCombinationsProvider.getCombinations(any(TestgridYaml.class), eq(DEFAULT_SCHEDULE)))
                .thenReturn(Collections.singleton(comb1));

        logger.info("Product : " + product.getName());
        when(productUOW.persistProduct(anyString())).thenReturn(product);
        when(productUOW.getProduct(anyString())).thenReturn(Optional.of(product));
        when(deploymentPatternUOW.getDeploymentPattern(eq(product), anyString()))
                .thenReturn(Optional.of(deploymentPattern));

        doAnswer(invocation -> new TestScenario()).when(testScenarioUOW).persistTestScenario(any(TestScenario.class));
        // we need to further improve this
        when(testScenarioUOW.isFailedTestScenariosExist(anyObject())).thenReturn(false);
        when(testCaseUOW.isExistsFailedTests(anyObject())).thenReturn(false);

        when(testPlanUOW.persistTestPlan(any(TestPlan.class))).thenReturn(testPlan);
        when(testPlanUOW.getTestPlanById(TESTPLAN_ID)).thenReturn(Optional.of(testPlan));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Path testPlanPath = Paths.get(TESTGRID_HOME, TestGridConstants.TESTGRID_JOB_DIR, product.getName(),
                TestGridConstants.PRODUCT_TEST_PLANS_DIR, "test-plan-01.yaml");
        if (Files.exists(testPlanPath)) {
            FileUtils.forceDelete(testPlanPath.toFile());
        } else {
            Assert.fail("Failed to delete test-plan. Test plan does not exist: " + testPlanPath.toString());
        }
    }

    @DataProvider
    public Object[][] getJobConfigData() {
        return new Object[][] {
                { "src/test/resources/job-config.yaml" },
                { "src/test/resources/job-config2.yaml" }
        };
    }

}
