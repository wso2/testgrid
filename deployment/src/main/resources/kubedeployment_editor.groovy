@Grapes(
        @Grab(group='org.yaml', module='snakeyaml', version='1.24')
)

import org.yaml.snakeyaml.Yaml


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
def EditDeployments(String outputYaml,String pathToOutputs, String pathToDeployment){



    try{
        Yaml yaml = new Yaml()

        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(pathToDeployment);

        Iterable<Object> KubeGroups = yaml.loadAll(inputStream);
        FileWriter fileWriter = new FileWriter(outputYaml)

        for (Object KubeGroup : KubeGroups ) {
            Map<String, Object> group = (Map<String, Object>) KubeGroup;

            if(group.is(null))break;
            if(group.get("kind").equals("Deployment")){

                new_VolumeMount = ["name":"logfilesmount", "mountPath":pathToOutputs]

                ArrayList newcontainerlist = []
                for ( Map container in group.get("spec").get("template").get("spec").get("containers")){
                    newcontainerlist.add(EditProperty(container,"volumeMounts",new_VolumeMount))
                }
                group.get("spec").get("template").get("spec").put("containers", newcontainerlist)


                /*
                Change to persistent disk claim if using persistent disk
                 */
                Map emptyMap = [:]
                Map new_Volume = ["name": "logfilesmount" , "emptyDir": emptyMap ]
                group.get("spec").get("template").put("spec",EditProperty(group.get("spec").get("template").get("spec"),"volumes",new_Volume))

                /*
               Remove entirely if using persistent disk
                */
                Map new_Container = [ "name": "logfile-sidecar" , "image":"nginx", "volumeMounts":[ ["name":"logfilesmount", "mountPath":"/testdata"]]]
                group.get("spec").get("template").put("spec",EditProperty(group.get("spec").get("template").get("spec"),"containers",new_Container))

            }
            yaml.dump(group,fileWriter)
            fileWriter.write("---\n\n")

        }
        fileWriter.close()
    }catch(RuntimeException e){
        println(e)
    }



}

EditDeployments(args[0],args[1],args[2])