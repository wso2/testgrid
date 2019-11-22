#!/usr/bin/env bash
# dockerGenerator-ubuntu.sh : generates dockerfiles for WSO2 ubuntu based images.
# Github:
# Copyright (c) 2019 WSO2, Inc. / Nishika De Silva

# "^@!*" <name-of-the-script> will be replaced this script
set -o xtrace; set -e

#TODO: textfile to get product name and versions

PRODUCT_SERVER_NAME=""
PRODUCT_SERVER_VERSION=""

JDK_TYPE=""
JDBC_TYPE=""
OPERATING_SYSTEM=""

GIT_REPO_LOCATION="" ; GIT_REPOSITORY=""; GIT_REPOSITORY_BRANCH=""

#TODO: LOG_FILE=""
DOCKERFILE_LOCATION=""
CONTEXT="TestGrid";
CURRENT_WORKING_DIR=$(pwd)
# TODO: usage(){}
# TODO: get args promting from command line
# TODO: use a file to get args

while [[ $# -gt 0 ]]
do
arg="$1"
case "$arg" in
    -psn|--product-server-name)
    PRODUCT_SERVER_NAME="$2"
    shift 2
    ;;
    -psv|--product-server-version)
    PRODUCT_SERVER_VERSION="$2"
    shift 2
    ;;
    -gitrepo|--github-repository)
    GIT_REPOSITORY="$2"
    shift 2
    ;;
    -grb|--github-repository-branch)
    GIT_REPOSITORY_BRANCH="$2"
    shift 2
    ;;
    -outdir|--output-directory)
    GIT_REPO_LOCATION="$2"
    shift 2
    ;;
    -dockerdir|--dockerfile-directory)
    DOCKERFILE_DIR="$2"
    shift 2
    ;;
    -os|--operating-system)
    OPERATING_SYSTEM="$2"
    shift 2
    ;;
    --)
    shift
    break
    ;;
    -*|--*)
    echo "Unsupported input"

esac
done

: ${GIT_REPO_LOCATION:=~/wso2-dockerfiles/"${PRODUCT_SERVER_NAME}-${PRODUCT_SERVER_VERSION}"}
: ${IS_DEFAULT_LOCATION:=true}

write_message(){
  msg="$1"
  echo "[$(date '+%D-%T')] $msg"
}

# get_jdbc_url(){
#   MYSQL_JDBC_VERSION="5.1.47"; ORACLE_JDBC_VERSION="12.2.0.1"; MSSQL_JDBC_VERSION="7.0.0"; POSTGRESQL_JDBC_VERSION="9.4.1212"
#
#   case "${JDBC_TYPE}" in
#     mysql)
#       sed -i.bak 's/"^@!jdbc-con$version"/ARG MYSQL_JDBC_VERSION='${MYSQL_JDBC_VERSION}'/g' ${DOCKERFILE}
#       JDBC_URL='http://central.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_JDBC_VERSION}/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar'
#       ;;
#     oracle)
#       sed -i.bak 's/"^@!jdbc-con$version"/ARG ORACLE_JDBC_VERSION='${ORACLE_JDBC_VERSION}'/g' ${DOCKERFILE}
#       JDBC_URL='https://maven.xwiki.org/externals/com/oracle/jdbc/ojdbc8/${ORACLE_JDBC_VERSION}/ojdbc8-${ORACLE_JDBC_VERSION}.jar'
#       ;;
#     mssql)
#       sed -i.bak 's/"^@!jdbc-con$version"/ARG MSSQL_JDBC_VERSION='${MSSQL_JDBC_VERSION}'/g' ${DOCKERFILE}
#       JDBC_URL='http://central.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/${MSSQL_JDBC_VERSION}.jre8/mssql-jdbc-${MSSQL_JDBC_VERSION}.jre8.jar'
#       ;;
#     postgres)
#       sed -i.bak 's/"^@!jdbc-con$version"/ARG POSTGRESQL_JDBC_VERSION='${POSTGRESQL_JDBC_VERSION}'/g' ${DOCKERFILE}
#       JDBC_URL='https://jdbc.postgresql.org/download/postgresql-${POSTGRESQL_JDBC_VERSION}.jar'
#       ;;
#   esac

#}

# create_addtional_java_directory(){
#   sed -i.bak 's#"^@!create@add!java"#RUN \
#       mkdir -p ${USER_HOME}/.java/.systemPrefs \\ \
#       && mkdir -p ${USER_HOME}/.java/.userPrefs \\ \
#       && chmod -R 755 ${USER_HOME}/.java \\ \
#       && chown -R ${USER}:${USER_GROUP} ${USER_HOME}/.java#g' "${DOCKERFILE}"
#
#   sed -i.bak 's#"^@!env@java+opts"#JAVA_OPTS="-Djava.util.prefs.systemRoot=${USER_HOME}/.java -Djava.util.prefs.userRoot=${USER_HOME}/.java/.userPrefs"#g'
# }

# jdk_getset(){
#   case "${JDK_TYPE}" in
#     adopt_open_jdk8)
#       JDK_URL="https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jdk_x64_linux_hotspot_8u222b10.tar.gz"
#       sed -i.bak 's/"^@!other$header@necessary"/\\/g' ${DOCKERFILE}
#       if [[ "$CREATE_ADDITIONAL_DIR_JAVA" = true ]]; then
#           create_addtional_java_directory
#       else
#           sed -i.bak 's/"^@!create@add!java"//g' "${DOCKERFILE}"
#           sed -i.bak 's/"^@!env@java+opts"//g' "${DOCKERFILE}"
#       fi
#       ;;
#     corretto)
#       JDK_URL="https://d3pxv6yz143wms.cloudfront.net/8.222.10.1/amazon-corretto-8.222.10.1-linux-x64.tar.gz"
#       sed -i.bak 's/"^@!other$header@necessary"/\\/g' ${DOCKERFILE}
#       sed -i.bak 's/"^@!create@add!java"//g' "${DOCKERFILE}"
#       sed -i.bak 's/"^@!env@java+opts"//g' "${DOCKERFILE}"
#       ;;
#     oracle)
#       JDK_URL="http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.tar.gz"
#       sed -i.bak 's/"^@!other$header@necessary"/--no-check-certificate --https-only -c --header \\ \
#         "Cookie: oraclelicense=accept-securebackup-cookie" \\/g' ${DOCKERFILE}
#       sed -i.bak 's/"^@!create@add!java"//g' "${DOCKERFILE}"
#       sed -i.bak 's/"^@!env@java+opts"//g' "${DOCKERFILE}"
#       ;;
#   esac
#   sed -i.bak 's#"^@!url@jdk$install"#'${JDK_URL}'#g' ${DOCKERFILE}
# }



replace_ei(){

  sed -i.bak 's/"^@!additional+server!info"/ARG WSO2_SERVER_PROFILE_NAME=integrator\
ARG WSO2_SERVER_PROFILE_OPTIMIZER_NUMBER=1/g' "${DOCKERFILE}"

  if [[ "${OPERATING_SYSTEM}" = "alpine" ]]; then
    sed -i.bak 's/"^@!additional!dependants"/zip bash/g' "${DOCKERFILE}"
  else
    sed -i.bak 's/"^@!additional!dependants"/zip/g' "${DOCKERFILE}"
  fi

  sed -i.bak 's/"^@!list@expose-ports"/8280 8243 9443 4100/g' "${DOCKERFILE}"

  sed -i.bak 's#"^@!get@wso2$basepackage"#COPY --chown=wso2carbon:wso2 files/${WSO2_SERVER}/ ${WSO2_SERVER_HOME}/\
COPY --chown=wso2carbon:wso2 files/${WSO2_SERVER}/repository/deployment/server/ ${USER_HOME}/wso2-tmp/server/#g' "${DOCKERFILE}"

    sed -i.bak 's#"^@!install+JDBC"#@JDBC_URL_WSO2 ${WSO2_SERVER_HOME}/dropins/#g' ${DOCKERFILE}


}

replace_apim(){
  sed -i.bak 's/"^@!additional+server!info"//g' "${DOCKERFILE}"

  if [[ "${OPERATING_SYSTEM}" = "centos" ]]; then
    sed -i.bak 's/"^@!additional!dependants"//g' "${DOCKERFILE}"
  else
    sed -i.bak 's/"^@!additional!dependants"/libxml2-utils/g' "${DOCKERFILE}"
  fi

  sed -i.bak 's/"^@!list@expose-ports"/9763 9443 9999 11111 8280 8243 5672 9711 9611 7711 7611 10397 9099/g' "${DOCKERFILE}"

  sed -i.bak 's#"^@!get@wso2$basepackage"#COPY --chown=wso2carbon:wso2 files/${WSO2_SERVER}/ ${WSO2_SERVER_HOME}/\
COPY --chown=wso2carbon:wso2 files/${WSO2_SERVER}/repository/deployment/server/ ${USER_HOME}/wso2-tmp/server/#g' "${DOCKERFILE}"

  sed -i.bak 's#"^@!install+JDBC"#@JDBC_URL_WSO2 ${WSO2_SERVER_HOME}/repository/components/dropins/#g' ${DOCKERFILE}

}

replace_is(){
  sed -i.bak 's/"^@!additional+server!info"//g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!additional!dependants"//g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!list@expose-ports"/4000 9763 9443/g' "${DOCKERFILE}"

  sed -i.bak 's#"^@!get@wso2$basepackage"#COPY --chown=wso2carbon:wso2 files/${WSO2_SERVER}/ ${WSO2_SERVER_HOME}/ \
COPY --chown=wso2carbon:wso2 files/${WSO2_SERVER}/repository/deployment/ ${USER_HOME}/wso2-tmp/deployment/#g' "${DOCKERFILE}"

  sed -i.bak 's#"^@!install+JDBC"#@JDBC_URL_WSO2 ${WSO2_SERVER_HOME}/repository/components/dropins/#g' ${DOCKERFILE}

}

clone_git_repo(){
  write_message "Cloning ${GIT_REPOSITORY} to $(pwd)"
  git clone https://github.com/wso2/${GIT_REPOSITORY}
  GIT_REPO_LOCATION="${GIT_REPOSITORY}"
}

check_dockerfile(){

  read -p  $(echo "${GIT_REPO_LOCATION} not found.Do you want to clone the ${PRODUCT_SERVER_NAME}-${PRODUCT_SERVER_VERSION} repository (Y/n)") NEED_CLONE
  if [[ "${NEED_CLONE}" = "Y" ]] || [[ "${NEED_CLONE}" = "y" ]];then
    clone_git_repo
  elif [[ "${NEED_CLONE}" = "N" ]] || [[ "${NEED_CLONE}" = "n" ]];then
    write_message "Please specify an existing directory and try again."
    exit 0;
  else
    echo "Invalid command. Try again"
    check_dockerfile
  fi
}

main(){

  write_message "Creating Dockerfile for ${PRODUCT_SERVER_NAME}-${PRODUCT_SERVER_VERSION}"

  if [[ ! -d "${GIT_REPO_LOCATION}" ]]; then
    check_dockerfile
  fi

  cd "${GIT_REPO_LOCATION}"; git checkout ${GIT_REPOSITORY_BRANCH};

  DOCKERFILE="${DOCKERFILE_DIR}/Dockerfile"

  #check if  JAVA_OPT is declared in existing dockerfile for adopt openjdk
  if [[ ! -z "$(cat ${DOCKERFILE} | grep JAVA_OPTS)" ]] ; then
      yes | cp "${CURRENT_WORKING_DIR}/${OPERATING_SYSTEM}/Dockerfile" "${DOCKERFILE_DIR}/"
      sed -i.bak 's/"^@!create@add!java"/@JAVA_OPTS_DIR/g' "${DOCKERFILE}"
      sed -i.bak 's/"^@!env@java+opts"/@JAVA_OPTS_ENV/g' "${DOCKERFILE}"
  else
    yes | cp "${CURRENT_WORKING_DIR}/${OPERATING_SYSTEM}/Dockerfile" "${DOCKERFILE_DIR}/"
    sed -i.bak 's/"^@!create@add!java"//g' "${DOCKERFILE}"
    sed -i.bak 's/"^@!env@java+opts"//g' "${DOCKERFILE}"
  fi

  sed -i.bak 's/"^@!product!wso2-001"/'${PRODUCT_SERVER_NAME}'/g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!version!wso2-001"/'${PRODUCT_SERVER_VERSION}'/g' "${DOCKERFILE}"

  case "${PRODUCT_SERVER_NAME}" in
    wso2am)
      replace_apim
      ;;
    wso2is)
      replace_is
      ;;
    wso2ei)
      replace_ei
      ;;
  esac

  sed -i.bak 's#"^@!url@jdk$install"#@JDK_URL#g' ${DOCKERFILE}

  # Remove backup dockerfile
  rm "${DOCKERFILE_DIR}/Dockerfile.bak"

  write_message "Dockerfile is saved in ${GIT_REPO_LOCATION}/${DOCKERFILE_LOCATION}"
}

main
