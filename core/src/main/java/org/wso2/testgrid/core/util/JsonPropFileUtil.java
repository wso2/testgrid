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
import org.wso2.testgrid.common.config.Script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Map.Entry;
import java.util.Properties;


/**
 * This class is used for managing the properties files and json files used by TestGrid to store
 * script parameters
 *
 * @since 1.0.8
 */
public class JsonPropFileUtil {

    private static Logger logger = LoggerFactory.getLogger(JsonPropFileUtil.class);

    /**
     * Persist additional inputs required which are not related to a specific script
     *
     * @param properties properties to be added
     * @param propFilePath path of the property file
     * @param jsonFilePath path to the JSON file
     */
    public static void persistAdditionalInputs(Map<String, Object> properties, Path propFilePath, Path jsonFilePath) {

        // If value is not specified from a script add the value as a general value to the json file
        File phasejsonFile = new File(jsonFilePath.toString());
        if (phasejsonFile.exists()) {
            try {

                // If the JSON file exists read existing values and append to the file
                InputStream phasejsonInputStream = new FileInputStream(jsonFilePath.toString());
                JSONTokener phasejsonTokener = new JSONTokener(phasejsonInputStream);
                JSONObject phaseinputJson = new JSONObject(phasejsonTokener);

                // Add new value to a flatlist level of the script
                for (Entry<String, Object> property : properties.entrySet()) {
                    if (phaseinputJson.has("general")) {
                        phaseinputJson.getJSONObject("general").put(property.getKey(),
                                property.getValue());
                    } else {
                        JSONObject generalProps = new JSONObject(properties);
                        phaseinputJson.put("general", generalProps);
                    }
                }
                // Append to json file
                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    phaseinputJson.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (IOException e) {
                    logger.error("Error while persisting Input Parameters", e);
                }

            } catch (IOException e) {
                logger.error("Error while persisting Input Parameters", e);
            }
        } else {
            HashMap<String, Object> phaseParamMap = new HashMap<>();
            phaseParamMap.put("general", properties);
            JSONObject phaseJson = new JSONObject(phaseParamMap);
            try (BufferedWriter phaseJsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                phaseJson.write(phaseJsonWriter);
                phaseJsonWriter.write("\n");
            } catch (IOException e) {
                logger.error("Error while persisting Input Parameters", e);
            }
        }
        // append new properties to .properties file
        persistToPropFile(properties, propFilePath);
    }

    /**
     * Persist additional inputs required other than the outputs from previous steps (i.e. infra/deployment).
     * The additional inputs are specified in the testgrid.yaml.
     *
     *
     * @param properties properties to be added
     * @param propFilePath path of the property file
     * @param jsonFilePath path to the JSON file
     * @param scriptName  OPTIONAL Name of script file if not provided property added as a general variable
     */
    public static void persistAdditionalInputs(Map<String, Object> properties, Path propFilePath, Path jsonFilePath,
                                               String scriptName) {

        File phasejsonFile = new File(jsonFilePath.toString());
        if (phasejsonFile.exists()) {
            try {
                InputStream phasejsonInputStream = new FileInputStream(jsonFilePath.toString());
                JSONTokener phasejsonTokener = new JSONTokener(phasejsonInputStream);
                JSONObject phaseJSON = new JSONObject(phasejsonTokener);
                JSONObject generalProps = null;
                // Contains both general and script params
                JSONObject currentscriptJSON = new JSONObject();
                JSONObject scriptParamsOnlyJSON = new JSONObject(properties);

                if (phaseJSON.has("general")) {
                    generalProps = phaseJSON.getJSONObject("general");
                }

                for (Entry<String, Object> property : properties.entrySet()) {
                    currentscriptJSON.put(property.getKey(), property.getValue());
                }

                if (generalProps != null) {
                    Iterator<String> genit = generalProps.keys();
                    while (genit.hasNext()) {
                        String key = genit.next();
                        currentscriptJSON.put(key, generalProps.get(key));
                    }
                }

                phaseJSON.put(scriptName, scriptParamsOnlyJSON);
                phaseJSON.put("currentscript", currentscriptJSON);

                try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                    phaseJSON.write(jsonWriter);
                    jsonWriter.write("\n");
                } catch (IOException e) {
                    logger.error("Error while persisting Input Parameters", e);
                }

            } catch (IOException e) {
                logger.error("Error while persisting Input Parameters", e);
            }
        } else {

            JSONObject scriptParamsJson = new JSONObject(properties);
            JSONObject phaseJson = new JSONObject();
            phaseJson.put(scriptName, scriptParamsJson);
            phaseJson.put("currentscript", scriptParamsJson);

            try (BufferedWriter phasejsonWriter = Files.newBufferedWriter(Paths.get(jsonFilePath.toString()))) {
                phaseJson.write(phasejsonWriter);
                phasejsonWriter.write("\n");
            } catch (IOException e) {
                logger.error("Error while persisting Input Parameters", e);
            }
        }
        // append new properties to .properties file
        persistToPropFile(properties, propFilePath);
    }

    /**
     * Persist parameters to the properties file
     * @param properties
     * @param propFilePath
     */
    private static void persistToPropFile(Map<String, Object> properties, Path propFilePath) {
        try (PrintWriter propPrinterWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(propFilePath.toString(), true), StandardCharsets.UTF_8))) {
            for (Entry<String, Object> property : properties.entrySet()) {
                propPrinterWriter.println(property.getKey() + "=" + property.getValue());
            }
        } catch (IOException e) {
            logger.error("Error while persisting Input Parameters", e);
        }
    }

    /**
     *
     *  Optional method removes a scripts params from prop file after execution
     *  @param script - Script File
     *  @param propFilePath - Path Variable to properties File
     */
    public static void removeScriptParams(Script script, Path propFilePath) {


        try (InputStream propInputStream = new FileInputStream(propFilePath.toString())) {
            Properties existingProps = new Properties();
            existingProps.load(propInputStream);

            Map<String, Object> inputParams = script.getInputParameters();

            for (Map.Entry<String, Object> stringObjectEntry : inputParams.entrySet()) {
                if (existingProps.containsKey(stringObjectEntry.getKey())
                        && existingProps.get(stringObjectEntry.getKey()) != stringObjectEntry.getValue()) {
                    existingProps.remove(stringObjectEntry.getKey());
                }
            }

            try (PrintWriter propPrinterWriter = new PrintWriter(
                    new OutputStreamWriter(Files.newOutputStream(propFilePath), StandardCharsets.UTF_8))) {
                for (Map.Entry<Object, Object> objectObjectEntry : existingProps.entrySet()) {
                    propPrinterWriter.println(objectObjectEntry.getKey() + "=" + objectObjectEntry.getValue());
                }
            } catch (IOException e) {
                logger.error("Error while persisting Input Parameters", e);
            }

        } catch (IOException e) {
            logger.error("Error while persisting Input Parameters", e);
        }
    }

    /**
     * This method reads the appropriate intermediate json file of a phase and updates the final user see-able
     * params.json file to show all input params required by the currently executing script of that phase
     *
     * It stores this data under <phase_name>-in json parameter
     *
     * @param phaseJsonPath location of the intermediate json staging file relevant to the current phase
     * @param phase          Currently executing phase
     * @param paramsJsonPath Location to the user see-able params.json file\
     */
    public static void updateParamsJson(Path phaseJsonPath, String phase, Path paramsJsonPath) {

        try (InputStream phaseJsonInputStream = new FileInputStream(phaseJsonPath.toString())) {

            JSONTokener phaseJsonTokener = new JSONTokener(phaseJsonInputStream);
            JSONObject phaseJson = new JSONObject(phaseJsonTokener);

            File paramsJsonFile = new File(paramsJsonPath.toString());

            if (paramsJsonFile.exists()) {
                try (InputStream paramsJsonStream = new FileInputStream(paramsJsonPath.toString())) {

                    JSONTokener paramsTokener = new JSONTokener(paramsJsonStream);
                    JSONObject paramsJson = new JSONObject(paramsTokener);

                    if (phaseJson.has("currentscript")) {
                        paramsJson.put(phase + "-in", phaseJson.get("currentscript"));
                    } else if (phaseJson.has("general")) {
                        paramsJson.put(phase + "-in", phaseJson.get("general"));
                    }

                    if (paramsJson.length() != 0) {
                        try (BufferedWriter paramsJsonWriter = Files
                                .newBufferedWriter(Paths.get(paramsJsonPath.toString()))) {
                            paramsJson.write(paramsJsonWriter);
                            paramsJsonWriter.write("\n");
                        } catch (IOException e) {
                            logger.error("Error while persisting Input Parameters", e);
                        }
                    }

                } catch (IOException e) {
                    logger.error("Error while persisting Input Parameters", e);
                }
            } else {
                JSONObject paramsJson = new JSONObject();

                if (phaseJson.has("currentscript")) {
                    paramsJson.put(phase + "-in", phaseJson.get("currentscript"));
                } else if (phaseJson.has("general")) {
                    paramsJson.put(phase + "-in", phaseJson.get("general"));
                }

                if (paramsJson.length() != 0) {
                    try (BufferedWriter paramsJsonWriter =
                                 Files.newBufferedWriter(Paths.get(paramsJsonPath.toString()))) {
                        paramsJson.write(paramsJsonWriter);
                        paramsJsonWriter.write("\n");
                    } catch (IOException e) {
                        logger.error("Error while persisting Input Parameters", e);
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Error while persisting Input Parameters", e);
        }
    }

}
