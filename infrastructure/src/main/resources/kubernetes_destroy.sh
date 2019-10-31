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
#
#this script would destroy the resources created. The namespace will contain all the
#resources created for this testPlan. Hence the namespace will be destroyed to destroy all
#the resources created.
#

#definitions
INPUT_DIR=$2
ETC_HOSTS=/etc/hosts
source $INPUT_DIR/deployment.properties

read_property_file() {
    local property_file_path=$1
    # Read configuration into an associative array
    # IFS is the 'internal field separator'. In this case, your file uses '='
    local -n configArray=$2
    IFS="="
    while read -r key value
    do
      [[ -n ${key} ]] && configArray[$key]=$value
    done < ${property_file_path}
    unset IFS
}

function delete_resources() {
  echo "running destroy.sh"
  kubectl delete namespaces $namespace
  kubectl delete mutatingwebhookconfiguration "sidecar-injector-webhook-cfg-${namespace}"
  webhookadded=$(kubectl get mutatingwebhook "sidecar-injector-webhook-cfg-${namespace}" -o jsonpath='{.status.conditions[?(@.type=="Available")].status}')
  if [[ "$webhookadded" == "True" ]]
  then 
     kubectl delete mutatingwebhookconfiguration "sidecar-injector-webhook-cfg-${namespace}"
  fi
}


function removehost() {
    HOSTNAME=$1
    if [ -n "$(grep $HOSTNAME /etc/hosts)" ]
    then
        echo "$HOSTNAME Found in your $ETC_HOSTS, Removing now...";
        echo $TESTGRID_PASS | sudo -S sed -i".bak" "/$HOSTNAME/d" $ETC_HOSTS
    else
        echo "$HOSTNAME was not found in your $ETC_HOSTS";
    fi
}

declare -g -A infra_props

TESTGRID_ENVIRONMENT=${ENVIRONMENT}
TESTGRID_PASS=${PASS}

if [ -z "$TESTGRID_ENVIRONMENT" ]; then
  env='dev'
else
  env=${TESTGRID_ENVIRONMENT}
fi


if [[ "${env}" != "dev" ]] && [[ "${env}" != 'prod' ]]; then
    removehost $loadBalancerHostName
    return;
fi

delete_resources
