#!/bin/bash
# ----------------------------------------------------------------------------
#
# Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
#
# WSO2 LLC. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# ----
CFN_PROP_FILE=/opt/testgrid/workspace/cfn-props.properties
WSO2_USERNAME=$1
WSO2_PASSWORD=$2
WSO2_PRODUCT=$(grep -w "REMOTE_PACK_NAME" ${CFN_PROP_FILE} | cut -d'=' -f2)
echo "Unzipping $WSO2_PRODUCT Pack."
unzip -o -q $WSO2_PRODUCT.zip && cd $WSO2_PRODUCT/bin
PRODUCT_NAME=$(echo $WSO2_PRODUCT | rev | cut -d"-" -f2-  | rev)
PRODUCT_VERSION=$(echo $WSO2_PRODUCT | rev | cut -d"-" -f1  | rev)

if [ -z "${WSO2_USERNAME}" ] && [ -z "${WSO2_PASSWORD}" ]; then
  echo "WSO2 Credentials are empty. Proceeding with ${WSO2_PRODUCT} vanilla pack."
  exit 0
else
  # Note: config.json will be replaced with UAT information through cloudformation.
  sudo chmod 755 wso2update_linux
  sudo ./wso2update_linux check --username "'$WSO2_USERNAME'" --password "$WSO2_PASSWORD" -v
  export WSO2_UPDATES_UPDATE_LEVEL_STATE=VERIFYING
  sudo -E ./wso2update_linux --username "$WSO2_USERNAME" --password "$WSO2_PASSWORD" --backup /opt/testgrid/workspace/backup -v
  update_exit_code=$(echo $?)

  if [ $update_exit_code -eq 2 ]; then
    echo "Self Update."
    sudo -E ./wso2update_linux --username "$WSO2_USERNAME" --password "$WSO2_PASSWORD" --backup /opt/testgrid/workspace/backup -v
    update_exit_code=$(echo $?)
  fi

  if [ $update_exit_code -eq 0 ]; then
    echo "Successfully updated."
    echo `pwd`
    cd ../../
    ls
    rm -rf $WSO2_PRODUCT.zip
    ls
    # Check if the directory exists and is not empty
    if [ -d "$WSO2_PRODUCT" ] && [ "$(ls -A $WSO2_PRODUCT)" ]; then
      zip -r -q $WSO2_PRODUCT.zip $WSO2_PRODUCT
      echo "Successfully zipped $WSO2_PRODUCT."
    else
      echo "Error: Directory $WSO2_PRODUCT is missing or empty. Skipping zipping process."
      exit 1
    fi
    exit 0
  elif [ $update_exit_code -eq 1 ]; then
    echo "Default error."
  elif [ $update_exit_code -eq 3 ]; then
    echo "Conflict(s) encountered."
  elif [ $update_exit_code -eq 4 ]; then
    echo "Reverted."
  else
    echo "Unkown exit code from update tool."
  fi
  exit 1
fi
