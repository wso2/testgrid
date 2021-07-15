#!/bin/bash

# ----------------------------------------------------------------------------
#
# Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
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
readonly WSO2_USERNAME=$1
readonly WSO2_PASSWORD=$2
readonly WSO2_PRODUCT=$3
readonly 
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
  sudo ./wso2update_linux check --username "'$WSO2_USERNAME'" --password "$WSO2_PASSWORD"
  sed "s/PATTERN/$WSO2_PRODUCT/" /opt/testgrid/workspace/uat-config.json  | sed "s/PRODUCT_NAME/$PRODUCT_NAME/" | sed "s/PRODUCT_VERSION/$PRODUCT_VERSION/" > ../updates/config.json
  sudo ./wso2update_linux --username $WSO2_USERNAME --password $WSO2_PASSWORD --backup ../../backup
  update_exit_code=$(echo $?)

  if [ $update_exit_code -eq 2 ]; then
    echo "Self Update."
    sudo ./wso2update_linux --username $WSO2_USERNAME --password $WSO2_PASSWORD --backup ../../backup
    update_exit_code=$(echo $?)
  fi

  if [ $update_exit_code -eq 0 ]; then
    echo "Successfully updated."
    cd ../../
    rm -rf $WSO2_PRODUCT.zip
    zip -r -q $WSO2_PRODUCT.zip $WSO2_PRODUCT
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
