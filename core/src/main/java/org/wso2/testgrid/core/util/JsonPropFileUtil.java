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
 */

package org.wso2.testgrid.core.util;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;

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


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * This class is used for managing the .properties file and .json files used by testgrid
 */
public class JsonPropFileUtil {

    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Observes the properties file and updates any necessary values to the json file as a general input
     *  NOTE :: OVERRIDES ANY PROPERTY OF THE SAME NAME
     *
     * @param propFilePath path to .properties file
     * @param jsonFilePath path tp .json file
     */
    public void refillFromPropFile(Path propFilePath , Path jsonFilePath) {



        try (InputStream propInputStream = new FileInputStream(propFilePath.toString())) {

            Properties existingProps = new Properties();
            existingProps.load(propInputStream);

            File jsonFile = new File(jsonFilePath.toString());
            if (jsonFile.exists()) {
                try (InputStream jsonInputStream = new FileInputStream(jsonFilePath.toString())) {

                    JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
                    JSONObject inputJson = new JSONObject(jsonTokener);

                    Iterator it = existingProps.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        if (pair.getValue() != null) {
                            if (inputJson.has("general")) {
                                inputJson.getJSONObject("general").put((String) pair.getKey(), pair.getValue());
                            } else {
                                JSONObject generalProps = new JSONObject(existingProps);
                                inputJson.put("general", generalProps);
                            }
                        }
                    }
                    try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                        inputJson.write(jsonWriter);
                        jsonWriter.write("\n");
                    } catch (IOException e) {
                        logger.error(jsonFilePath + " Error while persisting infra input params to " + jsonFilePath);
                    }

                } catch (IOException ex) {
                    logger.info("ERROR please view log files to Debug, " + propFilePath + " found but " + jsonFilePath +
                            " file not found ");
                }
            } else {
                HashMap<String, Object> newJsonFileInputs = new HashMap<String, Object>();
                newJsonFileInputs.put("general", existingProps);

                JSONObject newJsonFile = new JSONObject(newJsonFileInputs);
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    newJsonFile.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (IOException exc) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath);
                }
            }

        } catch (FileNotFoundException e) {
            logger.info(propFilePath + " Not created yet ignoring read property file step");
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + propFilePath);
        }

    }

    /**
     * Persist additional inputs required other than the outputs from previous steps (i.e. infra/deployment).
     * The additional inputs are specified in the testgrid.yaml.
     *
     * NOTE :: Used in DeployPhase
     *
     * NOTE :: JSON File structure
     * { general: {
     *      gen_prop1:val1 , gen_prop2:val2
     *   },
     *   currentscript: {
     *      prop4:val4
     *   },
     *   script1: {prop3:val3},
     *   script2: {prop4:val4}
     *  }
     *
     * @param properties properties to be added
     * @param propFilePath path of the property file
     * @param jsonFilePath path to the JSON file
     * @param scriptName  OPTIONAL Name of script file if not provided property added as a general variable
     * @throws TestPlanExecutorException if writing to the property file fails
     */
    public void persistAdditionalInputs(Map properties, Path propFilePath , Path jsonFilePath,
                                         Optional<String> scriptName) throws TestPlanExecutorException {

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
                    } else {
                        JSONObject generalProps = new JSONObject(properties);
                        inputJson.put("general", generalProps);
                    }
                }
                // Append to json file
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (IOException e) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath, e);
                }

            } catch (IOException e) {
                // If file does not exist create new file and write into it
                logger.info(jsonFilePath + "created");
                HashMap<String, Object> newJsonFileInputs = new HashMap<String, Object>();
                newJsonFileInputs.put("general", properties);
                JSONObject newJsonFile = new JSONObject(newJsonFileInputs);
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    newJsonFile.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (IOException ex) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath, ex);
                }
            }
        } else {
            /*
             If input params are specified from a script save currently executing script under currentscript
             and save the same info under script name for future use.
             */
            try {
                InputStream jsonInputStream = new FileInputStream(jsonFilePath.toString());
                JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
                JSONObject inputJson = new JSONObject(jsonTokener);
                JSONObject generalProps = null;
                // Contains both general and script params
                JSONObject scriptParamsJson = new JSONObject();
                JSONObject scriptParamsOnly = new JSONObject(properties);

                if (inputJson.has("general")) {
                    generalProps = inputJson.getJSONObject("general");
                }

                Iterator it = properties.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    scriptParamsJson.put((String) pair.getKey(), pair.getValue());
                }

                if (generalProps != null) {
                    Iterator<String> genit = generalProps.keys();
                    while (genit.hasNext()) {
                        String key = genit.next();
                        scriptParamsJson.put(key, generalProps.get(key));
                    }
                }

                String scriptNameString = scriptName.get();
                inputJson.put(scriptNameString, scriptParamsOnly);
                inputJson.put("currentscript", scriptParamsJson);

                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (IOException e) {
                    logger.error("Error while persisting infra input params to " + jsonFilePath, e);
                }

            } catch (IOException e) {

                logger.info(jsonFilePath + "created");
                JSONObject scriptParamsJson = new JSONObject(properties);
                JSONObject inputJson = new JSONObject();
                String scriptNameString = scriptName.get();
                inputJson.put(scriptNameString, scriptParamsJson);
                inputJson.put("currentscript", scriptParamsJson);

                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    inputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (IOException ex) {
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
        } catch (IOException e) {
            throw new TestPlanExecutorException("Error occurred while writing deployment outputs.", e);
        }
    }

    /**
     *
     *  Optional method removes a scripts params from prop file after execution
     *  @Param script - Script File
     *  @Param propFilePath - Path Variable to properties File
     */

    public void removeScriptParams(Script script, Path propFilePath) {


        try (InputStream propInputStream = new FileInputStream(propFilePath.toString());) {
            Properties existingProps = new Properties();
            existingProps.load(propInputStream);

            Map<String, Object> inputParams = script.getInputParameters();

            Iterator it = inputParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (existingProps.containsKey(pair.getKey())) {
                    existingProps.remove(pair.getKey());
                    logger.info("removing property " + pair.getKey());
                }
            }

            try (PrintWriter printWriter = new PrintWriter(
                    new OutputStreamWriter(Files.newOutputStream(propFilePath), StandardCharsets.UTF_8))) {
                Iterator itr = existingProps.entrySet().iterator();
                while (itr.hasNext()) {
                    Map.Entry pair = (Map.Entry) itr.next();
                    printWriter.println(pair.getKey() + "=" + pair.getValue());
                }
            } catch (IOException e) {
                logger.error("Error while persisting infra input params to " + propFilePath, e);
            }

        } catch (FileNotFoundException e) {
            logger.info(propFilePath + " Not created yet ignoring read property file step");
        } catch (IOException e) {
            logger.info(propFilePath + " Failed to read file");
        }
    }

    /**
     *
     * adds General Infrastructure parameters into infrastructure.json file
     *
     * NOTE:: Used in Infra Phase
     *
     * @param properties properties to be added
     * @param jsonFilePath Path to Json File
     */

    public void persistInfratoJSON(Map properties, Path jsonFilePath) {

        try {
            InputStream is = new FileInputStream(jsonFilePath.toString());
            JSONTokener tokener = new JSONTokener(is);
            JSONObject inputJson = new JSONObject(tokener);
            Iterator it = properties.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (inputJson.has("general")) {
                    inputJson.getJSONObject("general").put((String) pair.getKey(), pair.getValue());
                } else {
                    JSONObject generalProps = new JSONObject(properties);
                    inputJson.put("general", generalProps);
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                inputJson.write(writer);
                writer.write("\n");
            } catch (IOException e) {
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
            } catch (IOException ex) {
                logger.error("Error while persisting infra input params to " + jsonFilePath, ex);
            }
        }

    }

    /**
     * Adds General Infra Parameters to both Json file and Properties file
     *
     * NOTE:: Used in Infra Phase
     *
     * NOTE :: calls persistInfratoJson() to add properties to JSON file
     *
     * @param propfilepath Path to Properties File
     * @param jsonfilepath Path to Json File
     * @param testplan Testplan of current execution
     */
    public void persistInfraInputsGeneral(Path propfilepath, Path jsonfilepath, TestPlan testplan) {

        // Add any pre existing properties in Properties file to json file to ensure Consistency
        this.refillFromPropFile(propfilepath, jsonfilepath);

        final Properties infraParameters = testplan.getInfrastructureConfig().getParameters();
        final Properties jobProperties = testplan.getJobProperties();
        final String keyFileLocation = testplan.getKeyFileLocation();

        if (keyFileLocation != null) {
            jobProperties.setProperty(TestGridConstants.KEY_FILE_LOCATION, keyFileLocation);
        }
        this.persistInfratoJSON(jobProperties, jsonfilepath);
        this.persistInfratoJSON(infraParameters, jsonfilepath);

        try (PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(propfilepath, CREATE, APPEND), StandardCharsets.UTF_8))) {
            for (Enumeration en = jobProperties.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                printWriter.println(key + "=" + jobProperties.getProperty(key));
            }
            for (Enumeration en = infraParameters.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                printWriter.println(key + "=" + infraParameters.getProperty(key));
            }


        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + propfilepath, e);
        }
    }

    /**
     *  Write infraParams of a script and job properties to .json file .properties file
     *
     *  NOTE:: Used in Infra Phase
     *
     *  NOTE :: calls to persistInfraParamstoJSON() to handle adding to json file
     *
     * @Param script script of which parameters need to be added
     * @Param propLocation path to properties file
     * @Param jsonLocation path to json file
     */
    public void persistInfraInputs(Script script, Path propLocation , Path jsonLocation) {

        Map<String, Object> inputParams = script.getInputParameters();
        persistInfraParamstoJSON(inputParams, jsonLocation, script.getName());

        try (PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(propLocation, CREATE, APPEND), StandardCharsets.UTF_8))) {
            Iterator it = inputParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                printWriter.println(pair.getKey() + "=" + pair.getValue());
            }
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + propLocation, e);
        }
    }

    /**
     *  Writes Infrastructure input Parameters to the JSON files under the
     *  proper Script name
     *
     *  NOTE:: Used in Infra Phase
     *
     * @Param properties properties need to be added
     * @Param jsonFilePath path to json file
     * @Param scriptName script Name
     */
    private void persistInfraParamstoJSON(Map properties, Path jsonFilePath, String scriptName) {
        try {

            InputStream jsonInputStream = new FileInputStream(jsonFilePath.toString());
            JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
            JSONObject inputJson = new JSONObject(jsonTokener);
            JSONObject generalProps = null;
            JSONObject scriptParamsJson = new JSONObject();
            JSONObject scriptParamsOnly = new JSONObject(properties);

            if (inputJson.has("general")) {
                generalProps = inputJson.getJSONObject("general");
            }


            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                scriptParamsJson.put((String) pair.getKey(), pair.getValue());
            }
            if (generalProps != null) {
                Iterator<String> genit = generalProps.keys();
                while (genit.hasNext()) {
                    String key = genit.next();
                    scriptParamsJson.put(key, generalProps.get(key));
                }
            }
            inputJson.put(scriptName, scriptParamsOnly);
            inputJson.put("currentscript", scriptParamsJson);
            try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                inputJson.write(jsonWriter);
                jsonWriter.write("\n");
            } catch (IOException e) {
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
            } catch (IOException ex) {
                logger.error("Error while persisting infra input params to " + jsonFilePath, ex);
            }

        }
    }

    /**
     * Updates the output.json flat data structure
     *
     * @param jsonFileLocation Current phase of execution
     */
    public void updateOutputJson(Path jsonFileLocation, String phase, Path outputJsonPath) {

        try (InputStream jsonInputStream = new FileInputStream(jsonFileLocation.toString())) {

            JSONTokener jsonTokener = new JSONTokener(jsonInputStream);
            JSONObject inputJson = new JSONObject(jsonTokener);

            File outputjsonFile = new File(outputJsonPath.toString());

            if (outputjsonFile.exists()) {
                try (InputStream outputJsonStream = new FileInputStream(outputJsonPath.toString())) {

                    JSONTokener outputTokener = new JSONTokener(outputJsonStream);
                    JSONObject insertOutputJsonVal = new JSONObject(outputTokener);

                    if (inputJson.has("currentscript")) {
                        insertOutputJsonVal.put(phase, inputJson.get("currentscript"));
                    } else if (inputJson.has("general")) {
                        insertOutputJsonVal.put(phase, inputJson.get("general"));
                    }

                    if (insertOutputJsonVal.length() != 0) {
                        try (BufferedWriter jsonWriter = Files
                                .newBufferedWriter(Paths.get(outputJsonPath.toString()))) {
                            insertOutputJsonVal.write(jsonWriter);
                            jsonWriter.write("\n");
                        } catch (IOException ex) {
                            logger.error("Error while persisting params to " + outputJsonPath, ex);
                        }
                    }

                } catch (IOException e) {
                    logger.error(outputJsonPath.toString() + "not found creating");
                }
            } else {
                JSONObject insertOutputJsonVal = new JSONObject();

                if (inputJson.has("currentscript")) {
                    insertOutputJsonVal.put(phase, inputJson.get("currentscript"));
                } else if (inputJson.has("general")) {
                    insertOutputJsonVal.put(phase, inputJson.get("general"));
                }

                if (insertOutputJsonVal.length() != 0) {
                    try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(outputJsonPath.toString()))) {
                        insertOutputJsonVal.write(jsonWriter);
                        jsonWriter.write("\n");
                    } catch (IOException ex) {
                        logger.error("Error while persisting params to " + outputJsonPath, ex);
                    }
                }
            }

        } catch (IOException e) {
            logger.error(jsonFileLocation.toString() + "not found");
        }
    }

}
