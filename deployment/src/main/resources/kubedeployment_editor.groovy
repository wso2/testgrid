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

@Grapes(
        @Grab(group='org.yaml', module='snakeyaml', version='1.24')
)

import org.yaml.snakeyaml.Yaml

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
Adds Mount path and Sidecar container to the Deployment
 */
def EditDeployments(String outputYaml,String pathToOutputs, String pathToDeployment){

    try{
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
                Map new_Container = [ "name": "logfile-sidecar" , "image":"nginx", "volumeMounts":[ ["name":"logfilesmount", "mountPath":"/testdata"]]]
                group.get("spec").get("template").put("spec",AddNewItem(group.get("spec").get("template").get("spec"),"containers",new_Container))

            }
            yaml.dump(group,fileWriter)
            fileWriter.write("---\n\n")

        }
        fileWriter.close()
    }catch(RuntimeException e){
        println(e)
    }
}

/*
Args must be provided as --outputyaml_name --path_to_testLogs --path_to_Deployment.yaml
 */
EditDeployments(args[0],args[1],args[2])