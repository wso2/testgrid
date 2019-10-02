#!/bin/sh

host=http://localhost:8080/admin
url=/pluginManager/installNecessaryPlugins

while [ $(curl -s -w "%{http_code}" $host/cli -o /dev/null) -eq 503 ]
do
 sleep 5
done

if [ $(java -jar $PWD/tomcat/apache-tomcat-8.5.43/webapps/admin/WEB-INF/jenkins-cli.jar -s $host groovy = < $PWD/scripts/pluginsEnumerator.groovy) -eq 153 ]
then
  exit 0
fi

curl -X POST -d '<jenkins><install plugin="ec2@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="amazon-ecr@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="aws-java-sdk@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="AnchorChain@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="ant@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="apache-httpcomponents-client-4-api@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="blueocean-autofavorite@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="cloudbees-bitbucket-branch-source@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="blueocean-bitbucket-pipeline@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="blueocean@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="blueocean-core-js@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="blueocean-pipeline-editor@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="build-timeout@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="build-view-column@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="calendar-view@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="config-file-provider@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="copyartifact@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="docker-plugin@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="email-ext@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="envinject-api@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="envinject@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="external-workspace-manager@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="generic-webhook-trigger@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="github-oauth@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="github-pullrequest@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="gradle@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="greenballs@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="postbuild-task@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="icon-shim@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="instant-messaging@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="handlebars@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="momentjs@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="jobConfigHistory@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="jobcopy-builder@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="log-file-filter@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="mapdb-api@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="mask-passwords@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="maven-plugin@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="pipeline-utility-steps@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="pipeline-aws@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="pipeline-github-lib@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="pipeline-rest-api@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="project-build-times@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="resource-disposer@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="role-strategy@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="s3@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="saferestart@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="schedule-build@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="shelve-project-plugin@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="sidebar-link@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="simple-theme-plugin@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="slack@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="slave-setup@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="ssh-agent@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="subversion@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="timestamper@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="view-job-filters@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="ws-cleanup@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="mission-control-view@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="configuration-as-code@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url
curl -X POST -d '<jenkins><install plugin="pipeline-stage-view@latest" /></jenkins>' --header 'Content-Type: text/xml' $host$url

while [ $(java -jar $PWD/tomcat/apache-tomcat-8.5.43/webapps/admin/WEB-INF/jenkins-cli.jar -s $host groovy = < $PWD/scripts/pluginsEnumerator.groovy) -lt 153 ]
do
   echo "plugins still installing.... "$(java -jar $PWD/tomcat/apache-tomcat-8.5.43/webapps/admin/WEB-INF/jenkins-cli.jar -s $host groovy = < $PWD/scripts/pluginsEnumerator.groovy)" installed"
   sleep 3
done

curl -X POST $host/safeRestart
