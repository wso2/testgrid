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
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.nio.file.Paths;
import java.util.Properties;

/**
 * This class creates the infrastructure for running tests.
 */
public class ShellScriptProvider implements InfrastructureProvider {

    private static final Logger logger = LoggerFactory.getLogger(ShellScriptProvider.class);
    private static final String SHELL_SCRIPT_PROVIDER = "Shell Executor";
    private static final String WORKSPACE = "workspace";
    private static final String OUTPUT_DIR = "output-dir";

    @Override
    public String getProviderName() {
        return SHELL_SCRIPT_PROVIDER;
    }

    @Override
    public boolean canHandle(InfrastructureConfig infrastructureConfig) {
        return infrastructureConfig.getInfrastructureProvider() == InfrastructureConfig.InfrastructureProvider.SHELL;
    }

    @Override
    public void init() throws TestGridInfrastructureException {
        // empty
    }

    @Override
    public InfrastructureProvisionResult provision(TestPlan testPlan)
            throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(testPlan.getInfrastructureRepository()).toString();
        InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
        logger.info("Executing provisioning scripts...");
        try {
            Script createScript = getScriptToExecute(infrastructureConfig, Script.Phase.CREATE);
            Properties inputParameters = createScript.getInputParameters();
            final String workspace = TestGridUtil.getTestRunWorkspace(testPlan, false).toString();
            inputParameters.setProperty(WORKSPACE, workspace);
            // TODO: this is deprecated.
            inputParameters.setProperty(OUTPUT_DIR, workspace);

            String parameterString = TestGridUtil.getParameterString(null, inputParameters);
            ShellExecutor executor = new ShellExecutor(null);
            InfrastructureProvisionResult result = new InfrastructureProvisionResult();
            int exitCode = executor.executeCommand("bash " + Paths
                    .get(testPlanLocation, createScript.getFile()) + " " + parameterString);
            if (exitCode > 0) {
                logger.error(StringUtil.concatStrings("Error occurred while executing the infra-provision script. ",
                        "Script exited with a status code of ", exitCode));
                result.setSuccess(false);
            }
            result.setResultLocation(workspace);
            return result;

        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(String.format(
                    "Exception occurred while executing the infra-provision script for deployment-pattern '%s'",
                    infrastructureConfig.getProvisioners().get(0).getName()), e);
        } catch (TestGridException e) {
            throw new TestGridInfrastructureException(
                    "Exception occurred while executing the infra-create script " +
                            "when generating Test run artifact directory ", e);
        }

    }

    @Override
    public boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir)
            throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(infraRepoDir).toString();

        logger.info("Destroying test environment...");
        ShellExecutor executor = new ShellExecutor(null);
        try {

            if (executor.executeCommand("bash " + Paths
                    .get(testPlanLocation, getScriptToExecute(infrastructureConfig, Script.Phase.DESTROY)
                            .getFile())) == 0) {
                return true;
            }
        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(
                    "Exception occurred while executing the infra-destroy script " + "for deployment-pattern '"
                            + infrastructureConfig.getProvisioners().get(0).getName() + "'", e);
        }
        return false;
    }

    /**
     * This method returns the script matching the correct script phase.
     *
     * @param infrastructureConfig {@link InfrastructureConfig} object with current infrastructure configurations
     * @param scriptPhase          {@link Script.Phase} enum value for required script
     * @return the matching script from deployment configuration
     * @throws TestGridInfrastructureException if there is no matching script for phase defined
     */
    private Script getScriptToExecute(InfrastructureConfig infrastructureConfig, Script.Phase scriptPhase)
            throws TestGridInfrastructureException {

        for (Script script : infrastructureConfig.getProvisioners().get(0).getScripts()) {
            if (scriptPhase.equals(script.getPhase())) {
                return script;
            }
        }
        if (Script.Phase.CREATE.equals(scriptPhase)) {
            for (Script script : infrastructureConfig.getProvisioners().get(0).getScripts()) {
                if (script.getPhase() == null) {
                    return script;
                }
            }
        }
        throw new TestGridInfrastructureException("The infrastructure provisioner's script list doesn't "
                + "contain the script for '" + scriptPhase.toString() + "' phase");
    }
}
