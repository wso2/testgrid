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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.ShellExecutor;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.EnvironmentUtil;
import org.wso2.testgrid.common.util.StringUtil;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for performing the tasks related to execution of single JMeter solution.
 *
 * @deprecated succeeded by {@link ShellTestExecutor}.
 * We do not need test tool specific executors at this moment.
 *
 * @since 1.0.0
 */
public class JMeterExecutor extends TestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ShellTestExecutor.class);
    public static final String JMETER_HOME = "JMETER_HOME";
    private String testLocation;
    private String testName;
    private TestScenario testScenario;

    @Override
    public void init(String testLocation, String testName, TestScenario testScenario) throws TestAutomationException {
        this.testName = testName;
        this.testLocation = testLocation;
        this.testScenario = testScenario;
    }

    @Override
    public void execute(String script, DeploymentCreationResult deploymentCreationResult)
            throws TestAutomationException {
        try {
            String jmeterHome = EnvironmentUtil.getSystemVariableValue(JMETER_HOME);
            if (jmeterHome == null) {
                logger.warn(JMETER_HOME + " environment variable is not set. JMeter test executions may fail.");
            } else {
                logger.info(JMETER_HOME + ": " + jmeterHome);
            }

            ShellExecutor shellExecutor = new ShellExecutor(Paths.get(testLocation));
            Map<String, String> environment = new HashMap<>();

            for (Host host : deploymentCreationResult.getHosts()) {
                environment.put(host.getLabel(), host.getIp());
            }
            int exitCode = shellExecutor.executeCommand("bash " + script, environment);
            if (exitCode > 0) {
                logger.error(StringUtil.concatStrings("Error occurred while executing the test: ", testName, ", at: ",
                        testScenario.getDir(), ". Script exited with a status code of ", exitCode));
            }

        } catch (CommandExecutionException e) {
            this.testScenario.setStatus(Status.ERROR);
            throw new TestAutomationException(String.format("Error executing scenario " +
                    "script%s", script), e);
        }
    }

}
