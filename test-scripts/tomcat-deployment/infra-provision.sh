#!/usr/bin/env bash
# ----------------------------------------------------------------------------
#
# Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# ----------------------------------------------------------------------------

stackName=TGdummy
keyPairName=tgDummy
amiId=ami-4524733f
outputFileName=infra_eps

## This script will create the CF infrastructure
echo "Infrastructure creation Initiated"

# Create a New Keypair in AWS space and saving it to a .pem file
output=$(aws ec2 create-key-pair --key-name $keyPairName --output json --query 'KeyMaterial' | cut -d '"' -f2)
#aws ec2 create-key-pair --key-name tgDummy --output json | jq -r ".KeyMaterial" > key.pem
echo -e $output > key.pem

# Changing the permission of the key
chmod 600 key.pem

# Create the Stack || --parameters ParameterKey=am-id,ParameterValue=ami-08c64c72
aws cloudformation create-stack --stack-name $stackName --template-body file://./cf-templates/add-instances.yaml --parameters ParameterKey=AMIID,ParameterValue=$amiId

# Waiting till the stack is created
aws cloudformation wait stack-create-complete --stack-name $stackName


# Write the public IP of the instance to a file
# This will only work if you have a single output. May not work for other cases
aws cloudformation describe-stacks --stack-name $stackName | grep -o -P '(?<=OutputValue": ").*(?=")' > $outputFileName


