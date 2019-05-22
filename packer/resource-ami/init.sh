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

ubuntu_os_version="16.04"
ubuntu_ssh_username="ubuntu"
ubuntu_source_ami_filter_name="ubuntu/images/*ubuntu-xenial-16.04-amd64-server-*"
ubuntu_source_ami_filter_owner="099720109477"

centos_os_version="7"
centos_ssh_username="centos"
centos_source_ami_filter_name="CentOS Linux 7 x86_64 HVM EBS ENA*"
centos_source_ami_filter_owner="679593333241"

rhel_os_version="7.4"
rhel_ssh_username="ec2-user"
rhel_source_ami_filter_name="RHEL-7.4_HVM_GA-20170724-x86_64-1-Hourly2-GP2"
rhel_source_ami_filter_owner="309956199498"

packer_file="packer-conf.json"

#=== FUNCTION ==================================================================
# NAME: startPacker
# DESCRIPTION: Validate config json and start packer.
#===============================================================================
function startPacker() {
    echo "Starting packer.."
	packer validate packer-conf.json
	packer build packer-conf.json
}

#=== FUNCTION ==================================================================
# NAME: checkIfExists
# DESCRIPTION: Check if the provided file exists.
# PARAMETER 1: File to check
#===============================================================================
function checkIfExists() {
	file=$1
	if [ -f "$file" ]
	then
		echo "$file found."
		return 0
	else
		echo "$file not found."
		return 1
	fi
}

#=== FUNCTION ==================================================================
# NAME: checkResources
# DESCRIPTION: Check if the resources given in resource.txt exists.
#===============================================================================
function checkResources() {
	notFound=0
	while read F  ; do
	    checkIfExists resources/$F
		boolExist=$?
		if [ "$boolExist" == 1 ]
		then
			notFound=1
		fi
	done <resources/resources.txt
	if [ "$notFound" = "1" ]
	then
		echo "Resources are missing.. Can not continue."
		exit;
	else
		creator_ip=$(ip route get 1.1.1.1 | awk '{print $NF; exit}')
		export PACKER_AMI_CREATOR_IP=$creator_ip
		startPacker;
	fi
}

#=== FUNCTION ==================================================================
# NAME: downloadResourcesFromS3
# DESCRIPTION: Download relevant artifacts from TestGrid S3 bucket.
# The function will download Unix resources and OS-specific resources.
#===============================================================================
function downloadResourcesFromS3() {
	echo "Downloading $os AMI resources from S3 (testgrid-resources/packer directory)"
	aws s3 sync s3://testgrid-resources/packer/Common resources/
	aws s3 sync s3://testgrid-resources/packer/Unix resources/
	aws s3 sync s3://testgrid-resources/packer/$os resources/
}

#Check if Packer, AWS CLI exists
command -v packer >/dev/null 2>&1 || { echo >&2 "Packer was not found. Please install Packer first."; exit 1; }
command -v aws >/dev/null 2>&1 || { echo >&2 "AWS CLI was not found. Please install AWS CLI first."; exit 1; }

echo "Select the OS of the AMI:"
echo "        1 - Ubuntu $ubuntu_os_version"
echo "        2 - CentOS $centos_os_version"
echo "        3 - Windows 2016"
echo "        4 - RHEL $rhel_os_version"
read os;
case "$os" in
    1)
	os="Ubuntu"
	export PACKER_SSH_USERNAME=$ubuntu_ssh_username
	export PACKER_SOURCE_OS=$os
	export PACKER_SOURCE_OS_VERSION=$ubuntu_os_version
	export PACKER_SOURCE_AMI_FILTER_NAME=$ubuntu_source_ami_filter_name
	export PACKER_SOURCE_AMI_FILTER_OWNER=$ubuntu_source_ami_filter_owner
	downloadResourcesFromS3
	checkResources
        ;;
    2)
	os="CentOS"
	export PACKER_SSH_USERNAME=$centos_ssh_username
	export PACKER_SOURCE_OS=$os
	export PACKER_SOURCE_OS_VERSION=$centos_os_version
    	export PACKER_SOURCE_AMI_FILTER_NAME=$centos_source_ami_filter_name
	export PACKER_SOURCE_AMI_FILTER_OWNER=$centos_source_ami_filter_owner
	downloadResourcesFromS3
	checkResources
        ;;
    3)
	os="Windows"
	echo "Not implemented yet."
        ;;
    4)
	os="RHEL"
	export PACKER_SSH_USERNAME=$rhel_ssh_username
	export PACKER_SOURCE_OS=$os
	export PACKER_SOURCE_OS_VERSION=$rhel_os_version
    	export PACKER_SOURCE_AMI_FILTER_NAME=$rhel_source_ami_filter_name
	export PACKER_SOURCE_AMI_FILTER_OWNER=$rhel_source_ami_filter_owner
	downloadResourcesFromS3
	checkResources
        ;;
    *)
        echo "$os is not a valid choice. Please enter the number"
        ;;
esac
