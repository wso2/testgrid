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
import org.wso2.testgrid.automation.parser.JMeterResultParser;
import org.wso2.testgrid.automation.parser.JMeterResultParserException;
import org.wso2.testgrid.automation.parser.JMeterResultParserFactory;
import org.wso2.testgrid.common.TestScenario;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Test class to test the functionality of the {@link JMeterExecutor} class.
 *
 * @since 1.0.0
 */
public class JMeterExecutorTest {

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(description = "Test for testing the functional test parser instance")
    public void testJMeterFunctionalTestParserInstance() throws JMeterResultParserException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testLocation = Paths
                .get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestScenario testScenario = new TestScenario();
        testScenario.setName("SolutionPattern22");
        Optional<JMeterResultParser> jMeterResultParser = JMeterResultParserFactory.
                getParser(testScenario, testLocation);
        Assert.assertTrue(jMeterResultParser.isPresent());
        Assert.assertTrue(jMeterResultParser.get() instanceof JMeterResultParser);
    }

    @Test(description = "Test for testing the functional test")
    public void testJMeterFunctionalTestParser() throws JMeterResultParserException {
        // Set cloned test plan location.
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-grid-is-resources");
        Assert.assertNotNull(resource);

        String testLocation = Paths
                .get(resource.getPath(), "SolutionPattern22", "Tests")
                .toAbsolutePath().toString();
        TestScenario testScenario = new TestScenario();
        testScenario.setName("SolutionPattern22");
        Optional<JMeterResultParser> jMeterResultParser = JMeterResultParserFactory.
                getParser(testScenario, testLocation);
        Assert.assertTrue(jMeterResultParser.isPresent());
        jMeterResultParser.get().parseResults();
        Assert.assertFalse(testScenario.getTestCases().isEmpty());
        Assert.assertTrue(testScenario.getTestCases().size() == 34);
    }
}
