/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.infrastructure.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.ShellExecutor;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * This class provide infrastructure for the kubernetes architecture.
 */
public class KubernetesProvider implements InfrastructureProvider {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesProvider.class);
    private static final String KUBERNETES_PROVIDER = TestPlan.DeployerType.KUBERNETES.toString();

    @Override
    public String getProviderName() {
        return KUBERNETES_PROVIDER;
    }

    @Override
    public boolean canHandle(Script.ScriptType scriptType) {
        return scriptType == Script.ScriptType.KUBERNETES;
    }

    @Override
    public void init(TestPlan testPlan) throws TestGridInfrastructureException {

    }

    @Override
    public void cleanup(TestPlan testPlan) throws TestGridInfrastructureException {

    }

    /**
     * This method invoke the infrastructure creation script which would create the
     * basic infrastructure in the Kubernetes Engine
     *
     * @param testPlan {@link TestPlan} with current test run specifications
     * @param script with the details of the infra creation script specifications
     * @return Returns the InfrastructureProvisionResult
     * @throws TestGridInfrastructureException when there is an error in the script
     */
    @Override
    public InfrastructureProvisionResult provision(TestPlan testPlan, Script script)
            throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(testPlan.getInfrastructureRepository()).toString();
        InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
        setInfraProperties(testPlan);
        logger.info("Executing provisioning scripts...");
        try {
            ShellExecutor executor = new ShellExecutor(null);
            InfrastructureProvisionResult result = new InfrastructureProvisionResult();
            String infraInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();

            String infraOutputsLoc = DataBucketsHelper.getOutputLocation(testPlan)
                    .toAbsolutePath().toString();

            final String command = "bash " + Paths.get(testPlanLocation, TestGridConstants.INFRA_SCRIPT)
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

    /**
     * This is used to invoke the destroy script which would be used to clean the resources created.
     *
     * @param infrastructureConfig an instance of a InfrastructureConfig in which Infrastructure should be removed.
     * @param infraRepoDir         location of the cloned repository related to infrastructure.
     * @param testPlan {@link TestPlan} with current test run specifications
     * @param script with the details of the infra creation script specifications
     * @return
     * @throws TestGridInfrastructureException when there is an error in the release infrastructure script
     */
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
                    .get(testPlanLocation, TestGridConstants.DESTROY_SCRIPT)
                    + " --input-dir " + testInputsLoc;
            int exitCode = executor.executeCommand(command);
            return exitCode == 0;
        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(
                    "Exception occurred while executing the infra-destroy script " + "for deployment-pattern '"
                            + infrastructureConfig.getFirstProvisioner().getName() + "'", e);
        }
    }

    /**
     * This is used to set the infra properties
     *
     * @param testPlan with current test run specifications
     */

    private void setInfraProperties(TestPlan testPlan) {
        final Path location = DataBucketsHelper.getInputLocation(testPlan)
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
        DeploymentConfig.DeploymentPattern deploymentPatternConfig = testPlan.getDeploymentConfig()
                .getDeploymentPatterns().get(0);
        try (OutputStream os = Files.newOutputStream(location, CREATE, APPEND)) {
            os.write(("\nname=" + deploymentPatternConfig.getName()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + location, e);
        }
    }

}
