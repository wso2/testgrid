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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.testgrid.core.phase;

import org.wso2.testgrid.common.GrafanaDashboardHandler;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * This class includes the implementation of the infrastructure-provisioning phase.
 */
public class InfraPhase extends Phase {

    @Override
    boolean verifyPrecondition() {
        if (getTestPlan().getPhase().equals(TestPlanPhase.PREPARATION_SUCCEEDED) &&
                getTestPlan().getStatus().equals(TestPlanStatus.RUNNING)) {
            return true;
        } else {
                logger.error("PREPARATION phase was not succeeded for test-plan: " + getTestPlan().getId() + ". Hence" +
                        "not starting other phases..");
                persistTestPlanProgress(TestPlanPhase.PREPARATION_ERROR, TestPlanStatus.ERROR);
            return false;
        }
    }

    @Override
    void executePhase() {
        // Provision infrastructure
        persistTestPlanPhase(TestPlanPhase.INFRA_PHASE_STARTED);
        InfrastructureProvisionResult infrastructureProvisionResult =
                provisionInfrastructure();
        //setup product performance dashboard
        if (infrastructureProvisionResult.isSuccess()) {
            GrafanaDashboardHandler dashboardSetup = new GrafanaDashboardHandler(getTestPlan().getId());
            dashboardSetup.initDashboard();
            persistTestPlanPhase(TestPlanPhase.INFRA_PHASE_SUCCEEDED);
        }
        getTestPlan().setInfrastructureProvisionResult(infrastructureProvisionResult);
    }

    /**
     * Sets up infrastructure for the given {@link InfrastructureConfig}.
     */
    private InfrastructureProvisionResult provisionInfrastructure() {
        InfrastructureConfig infrastructureConfig = getTestPlan().getInfrastructureConfig();
        try {
            if (infrastructureConfig == null) {
                persistTestPlanProgress(TestPlanPhase.INFRA_PHASE_ERROR, TestPlanStatus.ERROR);
                throw new TestPlanExecutorException(StringUtil
                        .concatStrings("Unable to locate infrastructure descriptor for deployment pattern '",
                                getTestPlan().getDeploymentPattern(), "', in TestPlan"));
            }

            printMessage("\t\t Provisioning infrastructure: " + infrastructureConfig.getFirstProvisioner().getName());

            persistInfraInputs();
            InfrastructureProvisionResult provisionResult = new InfrastructureProvisionResult();

            for (Script script : infrastructureConfig.getFirstProvisioner().getScripts()) {
                if (!Script.Phase.DESTROY.equals(script.getPhase())) {
                    InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                            .getInfrastructureProvider(script);
                    infrastructureProvider.init(getTestPlan());
                    logger.info("--- executing script: " + script.getName() + ", file: " + script.getFile());
                    InfrastructureProvisionResult aProvisionResult =
                            infrastructureProvider.provision(getTestPlan(), script);
                    addTo(provisionResult, aProvisionResult);
                }
            }

            provisionResult.setName(infrastructureConfig.getFirstProvisioner().getName());
            //TODO: remove. deploymentScriptsDir is deprecated now in favor of DeploymentConfig.
            provisionResult.setDeploymentScriptsDir(Paths.get(getTestPlan().getDeploymentRepository()).toString());
            logger.debug("Infrastructure provision result: " + provisionResult);
            return provisionResult;
        } catch (TestGridInfrastructureException e) {
            persistTestPlanProgress(TestPlanPhase.INFRA_PHASE_ERROR, TestPlanStatus.ERROR);
            String msg = StringUtil
                    .concatStrings("Error on infrastructure creation for deployment pattern '",
                            getTestPlan().getDeploymentPattern().getName(), "'");
            logger.error(msg, e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            persistTestPlanProgress(TestPlanPhase.INFRA_PHASE_ERROR, TestPlanStatus.ERROR);
            String msg = StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            getTestPlan().getDeploymentPattern(), "', in TestPlan");
            logger.error(msg, e);
        } catch (RuntimeException e) {
            // Catching the Exception here since we need to catch and gracefully handle all exceptions.
            persistTestPlanProgress(TestPlanPhase.INFRA_PHASE_ERROR, TestPlanStatus.ERROR);
            logger.error("Runtime exception while provisioning the infrastructure: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catching the Exception here since we need to catch and gracefully handle all exceptions.
            persistTestPlanProgress(TestPlanPhase.INFRA_PHASE_ERROR, TestPlanStatus.ERROR);
            logger.error("Unknown exception while provisioning the infrastructure: " + e.getMessage(), e);
        } finally {
            // Move testplan-props.properties out of data bucket to avoid exposing to the test execution phase.
            Path testplanPropsFilePath = DataBucketsHelper.getInputLocation(getTestPlan())
                    .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
            logger.info("Moving testplan.properties file from data bucket");
            File testPlanPropsFile = testplanPropsFilePath.toFile();
            if (!testPlanPropsFile.renameTo(new File(Paths.get(getTestPlan().getWorkspace(),
                    DataBucketsHelper.TESTPLAN_PROPERTIES_FILE).toString()))) {
                logger.error("Error while moving " + testPlanPropsFile);
            }
        }

        InfrastructureProvisionResult infrastructureProvisionResult = new InfrastructureProvisionResult();
        infrastructureProvisionResult.setSuccess(false);
        logger.debug("Infrastructure provision result: " + infrastructureProvisionResult);
        return infrastructureProvisionResult;
    }

    private void addTo(InfrastructureProvisionResult provisionResult, InfrastructureProvisionResult aProvisionResult) {
        provisionResult.getProperties().putAll(aProvisionResult.getProperties());
        if (!aProvisionResult.isSuccess()) {
            provisionResult.setSuccess(false);
        }
    }

    /**
     * The infra-provision.sh / deploy.sh / run-scenario.sh receive the test plan
     * configuration as a properties file.
     *
     */
    private void persistInfraInputs() {
        final Path location = DataBucketsHelper.getInputLocation(getTestPlan())
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
        final Properties jobProperties = getTestPlan().getJobProperties();
        final String keyFileLocation = getTestPlan().getKeyFileLocation();
        final Properties infraParameters = getTestPlan().getInfrastructureConfig().getParameters();
        try (OutputStream os = Files.newOutputStream(location, CREATE, APPEND)) {
            jobProperties.store(os, null);
            infraParameters.store(os, null);
            os.write((TestGridConstants.KEY_FILE_LOCATION + "=" + keyFileLocation).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + location, e);
        }
    }
}
