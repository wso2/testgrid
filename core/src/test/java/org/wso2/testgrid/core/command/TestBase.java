/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.testgrid.core.command;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.wso2.testgrid.automation.TestAutomationException;
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
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.core.ScenarioExecutor;
import org.wso2.testgrid.core.TestPlanExecutor;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestCaseUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;
import org.wso2.testgrid.infrastructure.InfrastructureCombinationsProvider;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

@PrepareForTest({ StringUtil.class, TestExecutorFactory.class })
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*" })
class TestBase extends PowerMockTestCase {

    private static final Logger logger = LoggerFactory.getLogger(RunTestPlanCommandTest.class);
    private static final String TESTGRID_HOME = Paths.get("target", "testgrid-home").toString();
    private static final String infraParamsString = "{\"operating_system\":\"ubuntu_16.04\"}";
    private static final String TESTPLAN_ID = "TP_1";
    private static final String DEFAULT_SCHEDULE = "manual";

    @InjectMocks
    protected RunTestPlanCommand runTestPlanCommand;

    @Mock
    protected InfrastructureCombinationsProvider infrastructureCombinationsProvider;

    @Mock
    protected ProductUOW productUOW;
    @Mock
    protected DeploymentPatternUOW deploymentPatternUOW;
    @Mock
    protected TestPlanUOW testPlanUOW;
    @Mock
    protected TestScenarioUOW testScenarioUOW;
    @Mock
    protected TestCaseUOW testCaseUOW;

    protected Product product;
    protected TestPlan testPlan;
    protected DeploymentPattern deploymentPattern;
    protected TestPlanExecutor testPlanExecutor;
    protected ScenarioExecutor scenarioExecutor;
    protected String actualTestPlanFileLocation;
    protected String workspaceDir;

    public TestBase(String workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    @BeforeMethod
    public void init() throws Exception {
        final String randomStr = StringUtil.generateRandomString(5);
        String productName = "wso2-" + randomStr;
        actualTestPlanFileLocation = Paths.get("target", "testgrid-home", TestGridConstants.TESTGRID_JOB_DIR,
                productName, TestGridConstants.PRODUCT_TEST_PLANS_DIR, "test-plan-01.yaml").toString();
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

        scenarioExecutor = new ScenarioExecutor(testScenarioUOW, testCaseUOW);
        testPlanExecutor = new TestPlanExecutor(scenarioExecutor, testPlanUOW, testScenarioUOW);
        testPlanExecutor = spy(testPlanExecutor);

        MockitoAnnotations.initMocks(this);

        when(testScenarioUOW.persistTestScenario(any(TestScenario.class))).thenAnswer(invocation -> invocation
                .getArguments()[0]);

        doMock();
    }

    private void doMock() throws TestGridDAOException, TestAutomationException {
        spy(StringUtil.class);

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
}
