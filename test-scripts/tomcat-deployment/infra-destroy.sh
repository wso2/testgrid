#!/usr/bin/env bash
# ----------------------------------------------------------------------------
#
# Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# This script is responsible for destroying the AWS instances and other AWS resources created

set -o xtrace

stackName=testgrid-tomcat-deployment
keyPairName=tgDummy

echo "Infrastructure Destroy Initiated"

# Deleting the created stack
aws cloudformation delete-stack --stack-name $stackName

# Deleting the created key pair
aws ec2 delete-key-pair --key-name $keyPairName

# TO-DO Hold the script till the instance is in terminated state
