#!/usr/bin/env bash
#===================================================================================
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
#
# FILE: ec2-slave-setup.sh
#
# USAGE: ec2-slave-setup.sh [Master_IP]
#
# DESCRIPTION: This script is used to setup the Jenkins slave instance to be
#              operational. This is triggered from the slave setup plugin.
#
#===================================================================================
set -e
set -o xtrace

TG_MASTER_IP="$1"
MYSQL_DRIVER_LOCATION='https://repo1.maven.org/maven2/mysql/mysql-connector-java/6.0.6/mysql-connector-java-6.0.6.jar'

MASTER_KEY_LOCATION='/testgrid/testgrid-prod-key.pem'
TESTGRID_HOME='/testgrid/testgrid-home'

# Create TG Home directory
mkdir -p /testgrid/testgrid-home/testgrid-dist
cd /testgrid/testgrid-home/testgrid-dist

# Clear content if there is anything
rm -rf *

# Downloading the TG distribution
echo "Downloading the TG Distribution!"
echo "Download Location ${TG_MASTER_IP}:/testgrid/testgrid-home/testgrid-dist/WSO2-TestGrid.zip"
scp -i ${MASTER_KEY_LOCATION} -o StrictHostKeyChecking=no ubuntu@${TG_MASTER_IP}:${TESTGRID_HOME}/testgrid-dist/WSO2-TestGrid.zip ${TESTGRID_HOME}/testgrid-dist/
echo "Copying config.properties from master!"
scp -i ${MASTER_KEY_LOCATION} -o StrictHostKeyChecking=no ubuntu@${TG_MASTER_IP}:${TESTGRID_HOME}/config.properties ${TESTGRID_HOME}
echo "Copying JMETER from master!"
scp -qi ${MASTER_KEY_LOCATION} -r -o StrictHostKeyChecking=no ubuntu@${TG_MASTER_IP}:/testgrid/apache-jmeter-3.3 /testgrid
echo "set JMETER_HOME"
export JMETER_HOME=/testgrid/apache-jmeter-3.3
echo 'JMETER_HOME="/testgrid/apache-jmeter-3.3"' >> /etc/environment

echo "Unzip Tesgrid distribution and copy mysql jar"
unzip WSO2-TestGrid.zip
curl -o ./WSO2-TestGrid/lib/mysql.jar ${MYSQL_DRIVER_LOCATION}

# Starting a virtual display to support FX chart generation
echo "Starting Xvfb virtual display for chart generation"
export DISPLAY=:95
nohup Xvfb :95 -screen 0 1024x768x16 > /dev/null 2>&1 &

# Changing the testgrid home ownership
chown -R ubuntu:ubuntu ${TESTGRID_HOME}

rm ${MASTER_KEY_LOCATION}

# Add EI backends
echo "172.31.86.212      ei-backend.scenarios.wso2.org" >> /etc/hosts

# Install GCloud
#export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"
#echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" \
#| sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
#curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
#sudo apt-get update && sudo apt-get install google-cloud-sdk
# copy gke-developer json pvt key
# gcloud auth activate-service-account --key-file <path-to-key>.json
# generate kubeconfig:
# gcloud container clusters get-credentials <cluster-name> --zone us-central1-a --project testgrid
# gcloud version
# install kubectl && kubectl version

#sudo apt install xvfb
#mkdir -p /testgrid/software/java/jdk1.8.0_161
#sudo mkdir -p /testgrid/software/java/jdk1.8.0_161
#sudo chown -R ubuntu:ubuntu /testgrid
#mv jdk-8u161-linux-x64.tar.gz /testgrid/software/java/
#sudo apt-get install unzip
#cd /testgrid/software/java/


# Current .bashrc content
#export PATH="$PATH:/opt/mssql-tools/bin"
#Xvfb :95 -screen 0 1024x768x16 &> xvfb.log &
## The next line updates PATH for the Google Cloud SDK.
#if [ -f '/testgrid/software/gcloud/google-cloud-sdk/path.bash.inc' ]; then . '/testgrid/software/gcloud/google-cloud-sdk/path.bash.inc'; fi
## The next line enables shell command completion for gcloud.
#if [ -f '/testgrid/software/gcloud/google-cloud-sdk/completion.bash.inc' ]; then . '/testgrid/software/gcloud/google-cloud-sdk/completion.bash.inc'; fi
