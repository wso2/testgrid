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

function installJDKs() {

	#Installing ORACLE_JAVA8
	echo "Installing ORACLE_JAVA8"
	sudo add-apt-repository -y ppa:webupd8team/java
	sudo apt-get update -y
	echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
	echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
	sudo apt-get -y install oracle-java8-installer
	#Installing OpenJDK8
	echo "Installing OPENJDK 8"
	sudo add-apt-repository -y ppa:openjdk-r/ppa
	sudo apt-get update -y
	sudo apt-get -y install openjdk-8-jdk

    #Download JCE policies
	echo "Installing JCE policies (Presumed agreement on Oracle license at https://www.oracle.com/technetwork/java/javase/terms/license/index.html)"
	installPackage wget
	wget -v --header "Cookie: oraclelicense=oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip
	unzip jce_policy-8.zip -d .
	sudo mv UnlimitedJCEPolicyJDK8/local_policy.jar /usr/java/jdk1.8.0_181-amd64/jre/lib/security/
	sudo mv UnlimitedJCEPolicyJDK8/US_export_policy.jar /usr/java/jdk1.8.0_181-amd64/jre/lib/security/
}

mkdir -p /opt/testgrid/
sudo apt install unzip -y
sudo apt install yum -y

installTinkererAgent
installPerfMonitoringArtifacts
installJDKs

export TELEGRAF_CONFIG_PATH=/opt/testgrid/agent/telegraf.conf
echo 'TELEGRAF_CONFIG_PATH=/opt/testgrid/agent/telegraf.conf' >> /etc/environment

JAVA_HOME='/usr/java/jdk1.8.0_181-amd64'
echo "JAVA_HOME=$JAVA_HOME" >> /etc/environment
source /etc/environment
echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile
source /etc/profile
echo PATH=$JAVA_HOME/bin:$PATH >> /etc/environment
source /etc/environment