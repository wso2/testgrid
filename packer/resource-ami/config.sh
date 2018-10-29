#!/bin/bash

# Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#=== FUNCTION ==================================================================
# NAME: echoLogo
# DESCRIPTION: Print the WSO2 TestGrid Logo.
#===============================================================================
function echoLogo() {
	echo "__          __ ____  ____ ___    _______        _    _____      _     _ "
	echo "\ \        / / ____|/ __ \__ \  |__   __|      | |  / ____|    (_)   | |"
	echo " \ \  /\  / / (___ | |  | | ) |    | | ___  ___| |_| |  __ _ __ _  __| |"
	echo "  \ \/  \/ / \___ \| |  | |/ /     | |/ _ \/ __| __| | |_ |  __| |/ _  |"
	echo "   \  /\  /  ____) | |__| / /_     | |  __/\__ \ |_| |__| | |  | | (_| |"
	echo "    \/  \/  |_____/ \____/____|    |_|\___||___/\__|\_____|_|  |_|\____|"
	echo ""
	echo "                            AMI-Creation-Tool                           "
}

#=== FUNCTION ==================================================================
# NAME: installPackage
# DESCRIPTION: Install passed package using the package installer based on OS.
# PARAMETER 1: Package (Or set of packages) to install
#===============================================================================
function installPackage() {
    package=$1;
    echo "installing $package"
    case "$AMI_OS" in
    Ubuntu)
    sudo apt -y install $package
    ;;
    CentOS)
    sudo yum -y install $package
    ;;
    RHEL)
    sudo yum -y install $package
    ;;
    esac
}

#=== FUNCTION ==================================================================
# NAME: showMessage
# DESCRIPTION: Display message with highlighting.
# PARAMETER 1: Message to display
#===============================================================================
function showMessage() {
	message=$1;
	echo ""
    echo ""
	echo "*****************************************************************"
	echo "$message"
	echo ""
}

#=== FUNCTION ==================================================================
# NAME: installJDKs
# DESCRIPTION: Install Java Development Kits. (ORACLE JDK8 and OPEN JDK8)
#===============================================================================
function installJDKs() {
	case "$AMI_OS" in
    Ubuntu)
	#Installing ORACLE_JAVA8
	echo "Installing ORACLE_JAVA8"
	sudo add-apt-repository -y ppa:webupd8team/java
	sudo apt-get update -y
	echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
	echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
	sudo apt-get -y install oracle-java8-installer
	#Installing OpenJDK8
	echo "Installing OPENJDK 8"
	sudo add-apt-repository -y ppa:openjdk-r/ppa
	sudo apt-get update -y
	sudo apt-get -y install openjdk-8-jdk
	;;

    CentOS)
	#Installing ORACLE_JAVA8
	echo "Installing ORACLE_JAVA8"
	sudo yum localinstall -y resources/jdk-8u181-linux-x64.rpm
	#Installing OpenJDK8
	echo "Installing OPENJDK 8"
	sudo yum install -y java-1.8.0-openjdk-devel
    sudo yum install -y wget #Dependency to download JCE policies
	;;

	RHEL)
	#Installing ORACLE_JAVA8
	echo "Installing ORACLE_JAVA8"
	sudo yum localinstall -y resources/jdk-8u181-linux-x64.rpm
	#Installing OpenJDK8
	echo "Installing OPENJDK 8"
	sudo yum install -y java-1.8.0-openjdk-devel
    sudo yum install -y wget #Dependency to download JCE policies
	;;

    esac
    #Download JCE policies
	echo "Installing JCE policies (Presumed agreement on Oracle license at https://www.oracle.com/technetwork/java/javase/terms/license/index.html)"
	installPackage wget
	wget -v --header "Cookie: oraclelicense=oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip
	unzip jce_policy-8.zip -d .
	sudo mv UnlimitedJCEPolicyJDK8/local_policy.jar /usr/java/jdk1.8.0_181-amd64/jre/lib/security/
	sudo mv UnlimitedJCEPolicyJDK8/US_export_policy.jar /usr/java/jdk1.8.0_181-amd64/jre/lib/security/
}

#=== FUNCTION ==================================================================
# NAME: installMaven
# DESCRIPTION: Install Maven.
#===============================================================================
function installMaven() {
	installPackage maven
	mvn --version
}

#=== FUNCTION ==================================================================
# NAME: installGit
# DESCRIPTION: Install Git client.
#===============================================================================
function installGit() {
	installPackage git
	git --version
}

#=== FUNCTION ==================================================================
# NAME: installSQLclientsAndTools.
# DESCRIPTION: Install SQL clients and tools.
# (MSSQL client, MySQL client, Mariadb client, Oracle client)
#===============================================================================
function installSQLclientsAndTools() {
    installPackage "unzip zip"
    case "$AMI_OS" in
    Ubuntu)
	#Add MSSQL repositories
	curl https://packages.microsoft.com/keys/microsoft.asc | sudo apt-key add
        curl https://packages.microsoft.com/config/ubuntu/16.04/prod.list | sudo tee /etc/apt/sources.list.d/msprod.list
	sudo apt-get update -y
	#Install mysql client, prostgres client and mssql tools
	echo "Agreeing on EULAs of the libs; unzip, zip, alien, postgresql-client, mysql-client and continuing installation."
	sudo env ACCEPT_EULA=Y
	sudo env DEBIAN_FRONTEND="noninteractive"
	sudo apt-get install -y alien
	sudo apt-get install -y postgresql-client
	sudo apt-get install -y mysql-client

	#Install mariadb client
	sudo apt-get install -y mariadb-client
	sudo ln -sfn /opt/mssql-tools/bin/sqlcmd /usr/bin/sqlcmd
	sudo ln -sfn /opt/mssql-tools/bin/bcp /usr/bin/bcp

	sudo alien -i resources/oracle-instantclient12.2-basic-12.2.0.1.0-1.x86_64.rpm
	sudo alien -i resources/oracle-instantclient12.2-sqlplus-12.2.0.1.0-1.x86_64.rpm
	sudo ldconfig
	;;
    CentOS)
	#Add MSSQL repositories
	echo "Adding MSSQL repositories"
	sudo su -c "curl https://packages.microsoft.com/config/rhel/7/prod.repo > /etc/yum.repos.d/msprod.repo"
	#Installing tools
	sudo ACCEPT_EULA=Y yum install -y mssql-tools
	sudo yum -y install unixODBC-devel mysql postgresql
	#Installing Oracle
	sudo yum -y install resources/oracle-instantclient12.2-basic-12.2.0.1.0-1.x86_64.rpm
	sudo yum -y install resources/oracle-instantclient12.2-sqlplus-12.2.0.1.0-1.x86_64.rpm
	#Add symbolic links
	sudo sh -c "echo /usr/lib/oracle/12.2/client64/lib > /etc/ld.so.conf.d/oracle-instantclient.conf"
	sudo ldconfig
	;;
	RHEL)
	#Add MSSQL repositories
	echo "Adding MSSQL repositories"
	sudo su -c "curl https://packages.microsoft.com/config/rhel/7/prod.repo > /etc/yum.repos.d/msprod.repo"
	#Installing tools
	sudo ACCEPT_EULA=Y yum install -y mssql-tools
	sudo yum -y install unixODBC-devel mysql postgresql
	#Installing Oracle
	sudo yum -y install resources/oracle-instantclient12.2-basic-12.2.0.1.0-1.x86_64.rpm
	sudo yum -y install resources/oracle-instantclient12.2-sqlplus-12.2.0.1.0-1.x86_64.rpm
	#Add symbolic links
	sudo sh -c "echo /usr/lib/oracle/12.2/client64/lib > /etc/ld.so.conf.d/oracle-instantclient.conf"
	sudo ldconfig
	;;
    esac
}

#=== FUNCTION ==================================================================
# NAME: downloadJDBCDrivers
# DESCRIPTION: Download JDBC drivers required to configure the product.
# (mssql, mariadb, mysql, Oracle, postgresql clients)
# Note: Oracle client is received from resources directory (Since user-agreement
# -has to be done when downloading)
#===============================================================================
function downloadJDBCDrivers() {
	mkdir sql-drivers
	curl http://central.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/6.4.0.jre8/mssql-jdbc-6.4.0.jre8.jar --output sql-drivers/sqlserver-ex.jar
	curl https://downloads.mariadb.com/Connectors/java/connector-java-2.2.3/mariadb-java-client-2.2.3.jar --output sql-drivers/mariadb.jar
	curl http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.44/mysql-connector-java-5.1.44.jar --output sql-drivers/mysql.jar
	curl https://jdbc.postgresql.org/download/postgresql-42.2.1.jar --output sql-drivers/postgres.jar
	#Copy “ojdbc8.jar” from resources directory to sql-drivers directory
	mv resources/ojdbc8.jar sql-drivers/oracle-se.jar
	echo "JDBC drivers are copied to 'sql-drivers' directory."
	ls sql-drivers
	sudo mv sql-drivers /opt/testgrid/sql-drivers
}

#=== FUNCTION ==================================================================
# NAME: updatePathVariable
# DESCRIPTION: Update path variable with appending MSSQL tools & Oracle client
#===============================================================================
function updatePathVariable() {
	echo "updating path variable..."
    case "$AMI_OS" in
    Ubuntu)
	#Todo: verify the path update for Ubuntu.
	echo "WARN: Path variable is not properly updating for Ubuntu."
	#echo 'PATH=/opt/mssql-tools/bin:/usr/lib/oracle/12.2/client64/bin:$PATH' | sudo tee --append /etc/environment
	;;
    CentOS)
	sudo su -c "echo 'pathmunge /opt/mssql-tools/bin:/usr/lib/oracle/12.2/client64/bin' > /etc/profile.d/testgrid-libs.sh"
	sudo chmod +x /etc/profile.d/testgrid-libs.sh
	. /etc/profile
	;;
	RHEL)
	sudo su -c "echo 'pathmunge /opt/mssql-tools/bin:/usr/lib/oracle/12.2/client64/bin' > /etc/profile.d/testgrid-libs.sh"
	sudo chmod +x /etc/profile.d/testgrid-libs.sh
	. /etc/profile
	;;
	esac
}

#=== FUNCTION ==================================================================
# NAME: addEnvVariables
# DESCRIPTION: Adding environmental variables to JDKS
#===============================================================================
function addEnvVariables() {
    sudo su -c "echo 'ORACLE_JDK8=/usr/java/jdk1.8.0_181-amd64' > /etc/environment"
    sudo su -c "echo 'OPEN_JDK8=/usr/lib/jvm/java-1.8.0-openjdk-amd64' >> /etc/environment"
    source /etc/environment
}

#=== FUNCTION ==================================================================
# NAME: installTinkererAgent
# DESCRIPTION: Install TestGrid Tinkerer agent to AMI.
#===============================================================================
function installTinkererAgent() {
    #Download tinkererAgent from the last successful TestGrid build in WSO2 Jenkins.
    wget https://wso2.org/jenkins/job/testgrid/job/testgrid/lastSuccessfulBuild/artifact/remoting-agent/target/agent.zip
	sudo unzip agent.zip -d /opt/testgrid/
	rm agent.zip
	#Add testgrid-agent service
	sudo cp /opt/testgrid/agent/testgrid-agent /etc/init.d/
}

#=== FUNCTION ==================================================================
# NAME: installPerfMonitoringArtifacts
# DESCRIPTION: Install artifacts relates to performance monitoring to AMI.
#===============================================================================
function installPerfMonitoringArtifacts() {
    unzip resources/perf_monitoring_artifacts.zip -d .
    sudo cp -r perf_monitoring_artifacts/* /opt/testgrid/agent/
}

#=== FUNCTION ==================================================================
# NAME: installDependenciesForPython
# DESCRIPTION: Install necessary pre-requisites for Python installation.
#===============================================================================
function installDependenciesForPython() {
    echo "Installing dependencies for python"
	case "$AMI_OS" in
    Ubuntu)
	sudo apt-get install -y build-essential #Adding gcc which is dependency to build python
	sudo apt install -y zlib1g-dev		    #Dependency for python installing
	;;
    CentOS)
	sudo yum groupinstall -y "Development Tools"
	sudo yum install -y zlib-devel	        #Dependency for python installing
	;;
	RHEL)
	sudo yum groupinstall -y "Development Tools"
	sudo yum install -y zlib-devel	        #Dependency for python installing
	;;
    esac
    installPackage "bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel gdbm-devel db4-devel libpcap-devel xz-devel"
}

#=== FUNCTION ==================================================================
# NAME: installPython
# DESCRIPTION: Install Python (Download python from official FTP server and build)
#===============================================================================
function installPython() {
	python_version=$1	    #We can use any version available in python.org ftp server
	echo "Installing python version $python_version"
    wget https://www.python.org/ftp/python/$python_version/Python-$python_version.tgz
    tar -xvf Python-$python_version.tgz
    cd Python-$python_version
	./configure
    sudo make
	sudo make install
	cd ..
	sudo rm -r Python-$python_version
	sudo rm Python-$python_version.tgz
}

#=== FUNCTION ==================================================================
# NAME: installCfnSignalLibrary
# DESCRIPTION: Install CFN Signal Library (CFN Signal is used to inform status
#              of the instance to its CF Stack)
#===============================================================================
function installCfnSignalLibrary() {
    #Download lib
    wget https://s3.amazonaws.com/cloudformation-examples/aws-cfn-bootstrap-latest.amzn1.noarch.rpm
    #Installing python dependency for rpm installation
    sudo pip install pystache
    sudo pip install python-daemon
    #Installation
    sudo yum -y localinstall aws-cfn-bootstrap-latest.amzn1.noarch.rpm
    #To fix ImportError: No module named cfnbootstrap when executing Signal (Adding symbolic link)
    sudo ln -s /usr/local/lib/python2.7/site-packages/cfnbootstrap /usr/lib/python2.7/site-packages/cfnbootstrap
    ls /opt/aws/bin/cfn-signal
}

#=== FUNCTION ==================================================================
# NAME: installPythonArtifacts
# DESCRIPTION: Install necessary Python libs, tools with the installed Python.
#===============================================================================
function installPythonArtifacts() {
    echo "Installing Python artifacts"
    #Installing pip for sudoers
    #RHEL
    case "$AMI_OS" in
    Ubuntu)
	installPackage epel-release
    installPackage python-pip
    sudo pip install virtualenv #Necessary for WSO2 integration-test python repositories
	;;
    CentOS)
	installPackage epel-release
    installPackage python-pip
    sudo pip install virtualenv #Necessary for WSO2 integration-test python repositories
	;;
	RHEL)
	wget https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
    sudo yum install epel-release-latest-7.noarch.rpm -y

	installPackage epel-release
    installPackage python-pip
    sudo pip install virtualenv #Necessary for WSO2 integration-test python repositories
	;;
    esac
}

#=== FUNCTION ==================================================================
# NAME: showInstallations
# DESCRIPTION: Verify installations in the AMI.
#===============================================================================
function showInstallations() {
	echo "----git----"
	git --version
	echo "----python 3.6----"
	python3.6 -V
	echo
	echo "----python 3.5----"
	python3.5 -V
	echo
	echo "----maven----"
	mvn -v
	echo
	echo "----mysql (MySQL DB-Client)----"
	mysql --version
	echo
	echo "----sqlcmd (MSSQL DB-Client)----"
	sqlcmd | head -3
	echo
	echo "----sqlplus (Oracle DB-Client)----"
	sqlplus -version
	echo
	echo "----postgre (Postgre DB-Client)----"
	psql --version
	echo
	echo "----JDKs (all versions)----"
    case "$AMI_OS" in
    Ubuntu)
	sudo update-alternatives --list java
	;;
    CentOS)
	sudo update-alternatives --display java #list is only working in ubuntu.
	;;
	RHEL)
	sudo update-alternatives --display java #list is only working in ubuntu.
	;;
    esac
	echo
	echo "----PATH variable----"
	echo $PATH
	echo
	echo "----agent directory (/opt/testgrid/)----"
	ls /opt/testgrid/agent/
	echo
	echo "----TestGrid agent service"
	service --status-all |grep TestGrid
	echo
	echo "----JDBC drivers directory (/opt/testgrid/sql-drivers)----"
	ls /opt/testgrid/sql-drivers
	echo
	echo "----Environment Variables----"
	echo OracleJDK8 env= $ORACLE_JDK8
	echo OpenJDK8 env=$OPEN_JDK8
	echo
	echo "---- CFN Library ----"
    ls /opt/aws/bin/cfn-signal
    echo
}

case "$AMI_OS" in
    Ubuntu)
	showMessage "Starting Ubuntu configuration steps.."
        ;;
    CentOS)
	showMessage "Starting CentOS configuration steps.."
        ;;
    RHEL)
	showMessage "Starting RHEL configuration steps.."
        ;;
    Windows)
	showMessage "Windows configurations steps are not added yet."
	exit;
        ;;
    *)
        echo "$AMI_OS is not a valid choice."
	exit;
        ;;
esac

echoLogo;
sudo mkdir /opt/testgrid;
sudo mkdir /opt/wso2;
showMessage "1.Installing JDKs"
installJDKs;
showMessage "2.Installing SQL-clients and Tools"
installSQLclientsAndTools;
showMessage "3.Downloading JDBCDrivers"
downloadJDBCDrivers;
showMessage "4.Installing Maven"
installMaven
showMessage "5.Installing Git"
installGit
showMessage "6.Updating PathVariable"
updatePathVariable;
showMessage "7.Installing Tinkerer agent"
installTinkererAgent
showMessage "8.Installing Dependencies For Python"
installDependenciesForPython
showMessage "9.Installing Python"
installPython 3.5.6 #3.5 version is necessary for CFN-Signal library
installPython 3.6.7 #3.6 version is necessary for WSO2 integration-test python repositories
installPythonArtifacts
showMessage "9.Installing Performance monitoring artifacts"
installPerfMonitoringArtifacts
showMessage "10.Installing CFN-Signal library"
installCfnSignalLibrary
showMessage "10. Adding environment variables"
addEnvVariables
showMessage "11.Showing installations"
showInstallations
sudo rm -f -r *
