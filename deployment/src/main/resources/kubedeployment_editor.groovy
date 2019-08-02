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


import org.json.JSONTokener
@Grapes(
        @Grab(group='org.yaml', module='snakeyaml', version='1.24')
)
@Grapes(
        @Grab(group='org.json', module='json', version='20180813')
)
import org.yaml.snakeyaml.Yaml
import org.json.JSONObject;
import org.json.JSONTokener;
/*
Adds a new Item to an Existing Property
 */
def AddNewItem(Map variable, String propertyName , Object new_Value){
    try{
        ArrayList prev_property = (ArrayList)variable.get(propertyName)
        prev_property.add(new_Value)
        variable.put(propertyName,prev_property)
        return variable
    }catch(RuntimeException e){
        println(e)
    }
}

/*
This method reads the config.properties file
 */
def readconfigProperties(){

    InputStream testplanInput = new FileInputStream("tpid.properties")
    InputStream configinput = new FileInputStream("/opt/testgrid/home/config.properties")
    Properties config = new Properties();
    Properties tpid = new Properties();
    config.load(configinput)
    tpid.load(testplanInput)
    config.setProperty("TESTPLANID",tpid.getProperty("tpID"))

    testplanInput.close()
    return config
}

/*
Adds Mount path and Sidecar container to the Deployment
 */
def EditDeployments(String outputYaml,String pathToOutputs, String pathToDeployment){

    println(pathToOutputs);
    try{
        InputStream is = new FileInputStream(jsonFilePath.toString());
        JSONTokener tokener = new JSONTokener(is);
        JSONObject json = new JSONObject(tokener);
        println(json.get("deploy-"))

        Yaml yaml = new Yaml()

        InputStream inputStream = new FileInputStream(pathToDeployment)

        Iterable<Object> KubeGroups = yaml.loadAll(inputStream);
        FileWriter fileWriter = new FileWriter(outputYaml)

        for (Object KubeGroup : KubeGroups ) {
            Map<String, Object> group = (Map<String, Object>) KubeGroup;

            if(group.is(null))break;
            if(group.get("kind").equals("Deployment")){

                new_VolumeMount = ["name":"logfilesmount", "mountPath":pathToOutputs]

                ArrayList newcontainerlist = []
                for ( Map container in group.get("spec").get("template").get("spec").get("containers")){
                    newcontainerlist.add(AddNewItem(container,"volumeMounts",new_VolumeMount))
                }
                group.get("spec").get("template").get("spec").put("containers", newcontainerlist)


                /*
                Change to persistent disk claim if using persistent disk
                 */
                Map emptyMap = [:]
                Map new_Volume = ["name": "logfilesmount" , "emptyDir": emptyMap ]
                group.get("spec").get("template").put("spec",AddNewItem(group.get("spec").get("template").get("spec"),"volumes",new_Volume))

                /*
               Remove entirely if using persistent disk
                */
                Properties configprops = readconfigProperties()
                Map new_Container = [ "name": "logfile-sidecar" , "image":"ranikamadurawe/mytag", "volumeMounts":[ ["name":"logfilesmount", "mountPath":"/testdata"]],
                                        "env": [ ["name": "nodename" , "valueFrom" : ["fieldRef" : ["fieldPath" : "spec.nodeName"]] ],
                                                 ["name": "podname" , "valueFrom" : ["fieldRef" : ["fieldPath" : "metadata.name"]] ],
                                                 ["name": "podnamespace" , "valueFrom" : ["fieldRef" : ["fieldPath" : "spec.metadata.namespace"]] ],
                                                 ["name": "podip" , "valueFrom" : ["fieldRef" : ["fieldPath" : "status.podIP"]] ],
                                                 ["name": "wsEndpoint" , "value": configprops.getProperty("DEPLOYMENT_TINKERER_EP") ],
        /* --NEED TO CHANGE -- */                ["name": "region" , "value": "US"  ],
                                                 ["name": "provider" , "value": "K8S" ],
                                                 ["name": "testPlanId" , "value": configprops.getProperty("TESTPLANID")  ],
                                                 ["name": "userName" , "value": configprops.getProperty("DEPLOYMENT_TINKERER_USERNAME") ],
                                                 ["name": "password" , "value": configprops.getProperty("DEPLOYMENT_TINKERER_PASSWORD") ],

                                        ],
                                      "command": ["/bin/bash", "-c", "./kubernetes_startup.sh && tail -f /dev/null" ]
                                    ]
                group.get("spec").get("template").put("spec",AddNewItem(group.get("spec").get("template").get("spec"),"containers",new_Container))

            }
            yaml.dump(group,fileWriter)
            fileWriter.write("---\n\n")

        }
        fileWriter.close()
        new File("tpid.properties").delete()
    }catch(RuntimeException e){
        println(e)
    }
}

/*
Args must be provided as --outputyaml_name --path_to_testLogs --path_to_Deployment.yaml
 */
EditDeployments(args[0],args[1],args[2])
