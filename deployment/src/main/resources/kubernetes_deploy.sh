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
#This class is used for the deployment of resources into the namespace using kubectl commands
#The created resources will be exposed using an Ingress to the external usage
#

echo "deploy file is found"
dryRun=False

OUTPUT_DIR=$4
INPUT_DIR=$2
source $INPUT_DIR/infrastructure.properties
source $OUTPUT_DIR/deployment.properties

cat $OUTPUT_DIR/deployment.properties
#definitions

deploymentYamlFiles=($deploymentYamlFiles)
no_yamls=${#deploymentYamlFiles[@]}
dep=($exposedDeployments)
dep_num=${#dep[@]}

function edit_deployments(){

        i=0;
        for ((i=0; i<$no_yamls; i++))
        do
          groovy kubedeployment_editor.groovy deployment${i}_temp.yaml $TestFileLocation $yamlFilesLocation/${deploymentYamlFiles[$i]}
          rm $yamlFilesLocation/${deploymentYamlFiles[$i]}
          mv deployment${i}_temp.yaml  $yamlFilesLocation/${deploymentYamlFiles[$i]}
        done

}


function create_k8s_resources() {

    if [ -z $deploymentYamlFiles ]
    then
      echo "the yaml file is not created or the yaml file is not available"
      exit 1
    fi

    if [ -z $exposedDeployments ]
    then
      echo "No deployment is given. Please makesure to give atleast one deployment"
      exit 1
    fi


    if [ -z ${loadBalancerHostName} ]; then
        echo WARN: loadBalancerHostName not found in deployment.properties. Generating a random name under \
        *.gke.wso2testgrid.com CN
        loadBalancerHostName=wso2am-$(($RANDOM % 10000)).gke.wso2testgrid.com # randomized hostname
    else
        echo DEBUG: loadBalanceHostName: ${loadBalancerHostName}
    fi

    if [[ ${dryRun^^} != TRUE ]]; then
        i=0;
        for ((i=0; i<$no_yamls; i++))
        do
          kubectl create -f $yamlFilesLocation/${deploymentYamlFiles[$i]}
        done
    fi

    readiness_deployments
    sleep 10

# TODO: install ingress-nginx controller if not found.

# Create a ingress for the services we want to expose to public internet.
tlskeySecret=testgrid-certs
ingressName=tg-ingress
kubectl create secret tls ${tlskeySecret} \
    --cert $INPUT_DIR/testgrid-certs-v2.crt  \
    --key $INPUT_DIR/testgrid-certs-v2.key -n $namespace

    cat > ${ingressName}.yaml << EOF
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: ${ingressName}
  namespace: ${namespace}
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
spec:
  tls:
  - hosts:
    - mgt-${loadBalancerHostName}
    - gw-${loadBalancerHostName}
    - ${loadBalancerHostName}
    secretName: ${tlskeySecret}
  rules:
EOF
    i=0;
    for ((i=0; i<$dep_num; i++))
    do
      echo
      kubectl expose deployment ${dep[$i]} --name=${dep[$i]} -n $namespace
#      kubectl expose deployment ${dep[$i]} --name=${dep[$i]}  --type=LoadBalancer -n $namespace
      cat >> ${ingressName}.yaml << EOF
  - host: mgt-${loadBalancerHostName}
    http:
      paths:
      - backend:
          serviceName: ${dep[$i]}
          servicePort: 9443 # TODO: FIX THIS - this also need to come from the testgrid.yaml.
  - host: gw-${loadBalancerHostName}
    http:
      paths:
      - backend:
          serviceName: ${dep[$i]}
          servicePort: 8243 # TODO: FIX THIS - this also need to come from the testgrid.yaml.
  - host: ${loadBalancerHostName}
    http:
      paths:
      - backend:
          serviceName: ${dep[$i]}
          servicePort: 9443 # TODO: FIX THIS - this also need to come from the testgrid.yaml.
EOF
    done
    echo Final ingress yaml:
    cat ${ingressName}.yaml
    kubectl apply -f ${ingressName}.yaml -n $namespace

    readinesss_services

    echo "namespace=$namespace" >> $OUTPUT_DIR/deployment.properties
    echo "loadBalancerHostName=$loadBalancerHostName" >> $OUTPUT_DIR/deployment.properties
}

#This function constantly check whether the deployments are correctly deployed in the cluster
function readiness_deployments(){
    start=`date +%s`
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

    end=`date +%s`
    runtime=$((end-start))
    echo "Deployment \"${dep}\" got ready in ${runtime} seconds."
    echo
}

#This function check whether the ingress service created is correctly deployed in the cluster
function readinesss_services(){
    start=`date +%s`
    i=0;
    for ((i=0; i<$dep_num; i++)); do
      external_ip=""
      echo "Getting the ingress IP address for ingress: ${ingressName}"
      while [ -z $external_ip ]; do
        echo "Waiting for end point..."
#        external_ip=$(kubectl get service ${dep[$i]} --template="{{range .status.loadBalancer.ingress}}{{.ip}}{{end}}" --namespace ${namespace})
        external_ip=$(kubectl get ingress ${ingressName} --template="{{range .status.loadBalancer.ingress}}{{.ip}}{{end}}" --namespace ${namespace})
        [ -z "$external_ip" ] && sleep 10
      done
    echo "loadBalancerHostName=${loadBalancerHostName}" >> $OUTPUT_DIR/deployment.properties
    echo "loadBalancerIP=${external_ip}" >> $OUTPUT_DIR/deployment.properties

    done

    end=`date +%s`
    runtime=$((end-start))
    echo "Kubernetes Ingress service '${ingressName}' got ready in ${runtime} seconds."

}

#This function is used to direct accesss to the Ingress created from the AWS ec2 instances.
#Host mapping service provided by AWS, route53 is used for this purpose.
function add_route53_entry() {
    env=${TESTGRID_ENVIRONMENT} || 'dev'
    if [[ "${env}" != "dev" ]] && [[ "${env}" != 'prod' ]]; then
        echo "Not configuring route53 DNS entries since the environment is not dev/prod. You need to manually add
        '${external_ip} ${loadBalancerHostName}' into your /etc/hosts."
        return;
    fi

    command -v aws >/dev/null 2>&1 || { echo >&2 "I optionally require aws but it's not installed. "; return; }
    echo "Adding route53 entry to access Kubernetes ingress from the AWS ec2 instances."
    echo "IP/Host mapping: ${external_ip} ${loadBalancerHostName}"
    echo
    testgrid_hosted_zone_id=$(aws route53 list-hosted-zones --query "HostedZones[?Name=='wso2testgrid.com.'].Id" --output text)

    if [[ "$?" -ne 0 ]]; then
        echo
        echo "WARN: Failed to list hosted zones. Check whether you have enough AWS permissions. Route53 entry creation aborted."
        echo
        return;
    fi

    cat > route53-change-resource-record-sets.json << EOF
{
  "Comment": "testgrid job change batch req for mapping - ${external_ip} ${loadBalancerHostName}",
  "Changes": [
    {
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "mgt-${loadBalancerHostName}", "Type": "A", "TTL": 60,
        "ResourceRecords": [ { "Value": "${external_ip}" } ]
      }
    },
    {
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "gw-${loadBalancerHostName}", "Type": "A", "TTL": 60,
        "ResourceRecords": [ { "Value": "${external_ip}" } ]
      }
    },
    {
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "${loadBalancerHostName}", "Type": "A", "TTL": 60,
        "ResourceRecords": [ { "Value": "${external_ip}" } ]
      }
    }
  ]
}
EOF
cat route53-change-resource-record-sets.json

change_id=$(aws route53 change-resource-record-sets --hosted-zone-id ${testgrid_hosted_zone_id} \
    --change-batch file://route53-change-resource-record-sets.json \
    --query "ChangeInfo.Id" --output text)
aws route53 wait resource-record-sets-changed --id ${change_id}

echo "AWS Route53 DNS server configured to access the ingress IP  ${external_ip} via hostname ${loadBalancerHostName}"
echo
}

#DEBUG parameters: TODO: remove
TESTGRID_ENVIRONMENT=dev

if [ -z "$TestFileLocation" ]; then
    echo "Test Result location not set not changing deployment.yaml"
else
    echo "Test Result location set editing deployment.yaml"
    edit_deployments
fi
create_k8s_resources
add_route53_entry