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
;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.DeployerInitializationException;
import org.wso2.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.UnsupportedDeployerException;
import org.wso2.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * This class includes implementation of deployment-creation phase.
 */
public class DeployPhase extends Phase {
    @Override
    boolean verifyPrecondition() {
        if (getTestPlan().getPhase().equals(TestPlanPhase.INFRA_PHASE_SUCCEEDED) &&
                getTestPlan().getStatus().equals(TestPlanStatus.RUNNING)) {
            persistTestPlanPhase(TestPlanPhase.DEPLOY_PHASE_STARTED);
            return true;
        } else {
            logger.error("INFRA phase was not succeeded for test-plan: " + getTestPlan().getId() + "Hence" +
                    "not starting other phases.");
            persistTestPlanProgress(TestPlanPhase.INFRA_PHASE_ERROR, TestPlanStatus.ERROR);
            return false;
        }
    }

    @Override
    void executePhase() {
        try {
            DeploymentCreationResult deploymentCreationResult = createDeployment();
            getTestPlan().setDeploymentCreationResult(deploymentCreationResult);
            if (!deploymentCreationResult.isSuccess()) {
                persistTestPlanProgress(TestPlanPhase.DEPLOY_PHASE_ERROR, TestPlanStatus.ERROR);
                for (ScenarioConfig scenarioConfig : getTestPlan().getScenarioConfigs()) {
                    scenarioConfig.setStatus(Status.DID_NOT_RUN);
                }
                persistTestPlan();

                logger.error(StringUtil.concatStrings(
                        "Error occurred while performing deployment for test plan ", getTestPlan().getId(),
                        " Releasing infrastructure..."));
                releaseInfrastructure();
            } else {
                persistTestPlanPhase(TestPlanPhase.DEPLOY_PHASE_SUCCEEDED);

                //Append TestPlan id to deployment.properties file
                Map<String, Object> tgProperties = new HashMap<>();
                tgProperties.put("TEST_PLAN_ID", getTestPlan().getId());

                // TODO insert properties to DEPL_OUT_FILE and JSON file as they are been executed
                persistAdditionalInputs(tgProperties, DataBucketsHelper.getOutputLocation(getTestPlan())
                        .resolve(DataBucketsHelper.DEPL_OUT_FILE), DataBucketsHelper.getOutputLocation(getTestPlan())
                        .resolve(DataBucketsHelper.DEPL_OUT_JSONFILE), Optional.empty());
                // Append inputs from scenarioConfig in testgrid yaml to deployment outputs file
                Map<String, Object> sceProperties = new HashMap<>();
                for (ScenarioConfig scenarioConfig : getTestPlan().getScenarioConfigs()) {
                    sceProperties.putAll(scenarioConfig.getInputParameters());
                    persistAdditionalInputs(sceProperties, DataBucketsHelper.getOutputLocation(getTestPlan())
                            .resolve(DataBucketsHelper.DEPL_OUT_FILE),
                            DataBucketsHelper.getOutputLocation(getTestPlan())
                            .resolve(DataBucketsHelper.DEPL_OUT_JSONFILE), Optional.of(scenarioConfig.getName()));
                }
            }
        } catch (TestPlanExecutorException e) {
            logger.error("Error occurred while executing Deploy Phase for the dep.pattern " +
                    getTestPlan().getDeploymentPattern());
        }
    }

    /**
     * Creates the deployment in the provisioned infrastructure.
     *
     * @return created {@link DeploymentCreationResult}
     * @throws TestPlanExecutorException thrown when error on creating deployment
     */
    private DeploymentCreationResult createDeployment()
            throws TestPlanExecutorException {
        InfrastructureProvisionResult infrastructureProvisionResult = getTestPlan().getInfrastructureProvisionResult();
        Path infraOutFilePath = DataBucketsHelper.getOutputLocation(getTestPlan())
                .resolve(DataBucketsHelper.INFRA_OUT_FILE);
        Path infraOutJSONFilePath = DataBucketsHelper.getOutputLocation(getTestPlan())
                .resolve(DataBucketsHelper.INFRA_OUT_JSONFILE);
        try {
            DeploymentCreationResult result = new DeploymentCreationResult();
            for (Script script: getTestPlan().getDeploymentConfig().getFirstDeploymentPattern().getScripts()) {
                printMessage("\t\t Creating deployment: " + script.getName());

                if (!infrastructureProvisionResult.isSuccess()) {
                    DeploymentCreationResult nresult = new DeploymentCreationResult();
                    result.setSuccess(false);
                    logger.debug("Deployment result: " + nresult);
                    return result;
                }

                // Append deploymentConfig inputs in testgrid yaml to infra outputs file
                Map<String, Object> deplInputs = script.getInputParameters();
                persistAdditionalInputs(deplInputs, infraOutFilePath, infraOutJSONFilePath,
                        Optional.of(script.getName()));
                Deployer deployerService = DeployerFactory.getDeployerService(script);
                DeploymentCreationResult aresult =
                        deployerService.deploy(getTestPlan(),
                                infrastructureProvisionResult, script);
                addTo(result, aresult);
                logger.debug("Deployment result: " + result);
            }
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

        DeploymentCreationResult result = new DeploymentCreationResult();
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

    /**
     * Observes the properties file and updates any necessary values to the json file as a general input
     *  NOTE :: OVERRIDES ANY PROPERTY OF THE SAME NAME
     *
     * @param propFilePath path to .properties file
     * @param jsonFilePath path tp .json file
     */
    private void refillFromPropFile(Path propFilePath , Path jsonFilePath) {
        InputStream propInputStream = null;
        InputStream jsonInputStream = null;
        try {

            propInputStream = new FileInputStream(propFilePath.toString());
            Properties existingprops = new Properties();
            existingprops.load(propInputStream);
            propInputStream.close();
            try {
                jsonInputStream = new FileInputStream(jsonFilePath.toString());
                JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
                JSONObject inputJson = new JSONObject(jsonTokener);
                jsonInputStream.close();
                Iterator it = existingprops.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (pair.getValue() != null) {
                        if (inputJson.has("general")) {
                            inputJson.getJSONObject("general").put((String) pair.getKey(), pair.getValue());
                        }
                    }
                }


                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (Throwable e) {
                    logger.error(jsonFilePath + " Error while persisting infra input params to " + jsonFilePath);
                }
            } catch (IOException ex) {
                logger.info("ERROR please view log files to Debug, " + propFilePath + " found but " + jsonFilePath +
                        " file not found ");
                HashMap<String, Object> insertvalue = new HashMap<String, Object>();
                insertvalue.put("general", existingprops);
                JSONObject inputJson = new JSONObject(insertvalue);
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (Throwable exc) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath);
                }
            } finally {
                try {
                    if (jsonInputStream != null) {
                        jsonInputStream.close();
                    }
                } catch (Exception e) {
                    logger.error("Failed to close Stream");
                }
            }
        } catch (FileNotFoundException e) {
            logger.info(propFilePath + " Not created yet ignoring read property file step");
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + propFilePath);
        } finally {
            try {
                if (propInputStream != null) {
                    propInputStream.close();
                }
            } catch (Exception e) {
                logger.error("Failed to close Stream");
            }
        }

    }

    /**
     * Persist additional inputs required other than the outputs from previous steps (i.e. infra/deployment).
     * The additional inputs are specified in the testgrid.yaml.
     *
     * NOTE :: JSON File structure
     * { gen_prop1:val1 , gen_prop2:val2, currentscript: {prop4:val4}, script1: {prop3:val3} , script2: {prop4:val4} }
     *
     * @param properties properties to be added
     * @param propFilePath path of the property file
     * @param jsonFilePath path to the JSON file
     * @param scriptName Name of script file
     * @throws TestPlanExecutorException if writing to the property file fails
     */
    private void persistAdditionalInputs(Map properties, Path propFilePath , Path jsonFilePath,
                                         Optional<String> scriptName) throws TestPlanExecutorException {

        refillFromPropFile(propFilePath, jsonFilePath);

        if (!scriptName.isPresent()) {
            // If value is not specified from a script add the value as a general value to the json file
            try {

                // If the JSON file exists read existing values and append to the file
                InputStream jsonInputStream = new FileInputStream(jsonFilePath.toString());
                JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
                JSONObject inputJson = new JSONObject(jsonTokener);
                Iterator it = properties.entrySet().iterator();

                // Add new value to a flatlist level of the script
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (inputJson.has("general")) {
                        inputJson.getJSONObject("general").put((String) pair.getKey(), pair.getValue());
                    }
                }
                // Append to json file
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (Throwable e) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath, e);
                }

            } catch (IOException e) {
                // If file does not exist create new file and write into it
                logger.info(jsonFilePath + "created");
                HashMap<String, Object> insertvalue = new HashMap<String, Object>();
                insertvalue.put("general", properties);
                JSONObject inputJson = new JSONObject(insertvalue);
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (Throwable ex) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath, ex);
                }
            }
        } else {
            /*
             If input params are specified from a script save currently executing script under currentscript
             and save the same info under script name for later retrival.
             */
            try {
                InputStream jsonInputStream = new FileInputStream(jsonFilePath.toString());
                JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
                JSONObject inputJson = new JSONObject(jsonTokener);

                JSONObject scriptParamsJson = null;
                if (inputJson.has("general")) {
                    scriptParamsJson = inputJson.getJSONObject("general");
                }
                JSONObject scriptParamsOnly = new JSONObject(properties);

                Iterator it = properties.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (scriptParamsJson != null) {
                        scriptParamsJson.put((String) pair.getKey(), pair.getValue());
                    }
                }
                String scriptNameString = scriptName.get();
                inputJson.put(scriptNameString, scriptParamsOnly);
                inputJson.put("currentscript", scriptParamsJson);
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (Throwable e) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath, e);
                }
            } catch (IOException e) {
                logger.info(jsonFilePath + "created");
                JSONObject scriptParamsJson = new JSONObject(properties);
                JSONObject inputJson = new JSONObject();
                String scriptNameString = scriptName.get();
                inputJson.put(scriptNameString, scriptParamsJson);
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (Throwable ex) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath, ex);
                }
            }
        }
        // append new properties to .properties file
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(propFilePath.toString(), true), StandardCharsets.UTF_8))) {
            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                printWriter.println(pair.getKey() + "=" + pair.getValue());
            }
        } catch (Throwable e) {
            throw new TestPlanExecutorException("Error occurred while writing deployment outputs.", e);
        }
    }
}
