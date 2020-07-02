#!/bin/bash

set -o xtrace;

INPUT_DIR=$2

TEST_PROP_FILE=$INPUT_DIR/testplan-props.properties
INFRA_PROP_FILE=$INPUT_DIR/infrastructure.properties
GIT_USER=$(grep -m 1 -w "GIT_WUM_USERNAME" $TEST_PROP_FILE | cut -d'=' -f2)
GIT_PASS=$(grep -m 1 -w "GIT_WUM_PASSWORD" ${TEST_PROP_FILE} | cut -d'=' -f2)
keyFileLocation=$(grep -w "keyFileLocation" ${TEST_PROP_FILE} | cut -d'=' -f2)

OS=$(grep  -w "OS" ${INFRA_PROP_FILE} | cut -d'=' -f2)
WSO2InstanceName=$(grep -w "WSO2InstanceName" ${INFRA_PROP_FILE} | cut -d'=' -f2 | cut -d"/" -f3)

ANSIBLE_REPO=$(grep -w "ANSIBLE_GIT_URL" $TEST_PROP_FILE | cut -d'=' -f2 | cut -d'/' -f3-)
ANSIBLE_BRANCH=$(grep -w "ANSIBLE_GIT_BRANCH" $TEST_PROP_FILE | cut -d'=' -f2)
PRODUCT_NAME=$(grep -w "ProductName" ${TEST_PROP_FILE}| cut -d'=' -f2)
PRODUCT_VERSION=$(grep -w "ProductVersion" ${TEST_PROP_FILE}| cut -d'=' -f2)

LOCAL_DEPLOYMENT_SCRIPT=${INPUT_DIR}/../workspace/InfraRepository/jobs/intg-test-resources/latest/ansible-deployment/deploy.sh
REMOTE_DEPLOYMENT_SCRIPT=/opt/testgrid/workspace/deploy.sh

sed -i "s|ANSIBLE_REPO_URL|${ANSIBLE_REPO}|g" ${LOCAL_DEPLOYMENT_SCRIPT}
sed -i "s|ANSIBLE_REPO_BRANCH|${ANSIBLE_BRANCH}|g" ${LOCAL_DEPLOYMENT_SCRIPT}

if [ $OS = "Ubuntu" ]; then
    instanceUser="ubuntu"
fi

scp -o StrictHostKeyChecking=no -i ${keyFileLocation} ${LOCAL_DEPLOYMENT_SCRIPT} ${instanceUser}@${WSO2InstanceName}:${REMOTE_DEPLOYMENT_SCRIPT}
ssh -o StrictHostKeyChecking=no -i ${keyFileLocation} ${instanceUser}@${WSO2InstanceName} "cd /opt/testgrid/workspace && sudo bash ${REMOTE_DEPLOYMENT_SCRIPT} ${PRODUCT_NAME} ${PRODUCT_VERSION} ${GIT_USER} ${GIT_PASS}"
