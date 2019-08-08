#!/usr/bin/env bash
# This will build Docker images for all the latest WSO2 products to use in testgrid
set -o xtrace; set -e
alias wget='wget -q'
alias unzip='unzip -q'

LOG_FILE="$(date +%F).log"
BUILD_SCRIPT="docker_build.sh"

# Git repo URLs

#TODO: Change URLs when it comes to the production stage.
APIM_GIT_REPO_URL="https://github.com/wso2/docker-apim/archive/2.6.x.zip"
EI_GIT_REPO_URL="https://github.com/wso2/docker-ei/archive/6.5.x.zip"
IS_GIT_REPO_URL="https://github.com/wso2/docker-is/archive/5.8.x.zip"

# name of the unzipped directory
APIM_GIT_REPO_NAME="docker-apim-2.6.x"
EI_GIT_REPO_NAME="docker-ei-6.5.x"
IS_GIT_REPO_NAME="docker-is-5.8.x"

echo "----------------------------------------Building new images with latest updates--------------------------------------------------"

echo "----------------------------------------Building wso2am-2.6.0---------------------------------------------------------"
      ./${BUILD_SCRIPT} --log-file ${LOG_FILE} \
      --git-repo-zip-url ${APIM_GIT_REPO_URL} \
      --product-name "wso2am" \
      --wso2-server-version "2.6.0" \
      --git-repo-name ${APIM_GIT_REPO_NAME} \
      --docker-file-dir "ubuntu/apim" \
      --tag "2.6.0" \

      if [ $? -ne 0 ]; then
       exit 1
      fi

echo "WSO2APIM 2.6.0 Image build is successful !" | tee -a ${LOG_FILE}


echo "---------------------------------------------------------Building wso2is-5.8.0---------------------------------------------------------"
      ./${BUILD_SCRIPT} --log-file ${LOG_FILE} \
      --git-repo-zip-url ${IS_GIT_REPO_URL} \
      --product-name "wso2is" \
      --wso2-server-version "5.8.0" \
      --git-repo-name ${IS_GIT_REPO_NAME} \
      --docker-file-dir "ubuntu/is"\
      --tag "5.8.0" \

      if [ $? -ne 0 ]; then
       exit 1
      fi

echo "WSO2IS 5.8.0 Image build is successful !" | tee -a ${LOG_FILE}

echo "---------------------------------------------------------Building wso2ei-base:6.5.0---------------------------------------------------------"
    ./${BUILD_SCRIPT} --log-file ${LOG_FILE} \
      --git-repo-zip-url ${EI_GIT_REPO_URL} \
      --product-name "wso2ei" \
      --wso2-server-version "6.5.0" \
      --git-repo-name ${EI_GIT_REPO_NAME} \
      --docker-file-dir "ubuntu/integrator" \
      --tag "6.5.0" \


    if [ $? -ne 0 ]; then
      exit 1
    fi

echo "WSO2EI 6.5.0 Image build is successful !" | tee -a ${LOG_FILE}

if [ $(docker images | grep "<none>") ]; then
  docker rmi $(docker images | grep "<none>" | awk '{print $3}')
fi
echo "Docker Image building for all the products is successful !" | tee -a ${LOG_FILE}
