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
alias wget='wget -q'
alias unzip='unzip -q'

#
#This class is used for the deployment of resources into the namespace using helm
#The created resources will be exposed using an Ingress to the external usage
#

function edit_deployments() {
  details=$(groovy kubedeployment_editor.groovy \
  "${infra_props["depRepoLocation"]}/testgrid-sidecar/deployment/logpath-details.yaml" \
  "${OUTPUT_DIR}/infrastructure.properties" helm)
  read -ra detailsArr <<< $details
  sidecarReq=${detailsArr[0]}
  filename=${detailsArr[1]}
  if [[ "$sidecarReq" == "SidecarReq" ]] || [[ "$sidecarReq" == "onlyEnvVars" ]]
  then
    kubectl label namespace ${namespace} namespace=${namespace}
    kubectl label namespace ${namespace} sidecar-injector=enabled
    chmod 777 ./testgrid-sidecar/createSidecar.sh
    ./testgrid-sidecar/createSidecar.sh ${namespace} ${sidecarReq} ${filename} ${INPUT_DIR}
    if [ $? -eq 0 ]
    then
      echo "[ERROR] Could not create the Sidecar Deployment"
      exit 0
    else
      echo "[INFO] Created Mutating Webhook Deployment"
      exit 1
    fi
  fi
}

function create_k8s_resources() {

    if [ -z ${exposedDeployments} ]
    then
      echo "[WARN] Could not find inputParameter 'exposedDeployments' in deploymentConfig. No deployments will be
      exposed."
    fi

    if [ -z ${helmDeployScript} ]; then
      echo "[ERROR] Could not find inputParameter 'helmDeployScript' in deploymentConfig. Nothing to do. Exiting..."
      exit 1
    fi

    #deploy the helm configurations into the kubernetes cluster
    source $helmDeployScript
    readiness_deployments

    if [[ -z ${loadBalancerHostName} ]]; then
        echo WARN: loadBalancerHostName not found in deployment.properties. Generating a random name under \
        *.gke.wso2testgrid.com CN
        loadBalancerHostName=wso2am-$(($RANDOM % 10000)).gke.wso2testgrid.com # randomized hostname
    else
        echo DEBUG: loadBalanceHostName: ${loadBalancerHostName}
    fi

    readiness_deployments
    sleep 10

# TODO: install ingress-nginx controller if not found.

# Create a ingress for the services we want to expose to public internet.
tlskeySecret=testgrid-certs
ingressName=tg-ingress
kubectl create secret tls ${tlskeySecret} \
    --cert ${INPUT_DIR}/testgrid-certs-v2.crt  \
    --key ${INPUT_DIR}/testgrid-certs-v2.key -n ${namespace}

echo "public key to access the endpoints using the Ingress is available in $OUTPUT_DIR/deployment.properties"

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
      kubectl expose deployment ${dep[$i]} --name=${dep[$i]} -n ${namespace}
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

    echo "namespace=$namespace" >> ${OUTPUT_DIR}/deployment.properties
    echo "loadBalancerHostName=$loadBalancerHostName" >> ${OUTPUT_DIR}/deployment.properties
}

#This function constantly check whether the deployments are correctly deployed in the cluster
function readiness_deployments(){
    start=`date +%s`
    i=0;
    # todo add a terminal condition/timeout.
    TIMEOUT=600 # 10mins
    for ((i=0; i<$dep_num; i++)) ; do
        total_count=$((TIMEOUT/5))
        echo $total_count
        count=0
        echo Running kubectl get deployments -n $namespace ${dep[$i]} -o jsonpath='{.status.conditions[?(@.type=="Available")].status}'
        until [[ $count -ge $total_count ]]
        do
            deployment_status=$(kubectl get deployments -n $namespace ${dep[$i]} -o jsonpath='{.status.conditions[?(@.type=="Available")].status}')
            [[ "$deployment_status" == "True" ]] && break
            count=$(($count+1))
            sleep 5;
        done
        [[ "$deployment_status" != "True" ]] && echo "[ERROR] timeout while waiting for deployment, '${dep[$i]}', in \
        namespace, '$namespace', to succeed." && exit 78

    done

    end=`date +%s`
    runtime=$((end-start))
    echo "Deployment \"${dep}\" got ready in ${runtime} seconds."
    echo
}

#This function constantly check whether the deployments are correctly deployed in the cluster
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
    echo "loadBalancerHostName=${loadBalancerHostName}" >> ${OUTPUT_DIR}/deployment.properties
    echo "loadBalancerIP=${external_ip}" >> ${OUTPUT_DIR}/deployment.properties
    done

    end=`date +%s`
    runtime=$((end-start))
    echo "Kubernetes Ingress service '${ingressName}' got ready in ${runtime} seconds."

}

#This function is used to add paths to etc/host files
function addhost() {
    ip_address=$1
    hostname=$2
    host_line="$ip_address\t$hostname"
    if [ -n "$(grep $hostname /etc/hosts)" ]
        then
            echo "[INFO] $hostname already exists : $(grep $hostname /etc/hosts)"
        else
            echo "[INFO] Adding $hostname to your /etc/hosts";
            echo $testgrid_pass | sudo -S -- sh -c -e "echo '$HOSTS_LINE' >> /etc/hosts";

            if [ -n "$(grep $hostname /etc/hosts)" ]
                then
                    echo "[INFO] $hostname was added succesfully \n $(grep $hostname /etc/hosts)";
                else
                    echo "[ERROR] Failed to Add $hostname, Try again!";
            fi
    fi
}

#This function is used to direct accesss to the Ingress created from the AWS ec2 instances.
#Host mapping service provided by AWS, route53 is used for this purpose.
function add_route53_entry() {
    if [ -z "$testgrid_env" ]; then
      env='dev'
    else
      env=${testgrid_env}
    fi
    if [[ "${env}" != "dev" ]] && [[ "${env}" != 'prod' ]]; then
        echo "Not configuring route53 DNS entries since the environment is not dev/prod. You need to manually add
        '${external_ip} ${loadBalancerHostName}' into your /etc/hosts."
        addhost "${external_ip}" "${loadBalancerHostName}"
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




function install_helm(){
    #if helm is not installed in the cluster, helm will be installed.
    if command -v helm && helm version --template "{{.Client.SemVer}}" | grep "v3\.[0-9]*\.[0-9]*"; then
        helm version
        return;
    fi
    wget -q https://get.helm.sh/helm-v3.0.0-alpha.1-linux-amd64.tar.gz
    tar -zxvf helm-v3.0.0-alpha.1-linux-amd64.tar.gz
    [[ -f ~/.local/bin/ ]] && mkdir ~/.local/bin/mkdir ~/.local/bin/
    PATH=~/.local/bin/:$PATH
    mv linux-amd64/helm ~/.local/bin/helm
    ~/.local/bin/helm init

    echo "Helm version:" `helm version --short`
}

# Read a property file to a given associative array
#
# $1 - Property file
# $2 - associative array
# How to call
# declare -A somearray
# read_property_file testplan-props.properties somearray
read_property_file() {
    local property_file_path=$1
    if [[ ! -f $1 ]]; then
        echo "[WARN] property file not found: $property_file_path"
        return;
    fi
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

write_to_dep_props() {
  echo namespace=${namespace} >> "${OUTPUT_DIR}/deployment.properties"
}

echo "Starting helm kubernetes artifact deployment.."
dryRun=False

OUTPUT_DIR=$4
INPUT_DIR=$2
declare -g -A infra_props
declare -g -A deploy_props
read_property_file "${INPUT_DIR}/infrastructure.properties" infra_props
read_property_file "${OUTPUT_DIR}/deployment.properties" deploy_props
#source $INPUT_DIR/infrastructure.properties
#source $OUTPUT_DIR/deployment.properties

deploymentYamlFiles=(${infra_props["deploymentYamlFiles"]})
no_yamls=${#deploymentYamlFiles[@]}
exposedDeployments=${infra_props["exposedDeployments"]}
helmDeployScript=${infra_props["helmDeployScript"]}
dep=(${infra_props["exposedDeployments"]})
dep_num=${#dep[@]}
namespace=${infra_props["namespace"]}
yamlFilesLocation=${infra_props["yamlFilesLocation"]}
deploymentRepositoryLocation=${infra_props["deploymentRepositoryLocation"]}
loadBalancerHostName=${deploy_props["loadBalancerHostName"]}
logOptions=${infra_props["logOptions"]}

testgrid_env=${infra_props["environment"]}
testgrid_pass=${infra_props["password"]}

if [ -z "$logOptions" ]; then
    echo "No Logging capabilities are set"
else
    edit_deployments
    echo
fi

create_k8s_resources
add_route53_entry
write_to_dep_props
