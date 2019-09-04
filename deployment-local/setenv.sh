#!/bin/sh

. $PWD/credentials.config

export TESTGRID_HOME="$PWD/testgrid/testgrid-home"
export CATALINA_OPTS="-Djenkins.install.runSetupWizard=false -DJENKINS_HOME=jenkins-home"

export common_configs="$(cat $common_configs_path)"
export uat_nexus_settings="$(cat $uat_nexus_settings_path)"
