#!/bin/sh

export TESTGRID_HOME="$PWD/testgrid/testgrid-home"
export CATALINA_OPTS="-Djenkins.install.runSetupWizard=false -DJENKINS_HOME=jenkins-home"

