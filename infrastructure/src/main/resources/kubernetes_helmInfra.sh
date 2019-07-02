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

ClusterName=""
INPUT_DIR=$2
OUTPUT_DIR=$4
source $INPUT_DIR/testplan-props.properties


if [ -z $ClusterName ]
then
    SERVICE_ACCOUNT="gke-bot@testgrid.iam.gserviceaccount.com"
    CLUSTER_NAME="dev-test-cluster"
    ZONE="us-central1-a"
    PROJECT_NAME="testgrid" 
fi
#TODO
#functions

function check_tools() {
    echo "Please enable google cluster API, if not enabled."
    if ! type 'gcloud'
    then
        echo "installing gcloud - google cloud command line tool before you start with the setup"
        wget https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-247.0.0-linux-x86_64.tar.gz
        tar -xzf google-cloud-sdk-247.0.0-linux-x86_64.tar.gz
        cd google-cloud-sdk
        CLOUDSDK_CORE_DISABLE_PROMPTS=1 ./install.sh
        source path.bash.inc && source completion.bash.inc
        cd ..
    fi

    if ! type 'kubectl'
    then
        echo "installing Kubernetes command-line tool (kubectl) before you start with the setup"
        gcloud components install kubectl
    fi

}


function auth() {

     #authentication access to the google cloud
    gcloud auth activate-service-account --key-file=$INPUT_DIR/key.json

    #service account setup
    gcloud config set account $SERVICE_ACCOUNT

    #access the cluster
    gcloud container clusters get-credentials $CLUSTER_NAME --zone $ZONE --project $PROJECT_NAME

    rm $INPUT_DIR/key.json
    

    #install helm in not installed
    install_helm

}

function install_helm() {

    if ! type 'helm'
    then
        curl -LO https://git.io/get_helm.sh
        chmod 700 get_helm.sh
        ./get_helm.sh
        helm init
    fi
    
}
function create_randomName() {
    if [ -z $name ]
    then 
      echo "The name is not set"
    fi
    NAME="$name$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 5 | head -n 1)"
    echo $NAME
}

function create_namespace() {
    create_randomName
    kubectl create namespace $NAME
    kubectl config set-context $(kubectl config current-context) --namespace=$NAME
    kubectl config view | grep namespace:
}

function set_properties() {
    echo "namespace=$NAME" >> $OUTPUT_DIR/infrastructure.properties
    echo "randomPort=True">> $OUTPUT_DIR/infrastructure.properties
    echo "JDK=$JDK">> $OUTPUT_DIR/infrastructure.properties
    echo "DBEngineVersion=$DBEngineverstion">> $OUTPUT_DIR/infrastructure.properties
    echo "OS=$OS">> $OUTPUT_DIR/infrastructure.properties
    echo "DBEngine=$DBEngine">> $OUTPUT_DIR/infrastructure.properties
}

function infra_creation() {
    check_tools
    auth
    create_namespace
    set_properties
}

infra_creation

