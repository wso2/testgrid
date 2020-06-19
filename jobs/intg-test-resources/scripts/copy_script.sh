#!/bin/bash

set -o xtrace

INPUTS_DIR=$2

# SCRIPT_LOCATION=""
SCRIPT_NAME="test.sh"
PROP_FILE="${INPUTS_DIR}/deployment.properties"
PRODUCT_GIT_URL=$(grep -w "PRODUCT_GIT_URL" ${PROP_FILE} | cut -d'=' -f2)
PRODUCT_GIT_BRANCH=$(grep -w "PRODUCT_GIT_BRANCH" ${PROP_FILE} | cut -d'=' -f2)
keyFileLocation=$(grep -w "keyFileLocation" ${PROP_FILE} | cut -d'=' -f2)
WSO2InstanceName=$(grep -w "WSO2InstanceName" ${PROP_FILE} | cut -d'=' -f2 | cut -d"/" -f3)

function log_info(){
    echo "[INFO][$(date '+%Y-%m-%d %H:%M:%S')]: $1"
}

log_info "Copying ${SCRIPT_NAME} to remote ec2 instance"

scp -i ${keyFileLocation} ${SCRIPT_NAME} ubuntu@${WSO2InstanceName}:/opt/testgrid/workspace/${SCRIPT_NAME}
ssh -i ${keyFileLocation} ubuntu@${WSO2InstanceName} "cd /opt/testgrid/workspace && bash ${SCRIPT_NAME} ${PRODUCT_GIT_URL} ${PRODUCT_GIT_BRANCH}"
ssh -i ${keyFileLocation} ubuntu@${WSO2InstanceName} "ls /opt/testgrid/workspace"
ssh -i ${keyFileLocation} ubuntu@${WSO2InstanceName} "cd /opt/testgrid/workspace/product-apim && git branch"

sleep 3600

