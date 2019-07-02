#!/bin/bash

#-------------------------------------------------------------------------------
# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#--------------------------------------------------------------------------------

set -o xtrace
echo "deploy file is found"

OUTPUT_DIR=$4
INPUT_DIR=$2
source $INPUT_DIR/infrastructure.properties

#definitions

YAMLS=$yamls

yamls=($YAMLS)
no_yamls=${#yamls[@]}
dep=($deployments)
dep_num=${#dep[@]}

function create_resources() {

    #create the deployments

    if [ -z $deployments ]
    then
      echo "No deployment is given. Please makesure to give atleast one deployment"
      exit 1
    fi

    if [ -z $yamlFilesLocation ]; then
      echo "the yaml files location is not given"
      exit 1
    fi

    #create values.yaml file
    create_value_yaml

    #transfer yaml files created to deploy helm deployments
    transfer_yaml_files

    #install helm 
    install_helm

    i=0;
    for ((i=0; i<$dep_num; i++))
    do
      kubectl expose deployment ${dep[$i]} --name=${dep[$i]}  --type=LoadBalancer -n $namespace
    done

    readinesss_services

    echo "namespace=$namespace" >> $OUTPUT_DIR/deployment.properties
}

function readiness_deployments(){
    i=0;
    # todo add a terminal condition/timeout.
    for ((i=0; i<$dep_num; i++)) ; do
      num_true=0;
      while [ "$num_true" -eq "0" ] ; do
        sleep 5
        deployment_status=$(kubectl get deployments -n $namespace ${dep[$i]} -o jsonpath='{.status.conditions[?(@.type=="Available")].status}')
        if [ "$deployment_status" == "True" ] ; then
          num_true=1;
        fi
      done
    done
}

function readinesss_services(){
    i=0;
    for ((i=0; i<$dep_num; i++)); do
      external_ip=""

      while [ -z $external_ip ]; do
        echo "Waiting for end point..."
        external_ip=$(kubectl get service ${dep[$i]} --template="{{range .status.loadBalancer.ingress}}{{.ip}}{{end}}" --namespace ${namespace})
        #external_ip=$(kubectl get ingress ${ingressName} --template="{{range .status.loadBalancer.ingress}}{{.ip}}{{end}}" --namespace ${namespace})
        [ -z "$external_ip" ] && sleep 10
      done
      echo "PublisherUrl=https://$external_ip:9443/publisher" >> $OUTPUT_DIR/deployment.properties
      echo "StoreUrl=https://$external_ip:9443/store" >> $OUTPUT_DIR/deployment.properties
      echo "AdminUrl=https://$external_ip:9443/admin" >> $OUTPUT_DIR/deployment.properties
      echo "CarbonServerUrl=https://$external_ip:9443/services/" >> $OUTPUT_DIR/deployment.properties
      echo "CarbonServerUrl=https://$external_ip:9443/services/" >> $OUTPUT_DIR/deployment.properties
      echo "GatewayHttpsUrl=https://$external_ip:8243" >> $OUTPUT_DIR/deployment.properties
      done
}


function create_value_yaml(){

cat > values.yaml << EOF
username: $WUMUsername
password: $WUMPassword
email: $WUMUsername

namespace: $namespace
svcaccount: "wso2svc-account"
dbType: $DBEngine
operatingSystem: $OS
jdkType: $JDK
EOF
yes | cp -rf values.yaml $deploymentRepositoryLocation/

}

#transfer yaml files to templates to be used as helm deployments.
function transfer_yaml_files(){

 i=0;
 for ((i=0; i<$no_yamls; i++))
 do
 echo ${yamls[$i]}
 yes | cp -rf ${yamls[$i]} $deploymentRepositoryLocation/templates/
 done

}

function install_helm(){

  if [ -z helm ]
  then
    curl -LO https://git.io/get_helm.sh
    chmod 700 get_helm.sh
    ./get_helm.sh
    #resources_deployment
    kubectl create serviceaccount --namespace kube-system tiller
    kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=kube-system:tiller
    kubectl patch deploy --namespace kube-system tiller-deploy -p '{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}'
  fi
  helmDeployment="wso2apim$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 5 | head -n 1)"
  helm install --name $helmDeployment $deploymentRepositoryLocation/

  readiness_deployments

}

function resources_deployment(){

    if [ "$DBEngine" == "mysql"] 
    then
        helm install --name wso2is-rdbms-service -f $deploymentRepositoryLocation/mysql/values.yaml stable/mysql --namespace $namespace
    fi
    if [ "$DBEngine" == "postgresql"] 
    then
        helm install --name wso2is-rdbms-service -f $deploymentRepositoryLocation/postgresql/values.yaml stable/postgresql --namespace $namespace
    fi
    if [ "$DBEngine" == "mssql"] 
    then
        helm install --name wso2is-rdbms-service -f $deploymentRepositoryLocation/mssql/values.yaml stable/mssql-linux --namespace $namespace
        kubectl create -f $deploymentRepositoryLocation/jobs/db_provisioner_job.yaml --namespace $namespace
    fi

}

create_resources