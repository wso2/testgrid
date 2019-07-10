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
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.ShellExecutor;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;

import java.nio.file.Path;

/**
 * This class creates the infrastructure for running tests.
 */
public class ShellScriptProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger(ShellScriptProvider.class);

    public static InfrastructureProvisionResult provision(TestPlan testPlan, Path path)
            throws TestGridInfrastructureException {
        InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
        logger.info("Executing provisioning scripts...");
        try {
            ShellExecutor executor = new ShellExecutor(null);
            InfrastructureProvisionResult result = new InfrastructureProvisionResult();
            String infraInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();
            String infraOutputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();
            final String command = "bash " + path
                    + " --input-dir " + infraInputsLoc +  " --output-dir " + infraOutputsLoc;
            int exitCode = executor.executeCommand(command);
            if (exitCode > 0) {
                logger.error(StringUtil.concatStrings("Error occurred while executing the infra-provision script. ",
                        "Script exited with a status code of ", exitCode));
                result.setSuccess(false);
            }
            return result;

        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(String.format(
                    "Exception occurred while executing the infra-provision script for deployment-pattern '%s'",
                    infrastructureConfig.getFirstProvisioner().getName()), e);
        }

    }


    public static boolean release(InfrastructureConfig infrastructureConfig, TestPlan testPlan,
                                  Path path) throws TestGridInfrastructureException {
        logger.info("Destroying test environment...");
        ShellExecutor executor = new ShellExecutor(null); //todo
        try {

            String testInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();
            final String command = "bash " + path
                    + " --input-dir " + testInputsLoc;
            int exitCode = executor.executeCommand(command);
            return exitCode == 0;
        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(
                    "Exception occurred while executing the infra-destroy script " +
                            "for deployment-pattern '" + infrastructureConfig.getFirstProvisioner()
                            .getName() + "'", e);
        }
    }

}
