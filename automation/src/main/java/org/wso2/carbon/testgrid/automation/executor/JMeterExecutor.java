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

package org.wso2.carbon.testgrid.automation.executor;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.wso2.carbon.testgrid.automation.TestAutomationException;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Host;
import org.wso2.carbon.testgrid.common.Port;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;
import org.wso2.carbon.testgrid.common.util.EnvironmentUtil;
import org.wso2.carbon.testgrid.common.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Responsible for performing the tasks related to execution of single JMeter solution.
 *
 * @since 1.0.0
 */
public class JMeterExecutor implements TestExecutor {

    private String jMeterHome;
    private String testLocation;
    private String testName;

    @Override
    public void execute(String script, Deployment deployment) throws TestAutomationException {
        StandardJMeterEngine jMeterEngine = new StandardJMeterEngine();
        if (StringUtil.isStringNullOrEmpty(testName) || StringUtil.isStringNullOrEmpty(testLocation) ||
            StringUtil.isStringNullOrEmpty(jMeterHome)) {
            throw new TestAutomationException(
                    StringUtil.concatStrings("JMeter Executor not initialised properly.", "{ Test Name: ", testName,
                            ", Test Location: ", testLocation, ", JMeter Home: ", jMeterHome, "}"));
        }
        overrideJMeterConfig(testLocation, testName, deployment); // Override JMeter properties for current deployment.
        JMeterUtils.setJMeterHome(jMeterHome);
        JMeterUtils.initLocale();
        try {
            SaveService.loadProperties();
        } catch (IOException e) {
            throw new TestAutomationException("Error occurred when initialising JMeter save service");
        }

        HashTree testPlanTree;
        try {
            testPlanTree = SaveService.loadTree(new File(script));
        } catch (IOException e) {
            throw new TestAutomationException("Error occurred when loading test script.", e);
        }

        Summariser summariser = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summariser = new Summariser(summariserName);
        }

        Path scriptFileName = Paths.get(script).getFileName();
        if (scriptFileName == null) {
            throw new TestAutomationException(StringUtil.concatStrings("Script file ", script, " cannot be located."));
        }
        String resultFile =
                Paths.get(testLocation, "Results", "Jmeter", scriptFileName + ".xml").toAbsolutePath().toString();
        ResultCollector resultCollector = new ResultCollector(summariser);

        resultCollector.setFilename(resultFile);
        testPlanTree.add(testPlanTree.getArray()[0], resultCollector);

        // Run JMeter Test
        jMeterEngine.configure(testPlanTree);
        jMeterEngine.run();
        jMeterEngine.exit();
    }

    @Override
    public void init(String testLocation, String testName) throws TestAutomationException {
        jMeterHome = EnvironmentUtil.getSystemVariableValue(TestGridConstants.JMETER_HOME);
        if (StringUtil.isStringNullOrEmpty(jMeterHome)) {
            throw new TestAutomationException(
                    StringUtil.concatStrings(TestGridConstants.JMETER_HOME, " environment variable not set."));
        }

        this.testName = testName;
        this.testLocation = testLocation;
    }

    /**
     * Overrides the JMeter properties with the properties required for the current deployment.
     *
     * @param testLocation directory location of the tests
     * @param testName     test name
     * @param deployment   deployment details of the current pattern
     */
    private void overrideJMeterConfig(String testLocation, String testName, Deployment deployment) {
        Path path = Paths.get(testLocation, "JMeter", testName, "src", "test", "resources", "user.properties");
        if (Files.exists(path)) {
            JMeterUtils.loadJMeterProperties(path.toAbsolutePath().toString());
            for (Host host : deployment.getHosts()) {
                JMeterUtils.setProperty(host.getLabel(), host.getIp());
                for (Port port : host.getPorts()) {
                    JMeterUtils.setProperty(port.getProtocol(), String.valueOf(port.getPortNumber()));
                }
            }
        }
    }
}
