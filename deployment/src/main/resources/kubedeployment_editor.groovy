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

@Grapes(

        @Grab(group = 'org.yaml', module = 'snakeyaml', version = '1.24')
)
@Grapes(
        @Grab(group = 'org.json', module = 'json', version = '20180813')
)

import org.yaml.snakeyaml.Yaml
import org.json.JSONObject
import org.json.JSONArray

/**
 * This function formats the Elastic Search Endpoint to support logstash by filtering out any https endpoints
 *
 * @param esEndpoint
 * @return
 */
def static formatESURL(String esEndpoint) {
    if (esEndpoint.startsWith("https://")) {
        return (esEndpoint.split("https://")[1]).split(":")[0]
    } else if (esEndpoint.startsWith("http://")) {
        return (esEndpoint.split("http://")[1]).split(":")[0]
    }
    return "error"
}

/**
 * Formats the user provided inputs to a an absolute File Path that can be used by the sidecar
 *
 * @param logLocation String containing the log locations
 * @return
 */
def static formatFilePaths(String logLocation) {

    String absoluteFilePath
    if (logLocation.startsWith('/') && logLocation.endsWith('/')) {
        absoluteFilePath = logLocation
    } else if (logLocation.startsWith('/') && !logLocation.endsWith('/')) {
        absoluteFilePath = logLocation + "/"
    } else if (!logLocation.startsWith('/') && logLocation.endsWith('/')) {
        absoluteFilePath = "/" + logLocation
    } else {
        absoluteFilePath = "/" + logLocation + "/"
    }
    return absoluteFilePath
}

/**
 * TODO : Implement providing of correct logstash.conf based on carbon version , product and pattern
 * Returns the appropriate conf file which is to become the logstash.conf of the Logstash-collector-deployment
 *
 * @param logOptions - Log Options of the job
 * @return
 */

def static deriveConfFile(JSONObject logOptions) {
    return "default.conf"
}


/**
 * Creates yaml file which store all details required for sidecar injector to inject appropriate information to
 * the deployment
 *
 * @param logPathDetailsYamlLoc - Path to yaml file which is to store Log Path details

 * @param depPropPath- File path to the .properties file containing input Parameters
 * @param depType - Type of the deployment (Helm or K8S)
 *
 */
def static confLogCapabilities(String logPathDetailsYamlLoc, String depPropPath, String depType) {
    String logRequirementDetails = "false"
    try {
        InputStream depPropsFileReader = new FileInputStream(depPropPath)
        Properties depProps = new Properties()
        depProps.load(depPropsFileReader)

        String logOptionsString = depProps.getProperty("logOptions")
        logOptionsString = logOptionsString.replaceAll("=", ":")
        logOptionsString = logOptionsString.replaceAll("/", "&")
        JSONObject logOptions = new JSONObject(logOptionsString)
        String elasticsearchEndPoint = depProps.getProperty("elasticsearchEndPoint")
        String depRepo = depProps.getProperty("depRepoLocation")

        String logRequirement = logOptions.getString("logRequirement")

        if (logRequirement.equalsIgnoreCase("sidecar_required")) {
            // If sidecar is required create logPathDetails.yaml file with all information
            JSONArray logLocations = logOptions.getJSONArray("logFileLocations")
            Yaml logpathDetailsYaml = new Yaml()

            FileWriter logPathFileWriter = new FileWriter(logPathDetailsYamlLoc)

            if (logLocations.length() != 0) {
                List logPathConf = []
                for (int i = 0; i < logLocations.length(); i++) {
                    JSONObject logLocation = logLocations.getJSONObject(i)
                    String absoluteFilePath = formatFilePaths(logLocation.getString("path")
                            .replaceAll("&", "/"))
                    Map entry = ["deploymentName": logLocation.getString("deploymentName"), "containerName":
                            logLocation.getString("containerName"), "path": absoluteFilePath
                    ]
                    logPathConf.add(entry)
                }
                Map logConfiguration = ["onlyVars": false, "logPaths": logPathConf]
                logpathDetailsYaml.dump(logConfiguration, logPathFileWriter)
                logRequirementDetails = "SidecarReq default.conf"
            } else {
                logRequirementDetails = "False"
            }
            logPathFileWriter.close()
        } else if (logRequirement.equalsIgnoreCase("log_endpoints_required")) {
            if (depType == "helm") {
                // If only ES endpoint is required access values.yaml file and edit the appropriate value
                String valYamlFormat = logOptions.getString("valuesYamlLocation")
                        .replaceAll("&", "/")
                String rootProjLocation = depProps.getProperty("rootProjLocations")
                String valuesYamlLoc = depRepo.concat("/").concat(rootProjLocation)
                        .concat("/").concat(valYamlFormat)

                JSONArray replaceableValues = logOptions.getJSONArray("replaceableVals")
                InputStream valuesYamlInputStream = new FileInputStream(valuesYamlLoc)
                Yaml logpathDetailsYaml = new Yaml()
                Map valuesYaml = logpathDetailsYaml.load(valuesYamlInputStream)

                String elasticsearchURL = formatESURL(elasticsearchEndPoint)
                String elasticsearchPort = elasticsearchEndPoint.split(":")[2]
                for (int j = 0; j < replaceableValues.length(); j++) {
                    JSONObject replaceableObj = replaceableValues.getJSONObject(j)
                    Map editedValuesMap = valuesYaml
                    String replaceableObjLoc = replaceableObj.getString("location")
                    List < String > pathToRepObj = replaceableObjLoc.split("&")
                    String key
                    for (int i = 0; i < pathToRepObj.size() - 1; i++) {
                        key = pathToRepObj.get(i)
                        editedValuesMap = (Map) editedValuesMap[key]
                    }
                    // add more if blocks for other variables
                    if (replaceableObj.getString("type").equalsIgnoreCase("elasticsearchEndPoint") &&
                            elasticsearchURL != "error") {
                        editedValuesMap[pathToRepObj.get(pathToRepObj.size() - 1)] = elasticsearchURL
                    } else if (replaceableObj.getString("type").equalsIgnoreCase("elasticsearchPort") &&
                            elasticsearchURL != "error") {
                        editedValuesMap[pathToRepObj.get(pathToRepObj.size() - 1)] = elasticsearchPort
                    }
                }
                valuesYamlInputStream.close()
                FileWriter valuesYamlOutputStream = new FileWriter(valuesYamlLoc)
                logpathDetailsYaml.dump(valuesYaml, valuesYamlOutputStream)
                valuesYamlOutputStream.close()
                logRequirementDetails = "False"
            } else if (depType == "k8s") {
                // If k8s inject a env Var to the deployments which stores the elastic search endpoint
                FileWriter logPathFileWriter = new FileWriter(logPathDetailsYamlLoc)
                Yaml yaml = new Yaml()
                List envVars = []
                String formattedElasticsearchURL = elasticsearchEndPoint.replace("https://", "http://")
                JSONArray injectableValues = logOptions.getJSONArray("injectableVals")
                for (int i = 0; i < injectableValues.length(); i++) {
                    // add more ifs for other vars
                    JSONObject injectableObj = injectableValues.getJSONObject(i)
                    if (injectableObj.getString("type").equalsIgnoreCase("elasticsearchEndpoint")) {
                        envVars.add(["name": injectableObj.getString("name"), "value": formattedElasticsearchURL])
                    }
                }
                if (envVars.size() > 0) {
                    Map logConf = ["onlyVars": true, "envVars": envVars]
                    yaml.dump(logConf, logPathFileWriter)
                    logRequirementDetails = "onlyEnvVars null"
                } else {
                    logRequirementDetails = "False"
                }
                logPathFileWriter.close()
            }
        } else if (logRequirement == "None") {
            logRequirementDetails = "False"
        }
    } catch (RuntimeException e) {
        logRequirementDetails = "False"
    }
    println(logRequirementDetails)
}

/**
 * Args must be provided in following order
 * outputyaml_name   path_to_testLogs   path_to_Deployment.yaml
 */

confLogCapabilities(args[0], args[1], args[2])

