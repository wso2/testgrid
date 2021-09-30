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

#set -o xtrace
alias wget='wget -q'
alias unzip='unzip -q'

#
#This script contains creation of infrastructure through helm. Basic authenticaiton and
#creation of namespace is done by this script
#

echo "Setting up Kubernetes infrastructure on GKE..."

ClusterName=""
INPUT_DIR=$2
OUTPUT_DIR=$4
source $INPUT_DIR/testplan-props.properties

GCLOUD_SDK_FILE="google-cloud-sdk-247.0.0-linux-x86_64.tar.gz"

#if the cluster name is not specified through input parametes it is assumed that the default
#testgrid cluster is used for the creation of resources.

if [ -z $ClusterName ]
then
    SERVICE_ACCOUNT="gke-bot@testgrid.iam.gserviceaccount.com"
    CLUSTER_NAME="dev-test-cluster"
    ZONE="us-central1-a"
    PROJECT_NAME="testgrid"
fi

#functions

function check_tools() {

    echo "Please enable google cluster API, if not enabled."
    if ! type 'gcloud'
    then
        echo "installing gcloud - google cloud command line tool..."
        set -x
        wget -q https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-247.0.0-linux-x86_64.tar.gz
        tar -xzf google-cloud-sdk-247.0.0-linux-x86_64.tar.gz
        cd google-cloud-sdk
        CLOUDSDK_CORE_DISABLE_PROMPTS=1 ./install.sh
        source path.bash.inc && source completion.bash.inc
        cd ..
        set +x
    fi
    
    if ! type 'kubectl'
    then
        curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.12.0/bin/linux/amd64/kubectl
        chmod +x ./kubectl
        sudo mv ./kubectl /usr/bin/kubectl
        which kubectl
        kubectl version
    else
        curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.12.0/bin/linux/amd64/kubectl
        chmod +x ./kubectl
        dir=$(which kubectl)
        sudo mv ./kubectl $dir
        which kubectl
        kubectl version
    fi

}

## exit code 78 == gcloud authentication key missing
function auth() {
    echo 'authenticating to gcloud...'
    if [[ ! -f $INPUT_DIR/key.json ]]; then
        >&2 echo "[ERROR] Cannot authenticate to gcloud. Expected the private key of GKE service account, " \
        "$SERVICE_ACCOUNT(key.json).
    If you are running TG locally, then request a key from TG team, and place it at $INPUT_DIR/key.json. " \
    "After that, re-run the command."
    sleep .1 && exit 140
    fi
    gcloud auth activate-service-account --key-file=$INPUT_DIR/key.json

    #service account setup
    gcloud config set account $SERVICE_ACCOUNT

    #access the cluster
    gcloud container clusters get-credentials $CLUSTER_NAME --zone $ZONE --project $PROJECT_NAME

}

function create_randomName() {
    if [ -z $name ]
    then
      echo "The namespace prefix is not set"
    fi
    NAME="$name$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 5 | head -n 1)"
    echo $NAME
}

#creation of namespace for the deployment of resources.
function create_namespace() {
    echo "generating random namespace for deployment..."
    create_randomName
    kubectl create namespace $NAME
    kubectl config set-context $(kubectl config current-context) --namespace=$NAME
    kubectl config view | grep namespace:
}

#This function will set properties which will be used by the deploy phase
function set_properties() {
    echo "namespace=$NAME" >> $OUTPUT_DIR/infrastructure.properties
    echo "randomPort=True">> $OUTPUT_DIR/infrastructure.properties
    echo "JDK=$JDK">> $OUTPUT_DIR/infrastructure.properties
    echo "DBEngineVersion=$DBEngineverstion">> $OUTPUT_DIR/infrastructure.properties
    echo "OS=$OS">> $OUTPUT_DIR/infrastructure.properties
    echo "DBEngine=$DBEngine">> $OUTPUT_DIR/infrastructure.properties
}

function print_summary() {
    echo "
    ---------------------- Kubernetes cluster details ----------------------
    KUBERNETES_CLOUD_PROVIDER=GCP-GKE
    KUBERNETES_CLUSTER_NAME=$CLUSTER_NAME
    KUBERNETES_NAMESPACE=$NAME
    SERVICE_ACCOUNT=$SERVICE_ACCOUNT
    GCP_PROJECT_NAME=$PROJECT_NAME
    GCP_ZONE=$ZONE
    GCP_URL=https://console.cloud.google.com/kubernetes/list?project=$PROJECT_NAME&authuser=0
    ------------------------------------------------------------------------
    "
}

function install_helm(){

  #if helm is not installed in the cluster, helm and tiller will be installed.
  if ! type 'helm'
  then
    wget https://get.helm.sh/helm-v3.0.0-alpha.2-linux-amd64.tar.gz
    tar -zxvf helm-v3.0.0-alpha.2-linux-amd64.tar.gz
    sudo chmod + linux-amd64/helm
    sudo mv linux-amd64/helm /usr/bin
    helm init
    helm version
  fi
  if [[ ! -d /home/ubuntu/.helm ]]; then
       echo "Helm is already installed. But not initialized. Init before using"
    helm init
  fi
}

function infra_creation() {
    check_tools
    auth
    create_namespace
    set_properties
    print_summary
    install_helm
    echo "Kubernetes+Helm infrastructure setup completed."

}

infra_creation
