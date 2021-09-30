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

import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.DeployerInitializationException;
import org.wso2.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.UnsupportedDeployerException;
import org.wso2.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.testgrid.common.plugins.AWSArtifactReader;
import org.wso2.testgrid.common.plugins.ArtifactReadable;
import org.wso2.testgrid.common.plugins.ArtifactReaderException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.S3StorageUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.core.util.JsonPropFileUtil;
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class includes implementation of deployment-creation phase.
 * TODO : @since 1.0.8
 */
public class DeployPhase extends Phase {

    @Override
    // TODO : Only return wether previous stage is completed. DO NOT set the new stage
    boolean verifyPrecondition() {
        if (getTestPlan().getPhase().equals(TestPlanPhase.INFRA_PHASE_SUCCEEDED) &&
                getTestPlan().getStatus().equals(TestPlanStatus.RUNNING)) {
            persistTestPlanPhase(TestPlanPhase.DEPLOY_PHASE_STARTED);
            logger.info("Deploy Phase: verify pre condition...");
            return true;
        } else {
            logger.info("INFRA phase was not succeeded for test-plan: " + getTestPlan().getId() + "Hence" +
                    "not starting other phases.");
            persistTestPlanProgress(TestPlanPhase.INFRA_PHASE_ERROR, TestPlanStatus.ERROR);
            return false;
        }
    }

    @Override
    void executePhase() {
        try {
            logger.info("Deploy Phase: Create deployment...");
            DeploymentCreationResult deploymentCreationResult = createDeployment();
            getTestPlan().setDeploymentCreationResult(deploymentCreationResult);
            if (!deploymentCreationResult.isSuccess()) {
                persistTestPlanProgress(TestPlanPhase.DEPLOY_PHASE_ERROR, TestPlanStatus.ERROR);
                for (ScenarioConfig scenarioConfig : getTestPlan().getScenarioConfigs()) {
                    scenarioConfig.setStatus(Status.DID_NOT_RUN);
                }
                logger.info("Deploy Phase: persist test plan...");
                persistTestPlan();

                logger.error(StringUtil.concatStrings(
                        "Error occurred while performing deployment for test plan ", getTestPlan().getId(),
                        " Releasing infrastructure..."));
                releaseInfrastructure();
            } else {
                persistTestPlanPhase(TestPlanPhase.DEPLOY_PHASE_SUCCEEDED);
            }
        } catch (TestPlanExecutorException e) {
            logger.error("Error occurred while executing Deploy Phase for the dep.pattern " +
                    getTestPlan().getDeploymentPattern(), e);
        }
    }

    /**
     * Creates the deployment in the provisioned infrastructure.
     *
     * @return created {@link DeploymentCreationResult}
     */
    private DeploymentCreationResult createDeployment() {

        InfrastructureProvisionResult infrastructureProvisionResult = getTestPlan().getInfrastructureProvisionResult();

        DeploymentCreationResult result = new DeploymentCreationResult();

        logger.info("Deploy Phase: Check Infrastructure Provision Result...");
        if (!infrastructureProvisionResult.isSuccess()) {
            DeploymentCreationResult nresult = new DeploymentCreationResult();
            result.setSuccess(false);
            logger.info("Deployment result: " + nresult);
            return result;
        }

        Path infraOutFilePath = DataBucketsHelper.getOutputLocation(getTestPlan())
                .resolve(DataBucketsHelper.INFRA_OUT_FILE);
        Path infraOutJSONFilePath = DataBucketsHelper.getOutputLocation(getTestPlan())
                .resolve(DataBucketsHelper.INFRA_OUT_JSONFILE);
        Path outputjsonFilePath = DataBucketsHelper.getInputLocation(getTestPlan())
                .resolve(DataBucketsHelper.PARAMS_JSONFILE);

        Map<String, Object> additionalDepProps = new HashMap<>();

        HashMap<ConfigurationContext.ConfigurationProperties, String> generalPropertyList = new HashMap<>();

        generalPropertyList.put(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME, "s3Region");
        generalPropertyList.put(ConfigurationContext.ConfigurationProperties.AWS_ACCESS_KEY_SECRET_TG_BOT,
                "s3secretKey");
        generalPropertyList.put(ConfigurationContext.ConfigurationProperties.AWS_ACCESS_KEY_ID_TG_BOT, "s3accessKey");
        generalPropertyList.put(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME, "s3Bucket");
        generalPropertyList.put(ConfigurationContext.ConfigurationProperties.ES_ENDPOINT_URL, "elasticsearchEndPoint");
        generalPropertyList.put(ConfigurationContext.ConfigurationProperties.TESTGRID_ENVIRONMENT, "environment");
        generalPropertyList.put(ConfigurationContext.ConfigurationProperties.TESTGRID_PASS, "password");
        // Params to for Log File extraction through Mutating WebHook
        additionalDepProps.put("depRepoLocation", getTestPlan().getDeploymentRepository());
        logger.info("Deploy Phase: Property list generation completed...");

        for (Entry<ConfigurationContext.ConfigurationProperties,
                String> configPropStringpair : generalPropertyList.entrySet()) {
            String propertyValue = ConfigurationContext.getProperty(configPropStringpair.getKey());
            if (propertyValue != null) {
                additionalDepProps.put(configPropStringpair.getValue(), propertyValue);
            }
        }

        if (ConfigurationContext.getProperty
                (ConfigurationContext.ConfigurationProperties.AWS_S3_ARTIFACTS_DIR) != null) {
            try {
                ArtifactReadable artifactReadable = new AWSArtifactReader(ConfigurationContext.
                        getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME), ConfigurationContext.
                        getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME));
                String s3logPath = S3StorageUtil.deriveS3DeploymentOutputsDir(getTestPlan(), artifactReadable);
                additionalDepProps.put("s3logPath", s3logPath);
            } catch (ArtifactReaderException | IOException e) {
                logger.error("Error occurred while deriving deployment outputs directory for test-plan " +
                        getTestPlan(), e);
            }
        }
        // Params to edit /etc/hosts file in local TestGrid
        logger.info("Deploy Phase: Persist Additional Inputs...");
        JsonPropFileUtil.persistAdditionalInputs(additionalDepProps, infraOutFilePath, infraOutJSONFilePath);
        JsonPropFileUtil.updateParamsJson(infraOutJSONFilePath, "dep", outputjsonFilePath);

        try {

            logger.info("Deploy Phase: Creation deployments...");
            for (Script script: getTestPlan().getDeploymentConfig().getFirstDeploymentPattern().getScripts()) {
                printMessage("\t\t Creating deployment: " + script.getName());

                // Append deploymentConfig inputs in testgrid yaml to infra outputs file
                Map<String, Object> deplInputs = script.getInputParameters();
                JsonPropFileUtil.persistAdditionalInputs(deplInputs, infraOutFilePath, infraOutJSONFilePath,
                        script.getName());

                JsonPropFileUtil.updateParamsJson(infraOutJSONFilePath, "dep", outputjsonFilePath);

                Deployer deployerService = DeployerFactory.getDeployerService(script);
                DeploymentCreationResult aresult =
                        deployerService.deploy(getTestPlan(),
                                infrastructureProvisionResult, script);
                //TODO mark issue better renaming
                addTo(result, aresult);
                logger.debug("Deployment result: " + result);
                if (!aresult.isSuccess()) {
                    logger.warn("Deploy script '" + script.getName() + "' failed. Not running remaining scripts.");
                    break;
                }
                
            }
            logger.info("Deploy Phase: Creation deployments completed...");
            return result;
        } catch (TestGridDeployerException e) {
            persistTestPlanProgress(TestPlanPhase.DEPLOY_PHASE_ERROR, TestPlanStatus.ERROR);
            String msg = StringUtil
                    .concatStrings("Exception occurred while running the deployment for deployment pattern '",
                            getTestPlan().getDeploymentPattern(), "', in TestPlan");
            logger.error(msg, e);
        } catch (DeployerInitializationException e) {
            persistTestPlanProgress(TestPlanPhase.DEPLOY_PHASE_ERROR, TestPlanStatus.ERROR);
            String msg = StringUtil
                    .concatStrings("Unable to locate a Deployer Service implementation for deployment " +
                                    "pattern '", getTestPlan().getDeploymentPattern(), "', in TestPlan '");
            logger.error(msg, e);
        } catch (UnsupportedDeployerException e) {
            persistTestPlanProgress(TestPlanPhase.DEPLOY_PHASE_ERROR, TestPlanStatus.ERROR);
            String msg = StringUtil
                    .concatStrings("Error occurred while running deployment for deployment pattern '",
                            getTestPlan().getDeploymentPattern(), "' in TestPlan");
            logger.error(msg, e);
        } catch (Exception e) {
            // deployment creation should not interrupt other tasks.
            persistTestPlanProgress(TestPlanPhase.DEPLOY_PHASE_ERROR, TestPlanStatus.ERROR);
            String msg = StringUtil
                    .concatStrings("Unhandled error occurred hile running deployment for deployment "
                    + "pattern '", getTestPlan().getDeploymentConfig(), "' in TestPlan");
            logger.error(msg, e);
        } finally {
            // Move infrastructure.properties out of data bucket to avoid exposing to the test execution phase.
            logger.info("Moving infrastructure.properties from data bucket");
            File infraOutputFile = infraOutFilePath.toFile();
            if (!infraOutputFile.renameTo(new File(Paths.get(getTestPlan().getWorkspace(),
                    DataBucketsHelper.INFRA_OUT_FILE).toString()))) {
                logger.error("Error while moving " + infraOutFilePath);
            }
        }

        result = new DeploymentCreationResult();
        result.setSuccess(false);
        logger.debug("Deployment result: " + result);
        return result;
    }

    /**
     * The results created by running a single script in deploy phase is added
     * to the deployment creation result
     *
     * @param provisionResult result of the deploy phase
     * @param aProvisionResult result of a single script in deploy phase.
     */

    private void addTo(DeploymentCreationResult provisionResult, DeploymentCreationResult aProvisionResult) {
        provisionResult.getProperties().putAll(aProvisionResult.getProperties());
        if (!aProvisionResult.isSuccess()) {
            provisionResult.setSuccess(false);
        }
    }
    /**
     * Destroys the given InfrastructureConfig
     *
     * @throws TestPlanExecutorException thrown when error on destroying
     *                                   infrastructure
     */
    private void releaseInfrastructure()
            throws TestPlanExecutorException {
        InfrastructureProvisionResult infrastructureProvisionResult = getTestPlan().getInfrastructureProvisionResult();
        DeploymentCreationResult deploymentCreationResult = getTestPlan().getDeploymentCreationResult();
        logger.info("Deployment Phase: Release Infrastructure...");
        try {
            printMessage("\t\t Releasing infrastructure: " + getTestPlan()
                    .getInfrastructureConfig().getFirstProvisioner().getName());

            if (TestGridUtil.isDebugMode(getTestPlan())) {
                printMessage(TestGridConstants.DEBUG_MODE + " is enabled. NOT RELEASING the infrastructure. The"
                        + "infrastructure need to be manually released/de-allocated.");
                return;
            }
            if (!infrastructureProvisionResult.isSuccess() || !deploymentCreationResult.isSuccess()) {
                logger.error("Execution of previous steps failed. Trying to release the possibly provisioned "
                        + "infrastructure");
            }

            for (Script script : getTestPlan().getInfrastructureConfig().getFirstProvisioner().getScripts()) {
                if (!Script.Phase.CREATE.equals(script.getPhase())) {
                    InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                            .getInfrastructureProvider(script);
                    infrastructureProvider.release(getTestPlan().getInfrastructureConfig(),
                            getTestPlan().getInfrastructureRepository(),
                            getTestPlan(), script);
                    // Destroy additional infra created for test execution
                    infrastructureProvider.cleanup(getTestPlan());
                }
            }
            logger.info("Deployment Phase: Release Infrastructure completed...");
        } catch (TestGridInfrastructureException e) {
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error on infrastructure removal for deployment pattern '",
                            getTestPlan().getDeploymentPattern(), "', in TestPlan"), e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            getTestPlan().getDeploymentPattern(), "', in TestPlan"), e);
        }
    }
}
