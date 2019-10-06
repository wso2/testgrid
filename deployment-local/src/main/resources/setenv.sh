#!/bin/sh

. $PWD/params.properties

export TESTGRID_HOME="$PWD/testgrid/testgrid-home"
export CATALINA_OPTS="-Djenkins.install.runSetupWizard=false -DJENKINS_HOME=jenkins-home -Dkeystore_path=$keystore_path -Dkeystore_password=$keystore_password"
export common_configs="$(cat $common_configs_path)"
export uat_nexus_settings="$(cat $uat_nexus_settings_path)"
export testgrid_jenkins_library_path="$testgrid_jenkins_library_path"