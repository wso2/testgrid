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


/*
   Makes and Adds
    1.NFS server deployment , service -> provides cluster IP
    2. Create persistant volume with cluster IP
    3. Add Claim
 */
def AddPersistantVolume(){

}

/*
This method adds a new YamlGroup to the File
 */
def writeToFile(outputYamlPath,Yamlcontent){
    FileWriter filewrite = new FileWriter(outputYamlPath,true)
    YamlWriter writer = new YamlWriter(filewrite);
    writer.getConfig().writeConfig.setIndentSize(2);
    writer.getConfig().writeConfig.setAutoAnchor(false);
    writer.getConfig().writeConfig.setWriteDefaultValues(false);
    writer.getConfig().writeConfig.setWriteRootTags(false);
    writer.getConfig().writeConfig.setWriteClassname(YamlConfig.WriteClassName.NEVER);
    writer.write(Yamlcontent)
    filewrite.write("---\n\n");
    writer.close()
}

/*
This method adds a new object to an already exiting property within a yaml file
 */
def EditProperty(Map variable, String propertyName , Object new_Value){

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
Adds Mount path and Sidecar container to the Deployment
 */
def EditDeployments(String outputYaml, String pathToOutputs, String pathToDeployment){

    YamlReader reader = new YamlReader(new FileReader(pathToDeployment))
    reader.getConfig().readConfig.setGuessNumberTypes(true);
    while (true) {
        try{
            Map YamlGroup = reader.read();
            if (YamlGroup == null) break;
            if( YamlGroup.get("kind").equals("Deployment")){

                new_VolumeMount = ["name":"logfilesmount", "mountPath":pathToOutputs]
                ArrayList newcontainerlist = []
                for ( Map container in YamlGroup.get("spec").get("template").get("spec").get("containers")){
                    newcontainerlist.add(EditProperty(container,"volumeMounts",new_VolumeMount))
                }
                YamlGroup.get("spec").get("template").get("spec").put("containers", newcontainerlist)

                /*
                Change to persistent disk claim if using persistent disk
                 */
                Map new_Volume = ["name": "logfilesmount" ]
                YamlGroup.get("spec").get("template").put("spec",EditProperty(YamlGroup.get("spec").get("template").get("spec"),"volumes",new_Volume))

                /*
               Remove entirely if using persistent disk
                */
                Map new_Container = [ "name": "logfile-sidecar" , "image":"nginx", "volumeMounts":[ ["name":"logfilesmount", "mountPath":"/testdata"]]]
                YamlGroup.get("spec").get("template").put("spec",EditProperty(YamlGroup.get("spec").get("template").get("spec"),"containers",new_Container))

            }
            writeToFile(outputYaml, YamlGroup)
        }catch(RuntimeException e){
            println("error occured")
        }


    }
}

EditDeployments(args[0],args[1],args[2])