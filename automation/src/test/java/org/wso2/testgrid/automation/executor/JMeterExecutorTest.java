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

import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.testgrid.automation.exception.JTLResultParserException;
import org.wso2.testgrid.automation.exception.ParserInitializationException;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.automation.parser.ResultParser;
import org.wso2.testgrid.automation.parser.ResultParserFactory;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ScenarioConfig;

import java.net.URL;
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

    @BeforeMethod
    public void init() {
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
        Optional<ResultParser> jMeterResultParser = ResultParserFactory.
                getParser(testPlan, testScenario, scenarioConfig);
        Assert.assertTrue(jMeterResultParser.isPresent());
        Assert.assertTrue(jMeterResultParser.get() instanceof ResultParser);
    }

    @Test(description = "Test for testing the functional test")
    public void testJMeterFunctionalTestParser() throws ResultParserException, ParserInitializationException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testLocation = Paths
                .get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestScenario testScenario = new TestScenario();
        testScenario.setName("SolutionPattern22");
        testScenario.setDir("");
        TestPlan testPlan = new TestPlan();
        List<ScenarioConfig> scenarioConfigs = new ArrayList<>();
        ScenarioConfig scenarioConfig = new ScenarioConfig();
        scenarioConfig.setTestType(TestGridConstants.TEST_TYPE_FUNCTIONAL);
        scenarioConfig.setFile("");
        scenarioConfigs.add(scenarioConfig);
        testPlan.setScenarioConfigs(scenarioConfigs);
        testPlan.setScenarioTestsRepository(testLocation);
        testPlan.setWorkspace(Paths.get("src", "test", "resources").toString());
        testScenario.setTestPlan(testPlan);
        testScenario.setOutputDir("");
        Optional<ResultParser> jMeterResultParser = ResultParserFactory
                .getParser(testPlan, testScenario, scenarioConfig);
        Assert.assertTrue(jMeterResultParser.isPresent());
        jMeterResultParser.get().parseResults();
        Assert.assertFalse(testScenario.getTestCases().isEmpty());
        Assert.assertTrue(testScenario.getTestCases().size() == 34);
    }
}
