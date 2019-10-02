#!/bin/bash

host=http://localhost:8080/admin

. $PWD/params.properties

while [ $(curl -s -w "%{http_code}" $host/cli -o /dev/null) -eq 503 ]
do
 sleep 5
done

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "GIT_WUM_USERNAME",
    "secret":"$GIT_WUM_USERNAME",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "GIT_WUM_PASSWORD",
    "secret":"$GIT_WUM_PASSWORD",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "WUM_UAT_URL",
    "secret":"$WUM_UAT_URL",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "WUM_UAT_APPKEY",
    "secret":"$WUM_UAT_APPKEY",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "WUM_USERNAME",
    "secret":"$WUM_USERNAME",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "WUM_PASSWORD",
    "secret":"$WUM_PASSWORD",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "dockerhub_ballerina_scenarios_password",
    "secret":"$dockerhub_ballerina_scenarios_password",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "dockerhub_ballerina_scenarios_username",
    "secret":"$dockerhub_ballerina_scenarios_username",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "ballerina_integrator_aws_s3_access_key",
    "secret":"$ballerina_integrator_aws_s3_access_key",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "ballerina_integrator_aws_s3_secret_key",
    "secret":"$ballerina_integrator_aws_s3_secret_key",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "github_bot_as_secret_text",
    "secret":"$github_bot_as_secret_text",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "github_bot",
    "username": "github_bot",
    "privateKeySource": {
      "stapler-class": "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource",
      "privateKey": "dummy",
    },
    "stapler-class": "com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
-F secret=@/"$DEPLOYMENT_KEY_FILE_LOCATION" \
-F 'json={
 "": "4",
 "credentials": {
   "file":"secret",
   "id": "DEPLOYMENT_KEY",
   "stapler-class": "org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl",
   "$class": "org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl"
 }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "AWS_ACCESS_KEY_ID",
    "secret":"$AWS_ACCESS_KEY_ID",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

curl -X POST ''"$host"'/credentials/store/system/domain/_/createCredentials' \
--data-urlencode 'json={
  "": "0",
  "credentials": {
    "scope": "GLOBAL",
    "id": "AWS_SECRET_ACCESS_KEY",
    "secret":"$AWS_SECRET_ACCESS_KEY",
    "$class": "org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl"
  }
}'

while [ "$(java -jar $PWD/tomcat/apache-tomcat-8.5.43/webapps/admin/WEB-INF/jenkins-cli.jar -s $host groovy = < $PWD/scripts/credentialsEnumerator.groovy)" -lt 15 ]
do
   sleep 1
done
