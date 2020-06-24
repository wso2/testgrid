#!/bin/bash

WORKING_DIR=$(pwd)

PRODUCT_REPOSITORY=$1
PRODUCT_REPOSITORY_BRANCH=$2
PRODUCT_REPOSITORY_NAME=$(echo $PRODUCT_REPOSITORY | rev | cut -d'/' -f1 | rev | cut -d'.' -f1)
LOCAL_PRODUCT_PACK_LOCATION="/mnt/$(echo $PRODUCT_REPOSITORY_NAME | cut -d'-' -f2)"

PRODUCT_REPOSITORY_PACK_DIR="$WORKING_DIR/$PRODUCT_REPOSITORY_NAME/modules/distribution/product/target"
INT_TEST_MODULE_DIR="$WORKING_DIR/$PRODUCT_REPOSITORY_NAME/modules/integration"
API_IMPORT_EXPORT_MODULE_DIR="$WORKING_DIR/$PRODUCT_REPOSITORY_NAME/modules/api-import-export"
NEXUS_SCRIPT_NAME="uat-nexus-settings.xml"

PRODUCT_NAME=$3
PRODUCT_VERSION=$4

GIT_USER=$5
GIT_PASS=$6

set -o xtrace

function log_info(){
    echo "[INFO][$(date '+%Y-%m-%d %H:%M:%S')]: $1"
}

echo "Test script running"

log_info "Clone Product repository"
git clone https://$GIT_USER:$GIT_PASS@$PRODUCT_REPOSITORY --branch $PRODUCT_REPOSITORY_BRANCH

mkdir -p $PRODUCT_REPOSITORY_PACK_DIR

log_info "Copying product pack to Repository"
cd $LOCAL_PRODUCT_PACK_LOCATION && zip -qr $PRODUCT_NAME-$PRODUCT_VERSION.zip $PRODUCT_NAME-$PRODUCT_VERSION
mv $PRODUCT_NAME-$PRODUCT_VERSION.zip $PRODUCT_REPOSITORY_PACK_DIR/.
mv $WORKING_DIR/$NEXUS_SCRIPT_NAME $INT_TEST_MODULE_DIR/.

source /etc/environment

cd $API_IMPORT_EXPORT_MODULE_DIR && mvn clean install -fae -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
cd $INT_TEST_MODULE_DIR  && mvn clean install -s $NEXUS_SCRIPT_NAME -fae -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn