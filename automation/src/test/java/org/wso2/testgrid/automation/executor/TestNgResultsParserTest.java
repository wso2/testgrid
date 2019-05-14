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
 */

package org.wso2.testgrid.automation.executor;

import org.apache.commons.io.FileUtils;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.automation.parser.ResultParser;
import org.wso2.testgrid.automation.parser.ResultParserFactory;
import org.wso2.testgrid.automation.parser.TestNgResultsParser;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.util.DataBucketsHelper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * To view log4j logs, add following line to #init method.
 * org.apache.log4j.BasicConfigurator.configure();
 */
public class TestNgResultsParserTest {

    private static final String TESTGRID_HOME = Paths.get("target", "testgrid-home").toString();
    private static final String SUREFIRE_REPORTS_DIR = "surefire-reports";
    private static final Path SUREFIRE_REPORTS_DIR_OUT = Paths.get("scenarios", "SolutionPattern22");
    private static final Path SUREFIRE_REPORTS_DIR_OUT_NEGATIVE = Paths.get("scenarios", "SolutionPattern33");

    private TestPlan testPlan;
    private TestScenario testScenario;
    private ScenarioConfig scenarioConfig;
    private Path testArtifactPath = Paths.get("src", "test", "resources", "artifacts");
    private TestScenario testScenarioNegative;

    @BeforeMethod
    public void init() throws IOException {
        System.setProperty(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY, TESTGRID_HOME);

        scenarioConfig = new ScenarioConfig();
        testPlan = new TestPlan();
        testPlan.setJobName("wso2");
        List<ScenarioConfig> scenarioConfigs = new ArrayList<>();
        scenarioConfig.setTestType(TestGridConstants.TEST_TYPE_INTEGRATION);
        scenarioConfig.setFile("");
        scenarioConfig.setOutputDir("");
        scenarioConfigs.add(scenarioConfig);
        testPlan.setScenarioConfigs(scenarioConfigs);
        testPlan.setScenarioTestsRepository("resources");
        testPlan.setInfraParameters("{\"OS\": \"Ubuntu\"}");

        testScenario = new TestScenario();
        testScenario.setDir("scenarioDir");
        testScenario.setOutputDir("");
        testScenario.setName("SolutionPattern22");
        testScenario.setTestPlan(testPlan);

        testScenarioNegative = new TestScenario();
        testScenarioNegative.setDir("scenarioDir2");
        testScenarioNegative.setOutputDir("");
        testScenarioNegative.setName("SolutionPattern33");
        testScenarioNegative.setTestPlan(testPlan);

        Product product = new Product();
        product.setName("wso2");
        DeploymentPattern deploymentPatternDBEntry = new DeploymentPattern();
        deploymentPatternDBEntry.setName("deployment-pattern");
        deploymentPatternDBEntry.setProduct(product);
        testPlan.setDeploymentPattern(deploymentPatternDBEntry);
        testPlan.setWorkspace(Paths.get(TESTGRID_HOME, TestGridConstants.TESTGRID_JOB_DIR,
                product.getName()).toString());

        final Path outputLocation = DataBucketsHelper.getTestOutputsLocation(testPlan)
                .resolve(SUREFIRE_REPORTS_DIR_OUT);
        copyTestngResultsXml(outputLocation);
        Files.createDirectories(
                DataBucketsHelper.getTestOutputsLocation(testPlan).resolve(SUREFIRE_REPORTS_DIR_OUT_NEGATIVE));

        MockitoAnnotations.initMocks(this);
    }

    @Test(description = "Test for testing the functional test parser instance")
    public void testTestNgResultsParser() throws ResultParserException {

        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenario, scenarioConfig);
        Assert.assertTrue(parser.isPresent());
        Assert.assertTrue(parser.get() instanceof TestNgResultsParser);

        parser.get().parseResults();
        Assert.assertEquals(testScenario.getTestCases().size(), 18, "generated test cases does not match.");
        final long successTestCases = testScenario.getTestCases().stream()
                .filter(tc -> Status.SUCCESS.equals(tc.getStatus())).count();
        final long failureTestCases = testScenario.getTestCases().stream()
                .filter(tc -> Status.FAIL.equals(tc.getStatus())).count();
        final long skipTestCases = testScenario.getTestCases().stream()
                .filter(tc -> Status.SKIP.equals(tc.getStatus())).count();
        Assert.assertEquals(successTestCases, 12, "success test cases does not match.");
        Assert.assertEquals(failureTestCases, 4, "failure test cases does not match.");
        Assert.assertEquals(skipTestCases, 2, "skip test cases does not match.");
    }

    @Test(description = "Negative Test for testing the functional test parser instance")
    public void testNegativeEmptyTestSuiteXmls() throws ResultParserException {
        Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenarioNegative, scenarioConfig);
        Assert.assertTrue(parser.isPresent());
        Assert.assertTrue(parser.get() instanceof TestNgResultsParser);

        parser.get().parseResults();
        Assert.assertEquals(testScenarioNegative.getTestCases().size(), 0, "generated test cases does not match.");
    }

    @Test
    public void testArchiveResults() throws Exception {
        Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenario, scenarioConfig);
        Assert.assertTrue(parser.isPresent());
        Assert.assertTrue(parser.get() instanceof TestNgResultsParser);

        parser.get().parseResults();
        parser.get().archiveResults();
        Assert.assertEquals(testScenario.getTestCases().size(), 18, "generated test cases does not match.");
        final long successTestCases = testScenario.getTestCases().stream()
                .filter(tc -> Status.SUCCESS.equals(tc.getStatus())).count();
        final long failureTestCases = testScenario.getTestCases().stream()
                .filter(tc -> Status.FAIL.equals(tc.getStatus())).count();
        Assert.assertEquals(successTestCases, 12, "success test cases does not match.");
        Assert.assertEquals(failureTestCases, 4, "failure test cases does not match.");
    }

    private void copyTestngResultsXml(Path outputLocation) throws IOException {
        FileUtils.copyDirectory(testArtifactPath.toFile(), outputLocation.toFile());

    }

    @AfterClass
    public void tearDown() throws Exception {
        final Path buildOutputsDir = DataBucketsHelper.getBuildOutputsDir(testPlan);
        FileUtils.deleteQuietly(buildOutputsDir.toFile());
    }
}
