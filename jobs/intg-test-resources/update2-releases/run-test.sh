#----------------------------------------------------------------------------
#  Copyright (c) 2021 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#----------------------------------------------------------------------------
#!/bin/bash

set -o xtrace; set -e

INPUTS_DIR=$2
OUTPUTS_DIR=$4
CONFIG_PROP_FILE="${TESTGRID_HOME}/config.properties"
PROP_FILE="${INPUTS_DIR}/deployment.properties"
PRODUCT_GIT_URL=$(grep -w "PRODUCT_GIT_URL" ${PROP_FILE} | cut -d'=' -f2 | cut -d'/' -f3-)
PRODUCT_GIT_BRANCH=$(grep -w "PRODUCT_GIT_BRANCH" ${PROP_FILE} | cut -d'=' -f2)
PRODUCT_GIT_REPO_NAME=$(grep -w "PRODUCT_GIT_URL" ${PROP_FILE} | rev | cut -d'/' -f1 | rev | cut -d'.' -f1)
keyFileLocation=$(grep -w "keyFileLocation" ${PROP_FILE} | cut -d'=' -f2)
WSO2InstanceName=$(grep -w "WSO2InstanceName" ${PROP_FILE} | cut -d'=' -f2 | cut -d"/" -f3)
OperatingSystem=$(grep -w "OS" ${PROP_FILE} | cut -d'=' -f2)
PRODUCT_NAME=$(grep -w "WSO2_PRODUCT" ${PROP_FILE}| cut -d'=' -f2 | cut -d'-' -f1)
PRODUCT_VERSION=$(grep -w "WSO2_PRODUCT" ${PROP_FILE}| cut -d'=' -f2 | cut -d'-' -f2)
TEST_REPORTS_DIR="$(grep -w "surefire_report_dir" ${PROP_FILE} | cut -d'=' -f2 )"
WUM_USERNAME=$(grep -w "WUM_USERNAME" ${CONFIG_PROP_FILE} | cut -d'=' -f2)
WUM_PASSWORD=$(grep -w "WUM_PASSWORD" ${CONFIG_PROP_FILE} | cut -d'=' -f2)
SCRIPT_LOCATION=$(grep -w "TEST_SCRIPT_URL" ${PROP_FILE} | cut -d'=' -f2)
TEST_SCRIPT_NAME=$(echo $SCRIPT_LOCATION | rev | cut -d'/' -f1 | rev)
NEXUS_SCRIPT_NAME="uat-nexus-settings.xml"
NEXUS_SCRIPT_PATH="/testgrid/testgrid-home/jobs/u2-intg-test-${PRODUCT_NAME}-${PRODUCT_VERSION}-full"
INFRA_JSON=$INPUTS_DIR/../workspace/InfraRepository/jobs/intg-test-resources/infra.json
GIT_USER=$(grep -w "GIT_WUM_USERNAME" ${PROP_FILE} | cut -d'=' -f2)
GIT_PASS=$(grep -w "GIT_WUM_PASSWORD" ${PROP_FILE} | cut -d'=' -f2)
TEST_MODE=$(grep -w "TEST_MODE" ${PROP_FILE} | cut -d'=' -f2)
function log_info(){
    echo "[INFO][$(date '+%Y-%m-%d %H:%M:%S')]: $1"
}

wget -q ${SCRIPT_LOCATION}

log_info "Copying ${TEST_SCRIPT_NAME} to remote ec2 instance"

if [ ${OperatingSystem} = "Ubuntu" ]; then
    instanceUser="ubuntu"
elif [ ${OperatingSystem} = "CentOS" ]; then
    instanceUser="centos"
else
    instanceUser="ec2-user"
fi

scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ${keyFileLocation} ${TEST_SCRIPT_NAME} $instanceUser@${WSO2InstanceName}:/opt/testgrid/workspace/${TEST_SCRIPT_NAME}
scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ${keyFileLocation} ${NEXUS_SCRIPT_PATH}/${NEXUS_SCRIPT_NAME} $instanceUser@${WSO2InstanceName}:/opt/testgrid/workspace/${NEXUS_SCRIPT_NAME}
scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ${keyFileLocation} ${INFRA_JSON} $instanceUser@${WSO2InstanceName}:/opt/testgrid/workspace/infra.json
ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ${keyFileLocation} $instanceUser@${WSO2InstanceName} "cd /opt/testgrid/workspace && sudo bash /opt/testgrid/workspace/wso2-update.sh" "'$WUM_USERNAME'" "'$WUM_PASSWORD'"
ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ${keyFileLocation} $instanceUser@${WSO2InstanceName} "cd /opt/testgrid/workspace && sudo bash /opt/testgrid/workspace/provision_db_${PRODUCT_NAME}.sh"
ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ${keyFileLocation} $instanceUser@${WSO2InstanceName} "cd /opt/testgrid/workspace && sudo bash ${TEST_SCRIPT_NAME} ${PRODUCT_GIT_URL} ${PRODUCT_GIT_BRANCH} ${PRODUCT_NAME} ${PRODUCT_VERSION} ${GIT_USER} ${GIT_PASS} ${TEST_MODE}"
mkdir -p ${OUTPUTS_DIR}/scenarios/integration-tests
scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ${keyFileLocation}  -r ${instanceUser}@${WSO2InstanceName}:/opt/testgrid/workspace/${PRODUCT_GIT_REPO_NAME}/${TEST_REPORTS_DIR}/surefire-reports ${OUTPUTS_DIR}/scenarios/integration-tests/.