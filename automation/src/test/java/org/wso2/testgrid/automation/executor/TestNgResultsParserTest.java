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
import org.wso2.testgrid.automation.exception.ParserInitializationException;
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
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class TestNgResultsParserTest {

    private static final String TESTGRID_HOME = Paths.get("target", "testgrid-home").toString();
    private static final String SUREFIRE_REPORTS_DIR = "surefire-reports";
    private TestPlan testPlan;
    private TestScenario testScenario;
    private Path testArtifactPath = Paths.get("src", "test", "resources", "artifacts");

    @BeforeMethod
    public void init() {
        System.setProperty(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY, TESTGRID_HOME);

        testScenario = new TestScenario();
        testScenario.setDir("scenarioDir");
        testPlan = new TestPlan();
        testPlan.setJobName("wso2");
        ScenarioConfig scenarioConfig = new ScenarioConfig();
        scenarioConfig.setTestType(TestGridConstants.TEST_TYPE_INTEGRATION);
        testPlan.setScenarioConfig(scenarioConfig);
        testPlan.setScenarioTestsRepository("resources");
        testScenario.setName("SolutionPattern22");
        testScenario.setTestPlan(testPlan);
        testPlan.setInfraParameters("{\"OS\": \"Ubuntu\"}");

        Product product = new Product();
        product.setName("wso2");
        DeploymentPattern deploymentPatternDBEntry = new DeploymentPattern();
        deploymentPatternDBEntry.setName("deployment-pattern");
        deploymentPatternDBEntry.setProduct(product);
        testPlan.setDeploymentPattern(deploymentPatternDBEntry);
        MockitoAnnotations.initMocks(this);
    }

    @Test(description = "Test for testing the functional test parser instance")
    public void testTestNgResultsParser() throws ResultParserException, ParserInitializationException, IOException {

        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        final Path outputFile = DataBucketsHelper.getOutputLocation(testPlan).resolve(SUREFIRE_REPORTS_DIR)
                .resolve(TestNgResultsParser.RESULTS_INPUT_FILE);
        copyTestngResultsXml(outputFile);

        Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenario);
        Assert.assertTrue(parser.isPresent());
        Assert.assertTrue(parser.get() instanceof TestNgResultsParser);

        parser.get().parseResults();
        Assert.assertEquals(testScenario.getTestCases().size(), 9, "generated test cases does not match.");
        final long successTestCases = testScenario.getTestCases().stream().filter(tc -> Status.SUCCESS.equals(tc.getStatus())).count();
        final long failureTestCases = testScenario.getTestCases().stream().filter(tc -> Status.FAIL.equals(tc.getStatus())).count();
        final long skipTestCases = testScenario.getTestCases().stream()
                .filter(tc -> Status.SKIP.equals(tc.getStatus())).count();
        Assert.assertEquals(successTestCases, 6, "success test cases does not match.");
        Assert.assertEquals(failureTestCases, 3, "failure test cases does not match.");
        Assert.assertEquals(skipTestCases, 5, "skip test cases does not match.");
    }

    @Test
    public void testArchiveResults() throws Exception {
        Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenario);
        Assert.assertTrue(parser.isPresent());
        Assert.assertTrue(parser.get() instanceof TestNgResultsParser);

        final Path outputPath = DataBucketsHelper.getOutputLocation(testPlan);
        FileUtils.copyDirectory(testArtifactPath.resolve(SUREFIRE_REPORTS_DIR).toFile(),
                outputPath.resolve(SUREFIRE_REPORTS_DIR).toFile());
        FileUtils.copyFile(testArtifactPath.resolve("automation.log.rename").toFile(),
                outputPath.resolve("automation.log").toFile());

        parser.get().parseResults();
        parser.get().archiveResults();
        Assert.assertEquals(testScenario.getTestCases().size(), 9, "generated test cases does not match.");
        final long successTestCases = testScenario.getTestCases().stream().filter(tc -> Status.SUCCESS.equals(tc.getStatus())).count();
        final long failureTestCases = testScenario.getTestCases().stream().filter(tc -> Status.FAIL.equals(tc.getStatus())).count();
        Assert.assertEquals(successTestCases, 6, "success test cases does not match.");
        Assert.assertEquals(failureTestCases, 3, "failure test cases does not match.");
    }

    private void copyTestngResultsXml(Path outputLocation) throws IOException {

        Files.copy(testArtifactPath.resolve(SUREFIRE_REPORTS_DIR).resolve(TestNgResultsParser.RESULTS_INPUT_FILE),
                outputLocation, StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterClass
    public void tearDown() throws Exception {
        final Path buildOutputsDir = DataBucketsHelper.getBuildOutputsDir(testPlan);
        FileUtils.deleteQuietly(buildOutputsDir.toFile());
    }
}
