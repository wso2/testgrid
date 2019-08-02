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

import org.json.JSONObject;
import org.json.JSONTokener;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

            InfrastructureProvisionResult provisionResult = new InfrastructureProvisionResult();

            persistInfraInputsGeneral();

            for (Script script : infrastructureConfig.getFirstProvisioner().getScripts()) {
                if (!Script.Phase.DESTROY.equals(script.getPhase())) {
                    persistInfraInputs(script);
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
     *  Writes Any non script related Infrastructure parameters to the JSON file
     *  exists as an exterior label
     *
     */
    private void persistInfratoJSON(Map properties, Path jsonFilePath) {

        try {
            InputStream is = new FileInputStream(jsonFilePath.toString());
            JSONTokener tokener = new JSONTokener(is);
            JSONObject inputJson = new JSONObject(tokener);
            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (inputJson.has("general")) {
                    inputJson.getJSONObject("general").put((String) pair.getKey(), pair.getValue());
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                inputJson.write(writer);
                writer.write("\n");
            } catch (Throwable e) {
                logger.error("Error while persisting infra input params to " + jsonFilePath, e);
            }
        } catch (IOException e) {
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

    }

    /**
     *  Writes Infrastructure input Parameters to the JSON files under the
     *  proper Script name
     *
     */
    private void persistInfraParamstoJSON(Map properties, Path jsonFilePath, String scriptName) {
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
            inputJson.put(scriptName, scriptParamsOnly);
            inputJson.put("currentscript", scriptParamsJson);
            try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                inputJson.write(jsonWriter);
                jsonWriter.write("\n");
            } catch (Throwable e) {
                logger.error("Error while persisting infra input params to " + jsonFilePath, e);
            }
        } catch (IOException e) {
            logger.info(jsonFilePath + "created");
            JSONObject json = new JSONObject(properties);
            JSONObject scriptparams = new JSONObject();
            scriptparams.put(scriptName, json);
            scriptparams.put("currentscript", json);
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                scriptparams.write(writer);
                writer.write("\n");
            } catch (Throwable ex) {
                logger.error("Error while persisting infra input params to " + jsonFilePath, ex);
            }
        }
    }

    /**
     * Observes the properties file and updates any necessary values to the json file as a general input
     *  NOTE :: OVERRIDES ANY PROPERTY OF THE SAME NAME

     */
    private void refillFromPropFile() {

        final Path propFilePath = DataBucketsHelper.getInputLocation(getTestPlan())
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
        final Path jsonFilePath = DataBucketsHelper.getInputLocation(getTestPlan())
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_JSONFILE);

        InputStream propInputStream = null;
        InputStream jsonInputStream = null;

        try {
            propInputStream = new FileInputStream(propFilePath.toString());
            Properties existingprops = new Properties();
            existingprops.load(propInputStream);
            try {
                jsonInputStream = new FileInputStream(jsonFilePath.toString());
                JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
                JSONObject inputJson = new JSONObject(jsonTokener);

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
                    logger.error("Error while persisting infra input params to " + jsonFilePath);
                }
            } catch (IOException ex) {
                logger.info("ERROR please view log files to Debug, " + propFilePath + " found but " + jsonFilePath +
                        " file not found ");
                HashMap<String, Object> insertvalue = new HashMap<String, Object>();
                insertvalue.put("general", existingprops);
                JSONObject inputjson = new JSONObject(insertvalue);
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputjson.write(jsonWriter);
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
            logger.info(propFilePath + " Not created yet ignoring read property file step");
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
     * The infra-provision.sh / deploy.sh / run-scenario.sh receive the test plan
     * configuration as a properties file aswell as a json file
     *
     * @params script script to be added to databucket files
     *
     */
    private void persistInfraInputs(Script script) {

        refillFromPropFile();

        final Path location = DataBucketsHelper.getInputLocation(getTestPlan())
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
        final Path jsonlocation = DataBucketsHelper.getInputLocation(getTestPlan())
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_JSONFILE);

        Map<String, Object> inputParams = script.getInputParameters();
        persistInfraParamstoJSON(inputParams, jsonlocation, script.getName());

        try (PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(location, CREATE, APPEND), StandardCharsets.UTF_8))) {
            Iterator it = inputParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                printWriter.println(pair.getKey() + "=" + pair.getValue());
            }
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + location, e);
        }
    }

    /**
     *  Write infraParams and job properties to .json file .properties file
     *
     */
    private void persistInfraInputsGeneral() {

        refillFromPropFile();

        final Path location = DataBucketsHelper.getInputLocation(getTestPlan())
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
        final Path jsonlocation = DataBucketsHelper.getInputLocation(getTestPlan())
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_JSONFILE);
        final Properties infraParameters = getTestPlan().getInfrastructureConfig().getParameters();
        final Properties jobProperties = getTestPlan().getJobProperties();
        final String keyFileLocation = getTestPlan().getKeyFileLocation();

        if (keyFileLocation != null) {
            jobProperties.setProperty(TestGridConstants.KEY_FILE_LOCATION, keyFileLocation);
        }
        persistInfratoJSON(jobProperties, jsonlocation);
        persistInfratoJSON(infraParameters, jsonlocation);

        try (PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(location, CREATE, APPEND), StandardCharsets.UTF_8))) {
            for (Enumeration en = jobProperties.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                printWriter.println(key + "=" + jobProperties.getProperty(key));
            }
            for (Enumeration en = infraParameters.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                printWriter.println(key + "=" + infraParameters.getProperty(key));
            }
            printWriter.println((TestGridConstants.KEY_FILE_LOCATION + "=" + keyFileLocation));

        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + location, e);
        }
    }
}
