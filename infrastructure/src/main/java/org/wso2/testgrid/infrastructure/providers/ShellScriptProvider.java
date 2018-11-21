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

package org.wso2.testgrid.infrastructure.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.ShellExecutor;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;

import java.nio.file.Paths;

/**
 * This class creates the infrastructure for running tests.
 */
public class ShellScriptProvider implements InfrastructureProvider {

    private static final Logger logger = LoggerFactory.getLogger(ShellScriptProvider.class);
    private static final String SHELL_SCRIPT_PROVIDER = "Shell Executor";

    @Override
    public String getProviderName() {
        return SHELL_SCRIPT_PROVIDER;
    }

    @Override
    public boolean canHandle(Script.ScriptType scriptType) {
        return scriptType == Script.ScriptType.SHELL;
    }

    @Override
    public void init(TestPlan testPlan) throws TestGridInfrastructureException  {

    }

    @Override
    public void cleanup(TestPlan testPlan) throws TestGridInfrastructureException {
        String scriptsLocation = testPlan.getScenarioTestsRepository();
        ShellExecutor shellExecutor = new ShellExecutor(Paths.get(scriptsLocation));
        for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
            if (scenarioConfig.getScripts() != null
                    && scenarioConfig.getScripts().size() > 0) {
                for (Script script : scenarioConfig.getScripts()) {
                    if (Script.Phase.DESTROY.equals(script.getPhase())) {
                        try {
                            String testInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                                    .toAbsolutePath().toString();
                            final String command = "bash " + script.getFile() + " --input-dir " + testInputsLoc;
                            int exitCode = shellExecutor.executeCommand(command);
                            if (exitCode > 0) {
                                throw new TestGridInfrastructureException(StringUtil.concatStrings(
                                        "Error while executing ", script.getFile(),
                                        ". Script exited with a non-zero exit code (exit code = ", exitCode, ")"));
                            }
                        } catch (CommandExecutionException e) {
                            throw new TestGridInfrastructureException("Error while executing " + script.getFile(), e);
                        }
                        break;
                    }
                }
            }
        }

    }

    @Override
    public InfrastructureProvisionResult provision(TestPlan testPlan, Script script)
            throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(testPlan.getInfrastructureRepository()).toString();
        InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
        logger.info("Executing provisioning scripts...");
        try {
            Script createScript = script;
            ShellExecutor executor = new ShellExecutor(null);
            InfrastructureProvisionResult result = new InfrastructureProvisionResult();
            String infraInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();
            String infraOutputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();

            final String command = "bash " + Paths.get(testPlanLocation, createScript.getFile())
                    + " --input-dir " + infraInputsLoc +  " --output-dir " + infraOutputsLoc;
            int exitCode = executor.executeCommand(command);
            if (exitCode > 0) {
                logger.error(StringUtil.concatStrings("Error occurred while executing the infra-provision script. ",
                        "Script exited with a status code of ", exitCode));
                result.setSuccess(false);
            }
            result.setResultLocation(testPlanLocation);
            return result;

        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(String.format(
                    "Exception occurred while executing the infra-provision script for deployment-pattern '%s'",
                    infrastructureConfig.getProvisioners().get(0).getName()), e);
        }

    }

    @Override
    public boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir,
                           TestPlan testPlan, Script script) throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(infraRepoDir).toString();

        logger.info("Destroying test environment...");
        ShellExecutor executor = new ShellExecutor(null);
        try {

            String testInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();
            final String command = "bash " + Paths
                    .get(testPlanLocation, script.getFile())
                    + " --input-dir " + testInputsLoc;
            int exitCode = executor.executeCommand(command);
            return exitCode == 0;
        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(
                    "Exception occurred while executing the infra-destroy script " + "for deployment-pattern '"
                            + infrastructureConfig.getProvisioners().get(0).getName() + "'", e);
        }
    }

}
