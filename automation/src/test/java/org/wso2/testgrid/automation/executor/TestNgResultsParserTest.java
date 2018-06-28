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

import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.testgrid.automation.exception.ParserInitializationException;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.automation.parser.ResultParser;
import org.wso2.testgrid.automation.parser.ResultParserFactory;
import org.wso2.testgrid.automation.parser.TestNgResultsParser;
import org.wso2.testgrid.common.DeploymentPattern;
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

    @BeforeMethod
    public void init() {
        System.setProperty(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY, TESTGRID_HOME);
        MockitoAnnotations.initMocks(this);
    }

    @Test(description = "Test for testing the functional test parser instance")
    public void testTestNgResultsParser() throws ResultParserException, ParserInitializationException, IOException {

        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);
        TestScenario testScenario = new TestScenario();
        testScenario.setDir("scenarioDir");
        TestPlan testPlan = new TestPlan();
        testPlan.setJobName("wso2");
        ScenarioConfig scenarioConfig = new ScenarioConfig();
        scenarioConfig.setTestType(TestGridConstants.TEST_TYPE_INTEGRATION);
        testPlan.setScenarioConfig(scenarioConfig);
        testPlan.setScenarioTestsRepository("resources");
        testScenario.setName("SolutionPattern22");
        testScenario.setTestPlan(testPlan);
        testPlan.setInfraParameters("{\"OS\": \"Ubuntu\"}");
        DeploymentPattern deploymentPatternDBEntry = new DeploymentPattern();
        deploymentPatternDBEntry.setName("deployment-pattern");
        testPlan.setDeploymentPattern(deploymentPatternDBEntry);

        final Path outputFile = DataBucketsHelper.getOutputLocation(testPlan)
                .resolve(TestNgResultsParser.RESULTS_INPUT_FILE);
        copyTestngResultsXml(outputFile);

        Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenario);
        Assert.assertTrue(parser.isPresent());
        Assert.assertTrue(parser.get() instanceof TestNgResultsParser);

        parser.get().parseResults();
        Assert.assertEquals(testScenario.getTestCases().size(), 5, "expected five test cases.");
    }

    private void copyTestngResultsXml(Path outputLocation) throws IOException {
        Files.copy(Paths.get("src", "test", "resources", TestNgResultsParser.RESULTS_INPUT_FILE),
                outputLocation, StandardCopyOption.REPLACE_EXISTING);
    }

}
