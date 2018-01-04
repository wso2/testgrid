## Dummy Scripts to Test TG execution

This contains scripts to create infrastructure on AWS, deploy a tomcat server and test with Jmeter. 

1. First we need to install AWS CLI and configure, Following are some sample configs. These can be parsed as environment variables as well.

````
AWS Access Key ID [****************QMZQ]: 
AWS Secret Access Key [****************dV1K]: 
Default region name [us-east-1]: us-east-1
Default output format [None]: json
````
After configuring AWS CLI infra-provision.sh can be executed.

2. Next we can execute deploy.sh. For executing the deploy.sh Ansible should be installed and configured.

3. Last we can execute run-scenario.sh. When the scenario-run.sh is executing Jmeter should be set up and the Path should be added.
e.g : PATH=/home/yasassri/soft/Jmeter/apache-jmeter-3.2/bin

