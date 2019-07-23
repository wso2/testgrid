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

import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;

/*
This method adds a new object which does not exist in a object file
 */
def AddProperty(Map deployment, String propertyName , Object new_Value){

}

/*
This method removes an object to an already exiting property within a yaml file
 */
def RemoveProperty(Map deployment, String propertyName , Object new_Value){

}

/*
This method replaces object to an already exiting property within a yaml file
 */
def ReplaceProperty(Map deployment, String propertyName , Object new_Value){

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

def call(String pathtofile){
    YamlReader reader = new YamlReader(new FileReader("deployment.yaml"));
    while (true) {
        try{
            Map deployments = reader.read();
            if (deployments == null) break;
            if( deployments.get("kind").equals("Deployment")){
                new_Value = ["name":"logfilesmount", "mountPath":pathtofile]
                for ( Map container in deployments.get("spec").get("template").get("spec").get("containers")){
                    deployments.get("spec").get("template").get("spec").put("containers",EditProperty(container,"volumeMounts",new_Value));
                }
                new_Volume = ["name": "logfilesmount", "PersistentVolumeClaim": ["claimName": "testgridclaim"] ]
                deployments.get("spec").get("template").put("spec",EditProperty(deployments.get("spec").get("template").get("spec"),"volumes",new_Volume))
            }
            FileWriter filewrite = new FileWriter("output.yaml",true)
            YamlWriter writer = new YamlWriter(filewrite);
            writer.getConfig().writeConfig.setIndentSize(2);
            writer.getConfig().writeConfig.setAutoAnchor(false);
            writer.getConfig().writeConfig.setWriteDefaultValues(true);
            writer.getConfig().writeConfig.setWriteRootTags(false);
            writer.getConfig().writeConfig.setWriteClassname(YamlConfig.WriteClassName.NEVER);

            writer.write(deployments)
            filewrite.write("---\n\n");
            writer.close()
        }catch(GroovyCastException e){
            // logger.into("unrecoginized yaml section found");
        }


    }
}

call("/home/user/path/to/destination")