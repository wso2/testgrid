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

# Read a property file to a given associative array
#
# $1 - Property file
# $2 - associative array
# How to call
# declare -A somearray
# read_property_file testplan-props.properties somearray
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

function install_helm(){

  #if helm is not installed in the cluster, helm and tiller will be installed.
  if ! type 'helm'
  then
    wget https://get.helm.sh/helm-v3.0.0-alpha.2-linux-amd64.tar.gz
    tar -zxvf helm-v3.0.0-alpha.2-linux-amd64.tar.gz
    mkdir ~/.local/bin/
    PATH=~/.local/bin/:$PATH
    mv linux-amd64/helm ~/.local/bin/helm
    ~/.local/bin/helm init
    helm version

  fi
}



#deployment namespace
namespace=$1
#Sidecar requirement
req=$2
#Filename of the file which is to become the logstash.conf file
filename=$3
#Location of the infrastructure.properties file
INPUT_DIR=$4

declare -g -A infra_props
read_property_file "${INPUT_DIR}/infrastructure.properties" infra_props
elasticsearchEndPoint=${infra_props["elasticsearchEndPoint"]}
s3Region=${infra_props["s3Region"]}
s3Bucket=${infra_props["s3Bucket"]}
s3secretKey=${infra_props["s3secretKey"]}
s3accessKey=${infra_props["s3accessKey"]}
s3logPath=${infra_props["s3logPath"]}

install_helm

# Format elastic search Endpoint and to support logstash pipeline
if  [[ $elasticsearchEndPoint == https://* ]]  ;
then :
  oldString=https://
  repString=http://
  elasticsearchEndPoint=$(echo ${elasticsearchEndPoint/$oldString/$repString})
elif [[ $elasticsearchEndPoint == http:// ]] ;
then  :
else
   elasticsearchEndPoint=http://$1
fi

# provide execution access for scripts needed for creating sidecar
chmod 777 ./testgrid-sidecar/deployment/webhook-create-signed-cert.sh

# create config map for sidecar injector deployment to mount which contains details about deployments, containers
# and location to extact logs from
kubectl create configmap --dry-run logpath-config --from-file=./testgrid-sidecar/deployment/logpath-details.yaml --output yaml | tee ./testgrid-sidecar/deployment/helmchart/templates/logpath-configmap.yaml

if [[ "$req" == "SidecarReq" ]]
then
  sidecar_Req="true"
  # take the file chosen as the logstash.conf file and create it
  logstashconffile="./testgrid-sidecar/deployment/confs/logstash.conf"
  if [ -f $logstashconffile ] ; then
      rm $logstashconffile
  fi
  cp ./testgrid-sidecar/deployment/confs/${filename} ${logstashconffile}
  kubectl create configmap --dry-run logstash-conf --from-file=./testgrid-sidecar/deployment/confs/logstash.conf --output yaml | tee ./testgrid-sidecar/deployment/helmchart/templates/logconf.yaml
else
  sidecar_Req="false"
fi

# create signed keys for mutating webhook
./testgrid-sidecar/deployment/webhook-create-signed-cert.sh \
    --service sidecar-injector-webhook-svc \
    --secret sidecar-injector-webhook-certs \
    --namespace ${namespace}

ca_bundle=$(kubectl config view --raw --minify --flatten -o jsonpath='{.clusters[].cluster.certificate-authority-data}')

# create value.yaml for helm chart
cat > ./testgrid-sidecar/deployment/helmchart/values.yaml << EOF
wso2:
   sidecarReq: ${sidecar_Req}
   namespace: ${namespace}
   cabundle: ${ca_bundle}
   elasticSearch:
      esEndpoint: ${elasticsearchEndPoint}
   s3Vars:
      s3secretkey: ${s3secretKey}
      s3accesskey: ${s3accessKey}
      s3RegionName: ${s3Region}
      s3Bucket: ${s3Bucket}
      s3BucketPath: ${s3logPath}
EOF

helm install mwh-${namespace} ./testgrid-sidecar/deployment/helmchart -n ${namespace}

TIMEOUT=600 # 10mins
total_count=$((TIMEOUT/5))
echo $total_count
count=0
echo Running kubectl get deployments -n ${namespace} sidecar-injector-webhook-deployment -o jsonpath='{.status.conditions[?(@.type=="Available")].status}'
until [[ $count -ge $total_count ]]
do
    deployment_status=$(kubectl get deployments -n $namespace sidecar-injector-webhook-deployment -o jsonpath='{.status.conditions[?(@.type=="Available")].status}')
    [[ "$deployment_status" == "True" ]] && break
    count=$(($count+1))
    sleep 5;
done
[[ "$deployment_status" != "True" ]] && echo "[ERROR] timeout while waiting for deployment, '${dep[$i]}', in \
namespace, '$namespace', to succeed." && exit 78

sleep 30;
