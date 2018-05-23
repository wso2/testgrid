## Executing local Identity Server Deployment

### Prerequisites
1. Inorder to execute the Testgrid executable you should have a mysql setup running locally with username/password, root/root.
2. The local machine should have curl and unzip installed.
3. Java should be installed and configured. JAVA_HOME env variable must be set.
4. JMeter should be installed and JMETER_HOME env variable must be set to installed directory. (ex: "home/user/softwares/apache-jmeter")

### Steps to execute

1. Export the TESTGRID_HOME environment variable to local-is-deployment directory e.g: export TESTGRID_HOME=/home/yasassri/WORK/TG/code/testgrid/test-scripts/local-is-deployment

2. Copy the executable Jar along with libs to TESTGRID_HOME

3. Execute the generate-test-plan command: 
````
./testgrid generate-test-plan -p IS --file job-config.yaml
````
3. Now Open testgrid/test-scripts/local-is-deployment/DeploymentPatterns/pattern-2/deploy.sh and add your WSO2 credentials to download IS distribution or you can comment out this line and place a WSO2-is-5.4.0.zip at TESTGRID_HOME.

4. Execute the run-testplan command:
````
./testgrid run-testplan -p IS --file jobs/IS/test-plans/test-plan-01.yaml
``````