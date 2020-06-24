#!/bin/bash

set -o xtrace

INPUTS_DIR=$2

# SCRIPT_LOCATION=""
SCRIPT_NAME="test.sh"
PROP_FILE="${INPUTS_DIR}/deployment.properties"
PRODUCT_GIT_URL=$(grep -w "PRODUCT_GIT_URL" ${PROP_FILE} | cut -d'=' -f2 | cut -d'/' -f3-)
PRODUCT_GIT_BRANCH=$(grep -w "PRODUCT_GIT_BRANCH" ${PROP_FILE} | cut -d'=' -f2)
keyFileLocation=$(grep -w "keyFileLocation" ${PROP_FILE} | cut -d'=' -f2)
WSO2InstanceName=$(grep -w "WSO2InstanceName" ${PROP_FILE} | cut -d'=' -f2 | cut -d"/" -f3)
PRODUCT_NAME=$(grep -w "WSO2_PRODUCT" ${PROP_FILE}| cut -d'=' -f2 | cut -d'-' -f1)
PRODUCRT_VERSION=$(grep -w "WSO2_PRODUCT" ${PROP_FILE}| cut -d'=' -f2 | cut -d'-' -f2)

NEXUS_SCRIPT_NAME="uat-nexus-settings.xml"
NEXUS_SCRIPT_PATH='/testgrid/testgrid-home/jobs/wso2am-2.1.0-int-test'

GIT_USER=$(grep -w "VIM_GIT_USERNAME" ${PROP_FILE} | cut -d'=' -f2)
GIT_PASS=$(grep -w "VIM_GIT_TOKEN" ${PROP_FILE} | cut -d'=' -f2)

function log_info(){
    echo "[INFO][$(date '+%Y-%m-%d %H:%M:%S')]: $1"
}

log_info "Copying ${SCRIPT_NAME} to remote ec2 instance"

scp -o StrictHostKeyChecking=no -i ${keyFileLocation} ${SCRIPT_NAME} ubuntu@${WSO2InstanceName}:/opt/testgrid/workspace/${SCRIPT_NAME}
scp -o StrictHostKeyChecking=no -i ${keyFileLocation} ${NEXUS_SCRIPT_PATH}/${NEXUS_SCRIPT_NAME} ubuntu@${WSO2InstanceName}:/opt/testgrid/workspace/${NEXUS_SCRIPT_NAME}
ssh -o StrictHostKeyChecking=no -i ${keyFileLocation} ubuntu@${WSO2InstanceName} "cd /opt/testgrid/workspace && sudo bash ${SCRIPT_NAME} ${PRODUCT_GIT_URL} ${PRODUCT_GIT_BRANCH} ${PRODUCT_NAME} ${PRODUCRT_VERSION} ${GIT_USER} ${GIT_PASS}"
ssh -o StrictHostKeyChecking=no -i ${keyFileLocation} ubuntu@${WSO2InstanceName} "ls /opt/testgrid/workspace"
