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
import org.json.JSONObject;
import org.json.JSONTokener;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.executor.ShellTestExecutor;
import org.wso2.testgrid.automation.executor.TestExecutorFactory;

import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.config.TestgridYaml;
import org.wso2.testgrid.common.infrastructure.DefaultInfrastructureTypes;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import org.wso2.testgrid.core.ScenarioExecutor;
import org.wso2.testgrid.core.TestPlanExecutor;
import org.wso2.testgrid.core.util.JsonPropFileUtil;

import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestCaseUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;

import org.wso2.testgrid.infrastructure.InfrastructureCombinationsProvider;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@PrepareForTest({ StringUtil.class, TestExecutorFactory.class })
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*" })
public class PropJsonUtilTest extends PowerMockTestCase {

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

        TestPlan testPlan = FileUtil.readYamlFile(actualTestPlanPath.toString(), TestPlan.class);
        InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();

        JsonPropFileUtil jsonpropFileEditor = new JsonPropFileUtil();

        jsonpropFileEditor.persistInfraInputsGeneral(DataBucketsHelper.getInputLocation(testPlan)
                        .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE),
                DataBucketsHelper.getInputLocation(testPlan)
                        .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_JSONFILE), testPlan);

        InfrastructureProvisionResult provisionResult = new InfrastructureProvisionResult();

        for (Script script : infrastructureConfig.getFirstProvisioner().getScripts()) {
            if (!Script.Phase.DESTROY.equals(script.getPhase())) {
                jsonpropFileEditor.persistInfraInputs(script, DataBucketsHelper.getInputLocation(testPlan)
                                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE),
                        DataBucketsHelper.getInputLocation(testPlan)
                                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_JSONFILE));
                InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                        .getInfrastructureProvider(script);
                infrastructureProvider.init(testPlan);
                logger.info("");
                logger.info("--- executing script: " + script.getName() + ", file: " + script.getFile());
                InfrastructureProvisionResult aProvisionResult =
                        infrastructureProvider.provision(testPlan, script);
                addTo(provisionResult, aProvisionResult);


                Properties propertiesfile = readData(DataBucketsHelper.getInputLocation(testPlan)
                        .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE));
                JSONObject propjsonfile = readJsonData(DataBucketsHelper.getInputLocation(testPlan)
                        .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_JSONFILE));
                boolean assertionContainsScriptparams = true;
                for (String key : script.getInputParameters().keySet()) {
                    if (propertiesfile.containsKey(key))  {
                        assertionContainsScriptparams = assertionContainsScriptparams &&
                                propertiesfile.containsKey(key) &&
                                (propertiesfile.get(key)).equals(script.getInputParameters().get(key).toString());
                    } else {
                        assertionContainsScriptparams  = false;
                    }
                    if (propjsonfile.has("currentscript")) {
                        logger.info("has currentscript");
                        if (propjsonfile.getJSONObject("currentscript").has(key)) {
                            assertionContainsScriptparams = assertionContainsScriptparams &&
                                    propjsonfile.getJSONObject("currentscript").has(key) &&
                                    (propjsonfile.getJSONObject("currentscript").get(key).toString())
                                            .equals(script.getInputParameters().get(key).toString());
                        } else {
                            assertionContainsScriptparams  = false;
                        }
                    } else {
                        assertionContainsScriptparams  = false;
                    }
                    if (propjsonfile.has(script.getName())) {
                        if (propjsonfile.getJSONObject(script.getName()).has(key)) {
                            assertionContainsScriptparams = assertionContainsScriptparams &&
                                    propjsonfile.getJSONObject(script.getName()).has(key) &&
                                    (propjsonfile.getJSONObject(script.getName()).get(key).toString())
                                            .equals(script.getInputParameters().get(key).toString());

                        } else {
                            assertionContainsScriptparams  = false;
                        }
                    } else {
                        assertionContainsScriptparams  = false;
                    }
                }
                assertTrue(assertionContainsScriptparams, "Script Params are present");

                final Properties infraParameters = testPlan.getInfrastructureConfig().getParameters();
                final Properties jobProperties = testPlan.getJobProperties();
                Boolean assertionContainsGeneralInfraParams = true;

                for (Object key : infraParameters.keySet()) {
                    if (propertiesfile.containsKey(key)) {
                        assertionContainsGeneralInfraParams = assertionContainsGeneralInfraParams &&
                                propertiesfile.containsKey(key) &&
                                (propertiesfile.get(key)).equals(infraParameters.get(key).toString());
                    } else {
                        assertionContainsGeneralInfraParams = false;
                    }
                    if (propjsonfile.has("currentscript")) {
                        if (propjsonfile.getJSONObject("currentscript").has((String) key)) {
                            assertionContainsGeneralInfraParams = assertionContainsGeneralInfraParams &&
                                    propjsonfile.getJSONObject("currentscript").has((String) key) &&
                                    (propjsonfile.getJSONObject("currentscript").get((String) key)).toString()
                                            .equals(infraParameters.get(key).toString());
                        } else {
                            assertionContainsGeneralInfraParams = false;
                        }
                    } else {
                        assertionContainsGeneralInfraParams = false;
                    }
                    if (propjsonfile.has("general")) {
                        if (propjsonfile.getJSONObject("general").has((String) key)) {
                            assertionContainsGeneralInfraParams = assertionContainsGeneralInfraParams &&
                                    propjsonfile.getJSONObject("general").has((String) key) &&
                                    (propjsonfile.getJSONObject("general").get((String) key)).toString()
                                            .equals(infraParameters.get(key).toString());

                        } else {
                            assertionContainsGeneralInfraParams = false;
                        }
                    } else {
                        assertionContainsGeneralInfraParams = false;
                    }
                }

                assertTrue(assertionContainsGeneralInfraParams, "General Infra Params are present");

                if (propjsonfile.has("general")) {
                    assertEquals(propjsonfile.getJSONObject("general").length(),
                            infraParameters.size() + jobProperties.size(),
                            "general has wrong amount of values");
                }
                if (propjsonfile.has("currentscript")) {
                    assertEquals(propjsonfile.getJSONObject("currentscript").length(),
                            infraParameters.size() + jobProperties.size() + script.getInputParameters().size(),
                            "current script has wrong amount of values");
                    assertEquals(propertiesfile.size(),
                            infraParameters.size() + jobProperties.size() + script.getInputParameters().size(),
                            "prop file has wrong amount of values");
                }
                if (propjsonfile.has(script.getName())) {
                    assertEquals(propjsonfile.getJSONObject(script.getName()).length()
                            , script.getInputParameters().size(),  "script name has wrong amount of values");
                }



                jsonpropFileEditor.removeScriptParams(script, DataBucketsHelper.getInputLocation(testPlan)
                        .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE));
                jsonpropFileEditor.refillFromPropFile(DataBucketsHelper.getInputLocation(testPlan)
                                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE),
                        DataBucketsHelper.getInputLocation(testPlan)
                                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_JSONFILE));
            }
        }


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



    @DataProvider
    public Object[][] getJobConfigData() {
        return new Object[][] {
                { "src/test/resources/job-config.yaml" }
        };
    }

    private void addTo(InfrastructureProvisionResult provisionResult, InfrastructureProvisionResult aProvisionResult) {
        provisionResult.getProperties().putAll(aProvisionResult.getProperties());
        if (!aProvisionResult.isSuccess()) {
            provisionResult.setSuccess(false);
        }
    }

    private Properties readData(Path propFilePath) {
        InputStream propInputStream = null;
        Properties existingprops = new Properties();
        try {
            propInputStream = new FileInputStream(propFilePath.toString());
            existingprops.load(propInputStream);
        } catch (FileNotFoundException e) {
            logger.info(propFilePath + " Not created yet ignoring read property file step");
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + propFilePath);
        } finally {
            try {
                if (propInputStream != null) {
                    propInputStream.close();
                }
            } catch (Exception e) {
                logger.error("Failed to close Stream");
            }
        }
        return existingprops;
    }

    private JSONObject readJsonData(Path jsonFilePath) {
        InputStream jsonInputStream = null;
        JSONObject jsondata = null;
        try {
            jsonInputStream = new FileInputStream(jsonFilePath.toString());
            JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
            jsondata = new JSONObject(jsonTokener);
        } catch (IOException ex) {
            logger.info("Error json file not found");
        } finally {
            try {
                if (jsonInputStream != null) {
                    jsonInputStream.close();
                }
            } catch (Exception e) {
                logger.error("Failed to close Stream");
            }
        }
        return jsondata;
    }

}
