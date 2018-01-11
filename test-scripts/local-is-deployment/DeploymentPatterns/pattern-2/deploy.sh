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

# This script depends on curl and unzip, so make sure they are already installed.

echo "This is a Dummy Deployment creation scripts to deploy a standalone WSO2 Identity server"
echo "Here the test infrastructure is your local machine."
pwd
#if you do not have a account, please create a account at https://wso2.com/
WOS2_USER=yasassri@wso2.com
WSO2_PASSWORD=xxxxxxx
DEPLOYMENT_EP_FILE_NAME=deployment_eps

#echo "Downloading the IS 5.4.0 distribution"
#curl -k https://product-dist.wso2.com/products/identity-server/5.4.0/wso2is-5.4.0.zip --user $WSO2_USER:$WSO2_PASSWORD -o is.zip

echo "unzip the IS distribution"
unzip -q -o wso2is-5.4.0.zip

echo -n "Starting the IS server"
bash ./wso2is-5.4.0/bin/wso2server.sh start

# TO-DO: introduce loop break condition
x=0;
retry_count=60;
echo "Wait for the server start up"
while ! nc -z localhost 9763; do   
  sleep 2 # wait for 2 second before check again
  echo -n "."
  if [ $x = $retry_count ]; then
    echo "Identity server never started."
        exit 1
  fi
x=$((x+1))
done

echo "Successfully started the server!!"
echo "Generating the deployment_ep file with the endpoints"

echo "https://localhost:9443/carbon" > $DEPLOYMENT_EP_FILE_NAME

echo "Updating the user.property file for tests"
echo "IS_HOST=https://localhost:9443/carbon" > ./Solutions/solution01/src/test/resources/user.properties

echo "Deployment has finished, test execution will commence next"

