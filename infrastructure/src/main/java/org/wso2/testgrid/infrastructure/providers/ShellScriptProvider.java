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

import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class creates the infrastructure for running tests.
 */
public class ShellScriptProvider implements InfrastructureProvider {

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
    public void init(TestPlan testPlan) throws TestGridInfrastructureException {

    }

    @Override
    public void cleanup(TestPlan testPlan) throws TestGridInfrastructureException {

    }

    @Override
    public InfrastructureProvisionResult provision(TestPlan testPlan, Script script)
            throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(testPlan.getInfrastructureRepository()).toString();
        Script createScript = script;
        InfrastructureProvisionResult result = ShellScriptProviderFactory.provision(testPlan,
                Paths.get(testPlanLocation, createScript.getFile()));
        return result;
    }

    @Override
    public boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir,
                           TestPlan testPlan, Script script) throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(infraRepoDir).toString();
        Path path = Paths
                    .get(testPlanLocation, script.getFile());
        boolean exitCode = ShellScriptProviderFactory.release(infrastructureConfig, testPlan, path);
        return exitCode;
    }

}
