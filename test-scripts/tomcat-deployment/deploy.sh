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

# This script is responsible for deploying the Tomcat server on AWS instances

# For the deployment script to run infra_eps and key.pem files should exist

infraEPFileName=infra_eps
outputFileName=deployment_eps

#Extracting the IP of the tomcat server
endpoint=$(cat $infraEPFileName)
echo "The Tomcat Endpoint is set to : " $endpoint

# Populate the ansible playbook host file
echo -e "[tomcat-servers:vars]\nansible_ssh_private_key_file=./key.pem\n\n[tomcat-servers]\n$endpoint" > ansible/hosts

# Executing ansible
echo "Ansible execution started"
export ANSIBLE_HOST_KEY_CHECKING=False
ansible-playbook -i ./ansible/hosts ./ansible/site.yaml

# Writing the deployment endpoints to a file
echo "tomcat_host=http://$endpoint:8080" > $outputFileName
