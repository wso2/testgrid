/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
        @Grab(group='com.esotericsoftware.yamlbeans', module='yamlbeans', version='1.13')
)

import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import com.esotericsoftware.yamlbeans.YamlConfig


def writeToFile(outputYamlPath,Yamlcontent){
    FileWriter filewrite = new FileWriter(outputYamlPath,true)
    YamlWriter writer = new YamlWriter(filewrite);
    writer.getConfig().writeConfig.setIndentSize(2);
    writer.getConfig().writeConfig.setAutoAnchor(false);
    writer.getConfig().writeConfig.setWriteDefaultValues(true);
    writer.getConfig().writeConfig.setWriteRootTags(false);
    writer.getConfig().writeConfig.setWriteClassname(YamlConfig.WriteClassName.NEVER);
    writer.write(Yamlcontent)
    filewrite.write("---\n\n");
    writer.close()
}
/*
Adds persistant disk to Deployment.yaml
 */
def AddPersistantDisk(String outputYaml){
    Map persistantDiskYaml = [ "apiVersion": "v1" ,"kind": "PersistentVolumeClaim" ,"metadata": [ "name": "myclaim" ] ,"spec" : [ "accessModes":  ["ReadWriteOnce"]
                                                                                                                                  ,"volumeMode": "Filesystem" , "resources " : [ "requests" : [ "storage": "8Gi" ] ], "selector":  ["matchLabels": [ "release" : "stable"]]]]
    writeToFile(outputYaml,persistantDiskYaml )
}

/*
This method adds a new object to an already exiting property within a yaml file
 */
def EditProperty(Map variable, String propertyName , Object new_Value){
    ArrayList prev_property = (ArrayList)variable.get(propertyName)
    prev_property.add(new_Value);
    variable.put(propertyName,prev_property)
    return variable
}

/*
Adds the Persistant Volume Claims to the Deployment
 */
def EditDeployments(String outputYaml, String pathToOutputs, String pathToDeployment){
    YamlReader reader = new YamlReader(new FileReader(pathToDeployment));
    while (true) {
        try{
            Map deployments = reader.read();
            if (deployments == null) break;
            if( deployments.get("kind").equals("Deployment")){
                new_Value = ["name":"logfilesmount", "mountPath":pathToOutputs]
                for ( Map container in deployments.get("spec").get("template").get("spec").get("containers")){
                    deployments.get("spec").get("template").get("spec").put("containers",EditProperty(container,"volumeMounts",new_Value));
                }
                new_Volume = ["name": "logfilesmount", "PersistentVolumeClaim": ["claimName": "testgridclaim"] ]
                deployments.get("spec").get("template").put("spec",EditProperty(deployments.get("spec").get("template").get("spec"),"volumes",new_Volume))
            }
            writeToFile(outputYaml, deployments)
        }catch(RuntimeException e){

        }


    }
}

EditDeployments(args[0],args[1],args[2])
AddPersistantDisk(args[0])