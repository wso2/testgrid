#!/usr/bin/env bash
# dockerGenerator-ubuntu.sh : generates dockerfiles for WSO2 ubuntu based images.
# Github:
# Copyright (c) 2019 WSO2, Inc. / Nishika De Silva

# "^@!*" <name-of-the-script> will be replaced this script
set -o xtrace; set -e

#TODO: textfile to get product name and versions
: ${DOCKERFILE_HOME:=~/wso2-dockerfiles/"${PRODUCT_SERVER_NAME}-${PRODUCT_SERVER_VERSION}"}
: ${IS_DEFAULT_LOCATION:=true}

PRODUCT_SERVER_NAME=""
PRODUCT_SERVER_VERSION=""

JDK_TYPE=""
JDBC_TYPE=""

#TODO: LOG_FILE=""
DOCKERFILE_LOCATION=""
CONTEXT="TestGrid"

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
    -outdir|--output-directory)
    DOCKERFILE_HOME="$2"
    IS_DEFAULT_LOCATION=FALSE
    shift 2
    ;;
    -jdk|--type-jdk)
    JDK_TYPE="$2"
    shift 2
    ;;
    -jdbc|--type-jdbc)
    JDBC_TYPE="$2"
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

write_message(){
  msg="$1"
  echo "[$(date '+%D-%T')] $msg"
}

get_jdbc_url(){
  MYSQL_JDBC_VERSION="5.1.47"; ORACLE_JDBC_VERSION="12.2.0.1"; MSSQL_JDBC_VERSION="7.0.0"; POSTGRESQL_JDBC_VERSION="9.4.1212"

  case "${JDBC_TYPE}" in
    mysql)
      JDBC_URL="http://central.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_JDBC_VERSION}/mysql-connector-java-${MYSQL_JDBC_VERSION}.jar"
      ;;
    oracle)
      JDBC_URL="https://maven.xwiki.org/externals/com/oracle/jdbc/ojdbc8/${ORACLE_JDBC_VERSION}/ojdbc8-${ORACLE_JDBC_VERSION}.jar"
      ;;
    mssql)
      JDBC_URL="http://central.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/${MSSQL_JDBC_VERSION}.jre8/mssql-jdbc-${MSSQL_JDBC_VERSION}.jre8.jar"
      ;;
    postgres)
      JDBC_URL="https://jdbc.postgresql.org/download/postgresql-${POSTGRESQL_JDBC_VERSION}.jar"
      ;;
  esac

}

jdk_getset(){
  case "${JDK_TYPE}" in
    adopt_open_jdk8)
      JDK_URL="https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jdk_x64_linux_hotspot_8u222b10.tar.gz"
      sed -i.bak 's/"^@!other$header@necessary"/\\/g' ${DOCKERFILE}
      ;;
    corretto)
      JDK_URL="https://d3pxv6yz143wms.cloudfront.net/8.222.10.1/amazon-corretto-8.222.10.1-linux-x64.tar.gz"
      sed -i.bak 's/"^@!other$header@necessary"/\\/g' ${DOCKERFILE}
      ;;
    oracle)
      JDK_URL="http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.tar.gz"
      sed -i.bak 's/"^@!other$header@necessary"/--no-check-certificate --https-only -c --header \\ \
        "Cookie: oraclelicense=accept-securebackup-cookie" \\/g' ${DOCKERFILE}
      ;;
  esac
  sed -i.bak 's#"^@!url@jdk$install"#'${JDK_URL}'#g' ${DOCKERFILE}
}

replace_ei(){

  sed -i.bak 's/"^@!additional+server!info"/ARG WSO2_SERVER_PROFILE_NAME=integrator\
ARG WSO2_SERVER_PROFILE_OPTIMIZER_NUMBER=1/g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!additional!dependants"/zip/g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!list@expose-ports"/8280 8243 9443 4100/g' "${DOCKERFILE}"

  if [[ ! "${CONTEXT} = TestGrid" ]]; then
    sed -i.bak 's/"^@!get@wso2$basepackage"/RUN \\ \
        wget --no-check-certificate -O ${WSO2_SERVER}.zip "${WSO2_SERVER_DIST_URL}" \\ \
        \&\& unzip -d ${USER_HOME} ${WSO2_SERVER}.zip \\ \
        \&\& rm -f ${WSO2_SERVER}.zip \\ \
        \&\& echo "${WSO2_SERVER_PROFILE_OPTIMIZER_NUMBER}" | bash ${WSO2_SERVER_HOME}\/bin\/profile-creator.sh \\ \
        \&\& rm -rf ${WSO2_SERVER_HOME} \\ \
        \&\& unzip -d ${USER_HOME} ${WSO2_SERVER_HOME}_${WSO2_SERVER_PROFILE_NAME}.zip \\ \
        \&\& chown wso2carbon:wso2 -R ${WSO2_SERVER_HOME} \\ \
        \&\& rm -f ${WSO2_SERVER_HOME}_${WSO2_SERVER_PROFILE_NAME}.zip/g' "${DOCKERFILE}"
  fi
}

replace_apim(){
  sed -i.bak 's/"^@!additional+server!info"//g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!additional!dependants"/libxml2-utils/g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!list@expose-ports"/9763 9443 9999 11111 8280 8243 5672 9711 9611 7711 7611 10397 9099/g' "${DOCKERFILE}"

  if [[ ! "${CONTEXT} = TestGrid" ]]; then
    sed -i.bak 's/"^@!get@wso2$basepackage"/RUN \\   \
        wget --no-check-certificate -O ${WSO2_SERVER}.zip "${WSO2_SERVER_DIST_URL}" \\  \
        \&\& unzip -d ${USER_HOME} ${WSO2_SERVER}.zip \\ \
        \&\& chown wso2carbon:wso2 -R ${WSO2_SERVER_HOME} \\ \
        \&\& rm -f ${WSO2_SERVER}.zip/g' "${DOCKERFILE}"
  fi
}

replace_is(){
  sed -i.bak 's/"^@!additional+server!info"//g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!additional!dependants"//g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!list@expose-ports"/4000 9763 9443/g' "${DOCKERFILE}"

  if [[ ! "${CONTEXT} = TestGrid" ]]; then
    sed -i.bak 's/"^@!get@wso2$basepackage"/RUN \\ \
        wget --no-check-certificate -O ${WSO2_SERVER}.zip "${WSO2_SERVER_DIST_URL}" \\ \
        \&\& unzip -d ${USER_HOME} ${WSO2_SERVER}.zip \\ \
        \&\& chown wso2carbon:wso2 -R ${WSO2_SERVER_HOME} \\ \
        \&\& rm -f ${WSO2_SERVER}.zip/g' "${DOCKERFILE}"
  fi
}

main(){

  write_message "Creating Dockerfile for ${PRODUCT_SERVER_NAME}-${PRODUCT_SERVER_VERSION}"
  if [[ "${IS_DEFAULT_LOCATION}" = true ]]
  then
    write_message "No directory is specified for saving Dockerfiles. Using default location"
  fi

  # creating necessary directories
  mkdir -p "${DOCKERFILE_HOME}"

  DOCKERFILE="${DOCKERFILE_HOME}/Dockerfile"

  yes | cp "ubuntu/Dockerfile" "${DOCKERFILE_HOME}/"

  sed -i.bak 's/"^@!product!wso2-001"/'${PRODUCT_SERVER_NAME}'/g' "${DOCKERFILE}"
  sed -i.bak 's/"^@!version!wso2-001"/'${PRODUCT_SERVER_VERSION}'/g' "${DOCKERFILE}"

  case "${PRODUCT_SERVER_NAME}" in
    wso2am)
    replace_apim
    get_jdbc_url;
    sed -i.bak 's#"^@!install+JDBC"#'${JDBC_URL}' ${WSO2_SERVER_HOME}/repository/components/dropins/#g' ${DOCKERFILE}
    jdk_getset
    # "^@!url@jdk$install"
    ;;
    wso2is)
    replace_is
    get_jdbc_url;
    sed -i.bak 's#"^@!install+JDBC"#'${JDBC_URL}' ${WSO2_SERVER_HOME}/repository/components/dropins/#g' ${DOCKERFILE}
    jdk_getset
    ;;
    wso2ei)
    replace_ei
    get_jdbc_url;
    sed -i.bak 's#"^@!install+JDBC"#'${JDBC_URL}' ${WSO2_SERVER_HOME}/dropins/#g' ${DOCKERFILE}
    jdk_getset
    ;;
  esac

  # Remove backup dockerfile
  rm "${DOCKERFILE_HOME}/${DOCKERFILE_LOCATION}/Dockerfile.bak"

  write_message "Dockerfile is saved in ${DOCKERFILE_HOME}/${DOCKERFILE_LOCATION}"
}

main
