#!/bin/bash

# ----------------------------------------------------------------------------
#
# Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# ----

TESTGRID_DIR=/opt/testgrid/workspace

PRODUCT_REPOSITORY=$1
PRODUCT_REPOSITORY_BRANCH=$2
PRODUCT_NAME=$3
PRODUCT_VERSION=$4
GIT_USER=$5
GIT_PASS=$6
PRODUCT_REPOSITORY_NAME=$(echo $PRODUCT_REPOSITORY | rev | cut -d'/' -f1 | rev | cut -d'.' -f1)
PRODUCT_REPOSITORY_PACK_DIR="$TESTGRID_DIR/$PRODUCT_REPOSITORY_NAME/modules/distribution/product/target"
PRODUCT_PACK_NAME="$PRODUCT_NAME-$PRODUCT_VERSION"

rm $PRODUCT_PACK_NAME.zip

git clone https://${GIT_USER}:${GIT_PASS}@$PRODUCT_REPOSITORY --branch $PRODUCT_REPOSITORY_BRANCH --single-branch

cd $PRODUCT_REPOSITORY_NAME
mvn clean install -Dmaven.test.skip=true
mv -f $PRODUCT_REPOSITORY_PACK_DIR/$PRODUCT_PACK_NAME.zip $TESTGRID_DIR/
unzip -q $TESTGRID_DIR/$PRODUCT_PACK_NAME.zip -d $TESTGRID_DIR/
