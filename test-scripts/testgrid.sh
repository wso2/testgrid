#!/bin/bash

# Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#=== FUNCTION ==================================================================
# NAME: installTinkererAgent
# DESCRIPTION: Install TestGrid Tinkerer agent to AMI.
#===============================================================================
function installTinkererAgent() {
    #Download tinkererAgent from the last successful TestGrid build in WSO2 Jenkins.
    wget https://wso2.org/jenkins/job/testgrid/job/testgrid/lastSuccessfulBuild/artifact/remoting-agent/target/agent.zip
	sudo unzip agent.zip -d /opt/testgrid/
	rm agent.zip
	#Add testgrid-agent service
	sudo cp /opt/testgrid/agent/testgrid-agent /etc/init.d/
}

#=== FUNCTION ==================================================================
# NAME: installPerfMonitoringArtifacts
# DESCRIPTION: Install artifacts relates to performance monitoring to AMI.
#===============================================================================
function installPerfMonitoringArtifacts() {
    wget https://s3.amazonaws.com/testgrid-resources/packer/Unix/perf_monitoring_artifacts.zip
    unzip perf_monitoring_artifacts.zip -d .
    sudo cp -r perf_monitoring_artifacts/* /opt/testgrid/agent/
    sudo cp /opt/testgrid/agent/telegraf /bin/telegraf
}

mkdir -p /opt/testgrid/
sudo apt install unzip -y
sudo apt install yum -y

installTinkererAgent
installPerfMonitoringArtifacts
export TELEGRAF_CONFIG_PATH=/opt/testgrid/agent/telegraf.conf
echo 'TELEGRAF_CONFIG_PATH=/opt/testgrid/agent/telegraf.conf' >> /etc/environment