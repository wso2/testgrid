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
        @Grab(group='org.yaml', module='snakeyaml', version='1.24')
)
@Grapes(
        @Grab(group='org.json', module='json', version='20180813')
)
import org.yaml.snakeyaml.Yaml
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONTokener

/**
 * Formats the user provided inputs to a form that can be utilized by the sidecar container and log extraction
 * container
 *
 * @param loglocations JSONArray containing the log locations
 * @param i index of the required JSONobject in the array
 * @return
 */

def formatFilePaths(String loglocations){

    String containerFilepath
    if( loglocations.startsWith('/') && loglocations.endsWith('/') ){
        containerFilepath = loglocations
    }else if( loglocations.startsWith('/') && !loglocations.endsWith('/') ){
        containerFilepath = loglocations+"/"
    }else if( !loglocations.startsWith('/') && loglocations.endsWith('/') ){
        containerFilepath = "/"+loglocations
    }else{
        containerFilepath = "/"+loglocations+"/"
    }
    return containerFilepath

}


/**
 * Adds Mount path and Sidecar container to the Deployment
 * @param outputYaml - file name of the edited yaml file
 * @param depInJSONFilePath - file path to the json file containing input Parameters
 * @param pathToDeployment - file path to the input Yaml file
 *
 */
def confLogCapabilities(String logPathConfYamlLoc, String depInJSONFilePath, String depType, String esEndpoint,
                        String depRepo){
    try{
        // Read json file
        InputStream depInJSONinputStream = new FileInputStream(depInJSONFilePath.toString())
        JSONTokener tokener = new JSONTokener(depInJSONinputStream)
        JSONObject depInJSON = new JSONObject(tokener)

        JSONObject logOptions = depInJSON.getJSONObject("dep-in").getJSONObject("log-Options");
        String logRequirment = logOptions.getString("logRequirement")

        if (logRequirment.equals("Sidecar-Required")) {
            JSONArray loglocations = logOptions.getJSONArray("logFileLocations")
            Yaml yaml = new Yaml()

            FileWriter fileWriter = new FileWriter(logPathConfYamlLoc)

            if (loglocations.length() != 0){
                List logpathConf = []
                for (JSONObject logLocation in loglocations) {
                    String formatFilePath = formatFilePaths(logLocation.getString("path"))
                    Map entry = ["name" : logLocation.getString("deploymentname") + "-" +
                            logLocation.getString("containername") , "path" : formatFilePath]
                    logpathConf.add( entry )
                }
                Map logconf = [ "loglocs" : logpathConf]
                yaml.dump(logconf,fileWriter)
                println("True")
            }else{
                println("False")
            }
            fileWriter.close()
            return
        } else if ( logRequirment.equals("esEndpoint-Required") ) {
            if (depType.equals("helm")) {
                String valuesYamlLoc = depRepo.concat("/").concat(depInJSON.getString("rootProjLocation"))
                        .concat("/").concat(logOptions.getString("valuesYamlLocation"));
                String esEndPointEditLoc = logOptions.getString("esEndpointLoc").split(":")[0]
                InputStream valuesYamlInputStream = new FileInputStream(valuesYamlLoc);
                Yaml yaml = new Yaml()
                Map valuesYaml = yaml.load(valuesYamlInputStream)
                valuesYamlInputStream.close()
                List<String> pathToesEndPoint = esEndPointEditLoc.split("/")
                Map esEndPointMap = valuesYaml
                String key;
                for ( int i = 0 ; i < pathToesEndPoint.size() -1 ;  i++ ) {
                    key = pathToesEndPoint[i];
                    esEndPointMap = esEndPointMap[key]
                }
                esEndPointMap[pathToesEndPoint[pathToesEndPoint.size()-1]] = esEndpoint
                FileWriter valuesYamlOutputStream = new FileWriter(valuesYamlLoc)
                yaml.dump(valuesYaml,valuesYamlOutputStream)
                valuesYamlOutputStream.close();
                println("False")
                return
            } else if ( depType.equals("k8s")) {
                FileWriter fileWriter = new FileWriter(logPathConfYamlLoc)
                Yaml yaml = new Yaml()
                Yaml logpathConfYaml = new Yaml()
                Map logconf = [ "onlyes":true , "esEnvVarName": logOptions.getString("esVarName")]
                logpathConfYaml.dump(logconf)
                yaml.dump(logpathConfYaml,fileWriter)
                println("True")
                fileWriter.close()
                return
            }
        } else if ( logRequirment.equals("None") ) {
            println("False")
            return
        }
    }catch(RuntimeException e){
        println("False")
    }
}

/**
 * Args must be provided in following order
 * outputyaml_name   path_to_testLogs   path_to_Deployment.yaml
 */
confLogCapabilities(args[0],args[1],args[2],args[3],args[4])
