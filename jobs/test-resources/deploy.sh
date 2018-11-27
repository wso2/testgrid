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

set -e
set -o xtrace

echo "Running deploy.sh..."
pwd

INPUT_FILE=$2/infrastructure.properties
OUTPUT_FILE=$4/deployment.properties

MGT_CONSOLE_PROP_VALUE=`grep -w "MgtConsoleUrl" ${INPUT_FILE} | tr -d '\' | cut -d'=' -f2`
host="$MGT_CONSOLE_PROP_VALUE/admin/login.jsp"


########################################
# Function to wait for server startup  #
########################################

echo "waiting for product in $host"
wait_for_server_startup() {
    max_attempts=100
    attempt_counter=0

    MGT_CONSOLE_URL=$host
    until $(curl -k --output /dev/null --silent --fail $MGT_CONSOLE_URL); do
       if [ ${attempt_counter} -eq ${max_attempts} ];then
        echo "Max attempts reached"
        exit 1
       fi
        printf '.'
        attempt_counter=$(($attempt_counter+1))
        sleep 10
    done
}

if [ ! -z "$MGT_CONSOLE_PROP_VALUE" ]
then
    CONNECT_RETRY_COUNT=20
    wait_for_server_startup
fi

cp $INPUT_FILE $OUTPUT_FILE
