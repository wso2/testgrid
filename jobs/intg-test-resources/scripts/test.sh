#!/bin/bash

# LOCAL_PRODUCT_PACK_LOCATION=""
PRODUCT_REPOSITORY=$1
PRODUCT_REPOSITORY_BRANCH=$2
# PRODUCT_REPOSITORY_PACK_DIR=""

# PRODUCT_NAME=""
# PRODUCT_VERSION=""

set -o xtrace

function log_info(){
    echo "[INFO][$(date '+%Y-%m-%d %H:%M:%S')]: $1"
}

echo "Test script running"

log_info "Clone Product repository"
git clone $PRODUCT_REPOSITORY --branch $PRODUCT_REPOSITORY_BRANCH

log_info "Copying product pack to Repository"
# cp $LOCAL_PRODUCT_PACK_LOCATION/$PRODUCT_NAME-$PRODUCT_VERSION.zip $PRODUCT_REPOSITORY_PACK_DIR/$PRODUCT_NAME-$PRODUCT_VERSION.zip
