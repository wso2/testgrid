#!/bin/bash
#
# ----------------------------------------------------------------------------
# Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# TODO: Ideally, we should parse the input arguments via getopts.

echo "wsEndpoint=$1" > /opt/testgrid/agent-config.properties
echo "region=$2" >> /opt/testgrid/agent-config.properties
echo "testPlanId=$3" >> /opt/testgrid/agent-config.properties
echo "provider=$4" >> /opt/testgrid/agent-config.properties
echo "userName=$5" >> /opt/testgrid/agent-config.properties
echo "password=$6" >> /opt/testgrid/agent-config.properties
echo "instanceId=$(wget -qO- http://169.254.169.254/latest/meta-data/instance-id)" >> /opt/testgrid/agent-config.properties
localIp=$(wget -qO- http://169.254.169.254/latest/meta-data/public-ipv4)
if [[ -z $localIp ]]; then
   localIp=$(wget -qO- http://169.254.169.254/latest/meta-data/local-ipv4)
fi
echo "instanceIP=$localIp" >> /opt/testgrid/agent-config.properties
service testgrid-agent restart