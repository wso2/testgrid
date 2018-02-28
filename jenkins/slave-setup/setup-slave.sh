#!/bin/bash
###########################################################
#
# k8s on OpenStack Preparation Script
#
# Run this script with changes requested in the README.md
#
###########################################################

#Variables
JENKINS_SERVER=server-ip-or-hostname
JENKINS_MASTER_URL=jenkins-master-url

let tries=1

# Function to prepare nodes for
function prepareNode() {

	sudo apt-get update
	sudo apt-get install unzip

	echo "Installing terraform"
	wget https://releases.hashicorp.com/terraform/0.11.3/terraform_0.11.3_linux_amd64.zip
	unzip terraform_0.11.3_linux_amd64.zip
	sudo mv terraform /usr/local/bin/

	echo "Installing ansible"
	sudo apt-get install software-properties-common
	sudo apt-add-repository ppa:ansible/ansible
	sudo apt-get update
	sudo apt-get install ansible

	echo "Installing jinja2"
	sudo apt-get install python-pip
	sudo pip install https://pypi.python.org/packages/2.7/J/Jinja2/Jinja2-2.8-py2.py3-none-any.whl

	echo "Installing Ruby"
	gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3 7D2BAF1CF37B13E2069D6956105BD0E739499BDB
	curl -sSL https://get.rvm.io -o rvm.sh
	cat rvm.sh | bash -s stable
	source ~/.rvm/scripts/rvm
	rvm install ruby --default

	echo "Installing python net-addr"
	sudo apt-get install python-netaddr

	echo "Installing kubectl"
	curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.8.2/bin/linux/amd64/kubectl
	chmod +x ./kubectl
	sudo mv ./kubectl /usr/local/bin/kubectl

	echo "Installing git"
	sudo apt-get install git

	echo "Installing Java"
	sudo add-apt-repository ppa:webupd8team/java
	sudo apt-get update
	sudo apt-get install oracle-java8-installer

	echo "Installed versions"
	terraform --version
	ansible --version
	pip show Jinja2
	ruby -v
	kubectl version
	java -version

	echo "Generate SSH keys"
	ssh-keygen
	cat ~/.ssh/id_rsa.pub

	#Prepare for jenkins slave
	echo "Adding testgrid-live public certificate to slave keystore"
	openssl s_client -connect "$JENKINS_SERVER":443 < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > prod-cert.crt
	$JAVA_HOME/bin/keytool -import -alias testgrid-live.private.wso2.com -keystore $JAVA_HOME/jre/lib/security/cacerts -file prod-cert.crt

	sudo mkdir /testgrid
	sudo chown ubuntu:ubuntu /testgrid
	mkdir /testgrid/jenkins-home
	wget https://"$JENKINS_MASTER_URL"/jnlpJars/slave.jar --no-check-certificate
	echo "Run headless slave launch command given in jenkins master"

}

while getopts ":r:" opt; do
  case $opt in
    r)
      echo "$OPTARG retries triggered." >&2
      tries=$(($tries + $OPTARG))
      while ((tries>0)) ; do
	    prepareNode
	    ((tries-=1))
      done
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done
