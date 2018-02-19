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
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;

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
    public boolean canHandle(InfrastructureConfig infrastructureConfig) {
        return infrastructureConfig.getInfrastructureProvider() == InfrastructureConfig.InfrastructureProvider.SHELL;
    }

    @Override
    public void init() throws TestGridInfrastructureException {
        // empty
    }

    @Override
    public InfrastructureProvisionResult provision(InfrastructureConfig infrastructureConfig, String infraRepoDir)
            throws
            TestGridInfrastructureException {
        String testPlanLocation = Paths.get(infraRepoDir).toString();

        logger.info("Executing provisioning scripts...");
        try {
            ShellExecutor executor = new ShellExecutor(null);
            executor.executeCommand("bash " + Paths
                    .get(testPlanLocation, getScriptToExecute(infrastructureConfig, Script.Phase.CREATE)));
        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(String.format(
                    "Exception occurred while executing the infra-provision script for deployment-pattern '%s'",
                    infrastructureConfig.getProvisioners().get(0).getName()), e);
        }
        return new InfrastructureProvisionResult();
    }

    @Override
    public boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir)
            throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(infraRepoDir).toString();

        logger.info("Destroying test environment...");
        ShellExecutor executor = new ShellExecutor(null);
        try {

            if (executor.executeCommand("bash " + Paths
                    .get(testPlanLocation, getScriptToExecute(infrastructureConfig, Script.Phase.DESTROY)))) {
                return true;
            }
        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(
                    "Exception occurred while executing the infra-destroy script " + "for deployment-pattern '"
                            + infrastructureConfig.getProvisioners().get(0).getName() + "'", e);
        }
        return false;
    }

    private String getScriptToExecute(InfrastructureConfig infrastructureConfig, Script.Phase phase) {
        for (Script script : infrastructureConfig.getProvisioners().get(0).getScripts()) {
            if (phase == script.getPhase()) {
                return script.getFile();
            }
        }
        return null;
    }
}
