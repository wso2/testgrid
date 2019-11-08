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

#deployment namespace
namespace=$1
#elastic search endpoint
esEP=$2
#Sidecar requirement
req=$3
#Filename of the file which is to become the logstash.conf file
filename=$4

S3_REGION=$5
S3_BUCKET=$6
S3_SECRET_KEY=$7
S3_KEY_ID=$8

# provide execution access for scripts needed for creating sidecar
chmod 777 ./testgrid-sidecar/create.sh
chmod 777 ./testgrid-sidecar/deployment/patchnamespace.sh
chmod 777 ./testgrid-sidecar/deployment/webhook-create-signed-cert.sh
chmod 777 ./testgrid-sidecar/deployment/webhook-patch-ca-bundle.sh
chmod 777 ./testgrid-sidecar/deployment/patchesendpoint.sh
chmod 777 ./testgrid-sidecar/deployment/patchs3details.sh

# create config map for sidecar injector deployment to mount which contains details about deployments, containers
# and location to extact logs from
kubectl create configmap --dry-run logpath-config --from-file=./testgrid-sidecar/deployment/logpath-details.yaml --output yaml | tee ./testgrid-sidecar/deployment/logpath-configmap.yaml

if [[ "$req" == "SidecarReq" ]]
then
  # take the file chosen as the logstash.conf file and create it
  logstashconffile="./testgrid-sidecar/deployment/confs/logstash.conf"
  if [ -f $logstashconffile ] ; then
      rm $logstashconffile
  fi
  cp ./testgrid-sidecar/deployment/confs/${filename} ${logstashconffile}
  kubectl create configmap --dry-run logstash-conf --from-file=./testgrid-sidecar/deployment/confs/logstash.conf --output yaml | tee ./testgrid-sidecar/deployment/logconf.yaml
fi

# create signed keys for mutating webhook
./testgrid-sidecar/deployment/webhook-create-signed-cert.sh \
    --service sidecar-injector-webhook-svc \
    --secret sidecar-injector-webhook-certs \
    --namespace ${namespace}

# patch CA_BUNDLE field within mutatingwebhook.yaml
cat ./testgrid-sidecar/deployment/mutatingwebhook_template.yaml | \
    ./testgrid-sidecar/deployment/webhook-patch-ca-bundle.sh ${namespace}> \
    ./testgrid-sidecar/deployment/mutatingwebhook_temp.yaml

# patch NAMESPACE field within mutatingwebhook.yaml
cat ./testgrid-sidecar/deployment/mutatingwebhook_temp.yaml | \
    ./testgrid-sidecar/deployment/patchnamespace.sh ${namespace} > \
    ./testgrid-sidecar/deployment/mutatingwebhook-ca-bundle.yaml

# patch elastic search endpoint within the logstash collector deployment template
cat ./testgrid-sidecar/deployment/logstash-collector-template.yaml | \
    ./testgrid-sidecar/deployment/patchesendpoint.sh ${esEP} > \
    ./testgrid-sidecar/deployment/logstash-collector_temp.yaml

cat ./testgrid-sidecar/deployment/logstash-collector_temp.yaml | \
    ./testgrid-sidecar/deployment/patchs3details.sh ${S3_KEY_ID} ${S3_SECRET_KEY} ${S3_REGION} ${S3_BUCKET} > \
    ./testgrid-sidecar/deployment/logstash-collector.yaml

if [[ "$req" == "SidecarReq" ]]
then
  # resources required only if Sidecar Injection is required
  kubectl create -f ./testgrid-sidecar/deployment/filebeatyaml.yaml --namespace ${namespace}
  kubectl create -f ./testgrid-sidecar/deployment/logstashyaml.yaml --namespace ${namespace}
  kubectl create -f ./testgrid-sidecar/deployment/logconf.yaml --namespace ${namespace}
  kubectl create -f ./testgrid-sidecar/deployment/logstash-collector.yaml --namespace ${namespace}
  kubectl create -f ./testgrid-sidecar/deployment/logstash-service.yaml --namespace ${namespace}
fi

# resources required in general regardless of wether it is a sidecar injection or an env var injection
kubectl create -f ./testgrid-sidecar/deployment/logpath-configmap.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/configmap.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/deployment.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/service.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/mutatingwebhook-ca-bundle.yaml --namespace ${namespace}


