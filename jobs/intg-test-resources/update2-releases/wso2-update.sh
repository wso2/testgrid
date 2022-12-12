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

CFN_PROP_FILE=/opt/testgrid/workspace/cfn-props.properties
WSO2_PRODUCT=$(grep -w "REMOTE_PACK_NAME" ${CFN_PROP_FILE} | cut -d'=' -f2)
PRODUCT_NAME=$(echo $WSO2_PRODUCT | rev | cut -d"-" -f2-  | rev)
PRODUCT_VERSION=$(echo $WSO2_PRODUCT | rev | cut -d"-" -f1  | rev)

rm $WSO2_PRODUCT.zip

echo "Getting last successful build ID for APIM"
BUILD_ID=$(wget -qO- https://wso2.org/jenkins/view/products/job/products/job/product-apim/lastSuccessfulBuild/buildNumber)
echo "Last successful build ID is $BUILD_ID"

echo "Downloading APIM pack $WSO2_PRODUCT"
wget -q "https://wso2.org/jenkins/view/products/job/products/job/product-apim/${BUILD_ID}/org.wso2.am%24wso2am/artifact/org.wso2.am/wso2am/${PRODUCT_VERSION}/${WSO2_PRODUCT}.zip"

echo "Unzipping $WSO2_PRODUCT Pack."
unzip -o -q $WSO2_PRODUCT.zip
