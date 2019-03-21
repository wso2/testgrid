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
package org.wso2.testgrid.automation.executor;

import org.apache.commons.io.FileUtils;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.testgrid.automation.TestEngine;
import org.wso2.testgrid.automation.exception.JTLResultParserException;
import org.wso2.testgrid.automation.exception.ParserInitializationException;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.automation.parser.ResultParser;
import org.wso2.testgrid.automation.parser.ResultParserFactory;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.util.DataBucketsHelper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Test class to test the functionality of the {@link ShellTestExecutor} class.
 *
 * @since 1.0.0
 */
public class JMeterExecutorTest {

    private static final String TESTGRID_HOME = Paths.get("target", "testgrid-home").toString();
    private static final String JOB_NAME = "sample-job";
    private Path testArtifactPath = Paths.get("src", "test", "resources", "artifacts");

    @BeforeMethod
    public void init() {
        System.setProperty(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY, TESTGRID_HOME);
        MockitoAnnotations.initMocks(this);
    }

    @Test(description = "Test for testing the functional test parser instance")
    public void testJMeterFunctionalTestParserInstance() throws JTLResultParserException
            , ParserInitializationException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);
        TestScenario testScenario = new TestScenario();
        testScenario.setDir("scenarioDir");
        TestPlan testPlan = new TestPlan();
        List<ScenarioConfig> scenarioConfigs = new ArrayList<>();
        ScenarioConfig scenarioConfig = new ScenarioConfig();
        scenarioConfig.setTestType(TestGridConstants.TEST_TYPE_FUNCTIONAL);
        scenarioConfig.setFile("scenario.sh");
        scenarioConfigs.add(scenarioConfig);
        testPlan.setScenarioConfigs(scenarioConfigs);
        testPlan.setScenarioTestsRepository("resources");
        testScenario.setName("SolutionPattern22");
        testScenario.setTestPlan(testPlan);
        testScenario.setOutputDir("");
        testPlan.setWorkspace(
                Paths.get(TESTGRID_HOME, TestGridConstants.TESTGRID_JOB_DIR, JOB_NAME).toString());

        Optional<ResultParser> jMeterResultParser = ResultParserFactory.
                getParser(testPlan, testScenario, scenarioConfig);
        Assert.assertTrue(jMeterResultParser.isPresent());
    }

    @Test(description = "Test for testing the functional test")
    public void testJMeterFunctionalTestParser()
            throws ResultParserException, ParserInitializationException, IOException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testLocation = Paths
                .get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestScenario testScenario = new TestScenario();
        testScenario.setName("my-scenario");
        testScenario.setDir("");
        Product product = new Product();
        product.setName("sample-job");
        DeploymentPattern deploymentPatternDBEntry = new DeploymentPattern();
        deploymentPatternDBEntry.setName("deployment-pattern");
        deploymentPatternDBEntry.setProduct(product);

        TestPlan testPlan = new TestPlan();
        List<ScenarioConfig> scenarioConfigs = new ArrayList<>();
        ScenarioConfig scenarioConfig = new ScenarioConfig();
        scenarioConfig.setTestType(TestEngine.JMETER.toString());
        scenarioConfig.setFile("");
        scenarioConfigs.add(scenarioConfig);
        testPlan.setScenarioConfigs(scenarioConfigs);
        testPlan.setScenarioTestsRepository(testLocation);
        testPlan.setWorkspace(
                Paths.get(TESTGRID_HOME, TestGridConstants.TESTGRID_JOB_DIR, product.getName()).toString());
        testPlan.setDeploymentPattern(deploymentPatternDBEntry);
        testScenario.setTestPlan(testPlan);
        testScenario.setOutputDir("");

        Path outputPath = DataBucketsHelper.getTestOutputsLocation(testPlan)
                .resolve("scenarios").resolve(testScenario.getName());
        FileUtils.copyFile(testArtifactPath.resolve("scenario-results.jtl").toFile(),
                outputPath.resolve("scenario-results.jtl").toFile());

        Optional<ResultParser> jMeterResultParser = ResultParserFactory
                .getParser(testPlan, testScenario, scenarioConfig);
        Assert.assertTrue(jMeterResultParser.isPresent());
        jMeterResultParser.get().parseResults();
        Assert.assertFalse(testScenario.getTestCases().isEmpty());
        Assert.assertEquals(testScenario.getTestCases().size(), 34);
    }
}
