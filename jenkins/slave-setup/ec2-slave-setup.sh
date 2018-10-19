#!/usr/bin/env bash
#===================================================================================
#
# FILE: ec2-slave-setup.sh
#
# USAGE: ec2-slave-setup.sh [Master_IP]
#
# DESCRIPTION: This script is used to setup the Jenkins slave instance to be
#              operational. This is triggered from the slave setup plugin.
#
#===================================================================================
set -xe
set -o xtrace

TG_MASTER_IP="$1"
MYSQL_DRIVER_LOCATION='http://central.maven.org/maven2/mysql/mysql-connector-java/6.0.6/mysql-connector-java-6.0.6.jar'

MASTER_KEY_LOCATION='/testgrid/testgrid-prod-key.pem'

# Create TG Home directory
mkdir -p /testgrid/testgrid-home/testgrid-dist
cd /testgrid/testgrid-home/testgrid-dist

# Clear content if there is anything
rm -rf *

# Downloading the TG distribution
echo "Downloading the TG Distribution!"
echo "Download Location ${TG_MASTER_IP}:/testgrid/testgrid-home/testgrid-dist/WSO2-TestGrid.zip"
scp -i ${MASTER_KEY_LOCATION} -o StrictHostKeyChecking=no ubuntu@${TG_MASTER_IP}:/testgrid/testgrid-home/testgrid-dist/WSO2-TestGrid.zip /testgrid/testgrid-home/testgrid-dist/
echo "Copying config.properties from master!"
scp -i ${MASTER_KEY_LOCATION} -o StrictHostKeyChecking=no ubuntu@${TG_MASTER_IP}:/testgrid/testgrid-home/config.properties /testgrid/testgrid-home/

echo "Unzip Tesgrid distribution and copy mysql jar"
unzip WSO2-TestGrid.zip
curl -o ./WSO2-TestGrid/lib/mysql.jar ${MYSQL_DRIVER_LOCATION}

# Starting a virtual display to support FX chart generation
echo "Starting Xvfb virtual display for chart generation"
export DISPLAY=:95
nohup Xvfb :95 -screen 0 1024x768x16 > /dev/null 2>&1 &

# Changing the testgrid home ownership
chown -R ubuntu:ubuntu /testgrid/testgrid-home
