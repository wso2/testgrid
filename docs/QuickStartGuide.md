# TestGrid Quick Start Guide

This provides the required steps in detail on how to run TestGrid.

## Prerequisites

1. Download testgrid distribution from [https://wso2.org/jenkins/job/testgrid/job/testgrid/lastSuccessfulBuild/artifact/distribution/target/WSO2-TestGrid.zip](https://wso2.org/jenkins/job/testgrid/job/testgrid/lastSuccessfulBuild/artifact/distribution/target/WSO2-TestGrid.zip)
2. Extract **_WSO2-TestGrid.zip_** and copy [mysql connector](http://central.maven.org/maven2/mysql/mysql-connector-java/6.0.6/mysql-connector-java-6.0.6.jar) .jar file to `WSO2-TestGrid/lib` folder
3. Set _TESTGRID_HOME_ environment variable to a desired folder
4. Make sure _JAVA_HOME_ is set
5. Create **_$TESTGRID_HOME/config.properties_**

- _config.properties_ currently supports the following set of properties:
   
   ```properties
   #database configurations
   DB_URL
   DB_USER
   DB_USER_PASS
   
   #wum credentials
   WUM_USERNAME
   WUM_PASSWORD
   
   #AWS credentials and configurations
   AWS_REGION_NAME
   secretKey
   accessKey

   ```
6. Create MySQL database **_testgriddb_** <br>
      _**Note:** If you are using a remote mysql server define the following properties in $TESTGRID_HOME/config.properties_
      
      ```properties
      DB_URL=<REMOTE_MYSQL_DATABASE_URL> // e.g. jdbc:mysql://192.168.89.22:3306/testgriddb  
      DB_USER=<DATABASE_USER>
      DB_USER_PASS=<PASSWORD_FOR_DB_USER>
      ```
7. Generate table **_infrastructure_parameter_** with a default entry using the following command.
    ```
    DROP TABLE IF EXISTS `infrastructure_parameter`; 
    
    CREATE TABLE `infrastructure_parameter` (
      `id` varchar(36) NOT NULL,
      `created_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `modified_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `NAME` varchar(255) DEFAULT NULL,
      `PROPERTIES` varchar(255) DEFAULT NULL,
      `ready_for_testgrid` tinyint(1) DEFAULT '0',
      `TYPE` varchar(255) DEFAULT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `UNQ_infrastructure_parameter_0` (`NAME`)
    ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
    
    INSERT INTO `infrastructure_parameter` VALUES ('1bd03ead-84ff-4bfc-b796-8026d8849c3','2018-02-01 12:09:40','2018-02-01 12:09:40','ORACLE_JDK8','{}',1,'JDK'),
    ('1bd03ead-84ff-4bfc-b796-8026d8849c4','2018-02-01 12:09:32','2018-02-01 12:09:32','5.6.35','{}',1,'DBEngineVersion'),
    ('1bd03ead-84ff-4bfc-b796-8026d8849c5','2018-02-01 12:09:36','2018-02-01 12:09:36','mysql','{}',1,'DBEngine'),
    ('1bd03ead-84ff-4bfc-b796-8026d8849c6','2018-02-20 11:20:19','2018-02-22 04:16:54','UBUNTU','{}',1,'OS');
    
    ```
8. Identify and clone the repositories for infrastructure provisioning, deployment creation and scenario tests. <br>
    E.g: _For running scenario tests for IS 5.4.0 on AWS with cloudformation the repositories are as follows._
    	
    	infrastructure repository: https://github.com/wso2/cloudformation-is
    	deployment repository: https://github.com/wso2/cloudformation-is
    	scenario repository: https://github.com/wso2-incubator/identity-test-integration
    	
9.  Create testgrid.yaml. <br>
       E.g: _A sample for IS 5.4.0 with cloudformation is given below._
       
       ```yaml
        # TestGrid configuration file.
        version: '0.9'
        infrastructureConfig:
           iacProvider: CLOUDFORMATION
           infrastructureProvider: AWS
           containerOrchestrationEngine: None
           parameters:
            - JDK : ORACLE_JDK8
           provisioners:
            - name: 01-two-node-deployment
              description: Provision Infra for a two node IS cluster
              dir: cloudformation-templates/pattern-1
              scripts:
               - name: infra-for-two-node-deployment
                 description: Creates infrastructure for a IS two node deployment.
                 type: CLOUDFORMATION
                 file: pattern-1-with-puppet-cloudformation.template.yml
                 inputParameters:
                    parseInfrastructureScript: false
                    region: us-east-1
                    DBPassword: "DB_Password"
                    EC2KeyPair: "ec2-key"
                    ALBCertificateARN: "arn:aws:acm:us-east-1:40648900521:certificate/2ab5fegt-5df1-4219-9f7e-91639ff8064e"
        
        scenarioConfig:
         scenarios:
           -
             name: scenario02
             description: 'Multiple login options by service provider'
             dir: scenario02
           -
             name: scenario12
             description: 'Claim Mapper with Service Provider Travelocity and Identity Provider Facebook and Google'
             dir: scenario12
           -
             name: scenario18
             description: 'Fine-grained access control for service providers'
             dir: scenario18
           -
             name: scenario21
             description: 'Enforce users to provide missing required attributes while getting JIT provisioned to the local system'
             dir: scenario21
           -
             name: scenario28
             description: 'Home realm discovery'
             dir: scenario28         
    ```
10. Create job-config.yaml in a desired place containing absolute paths to infrastructure, deployment and scenario scripts in above cloned repos. 
<br>
E.g: IS 5.4.0 with cloudformation

   ```yaml
    infrastructureRepository: "/home/ubuntu/cloudformation-is/cloudformation-templates/pattern-1"
    deploymentRepository: "/home/ubuntu/cloudformation-is/cloudformation-templates/pattern-1"
    scenarioTestsRepository: "/home/ubuntu/identity-test-integration"
    testgridYamlLocation: "/home/ubuntu/testgrid.yaml"
    relativePaths: false
   ```
    
## Running TestGrid

Execution of testGrid contains 4 commands to be executed sequentially in the following order. <br>
Change directory to `<TESTGRID_DISTRIBUTION_LOCATION>`

#### Command #1 - generate-test-plan 
This will generate the test plans for each infrastructure combination in `$TESTGRID_HOME/jobs/<PRODUCT>/test-plans/test-plan-*.yaml`
  

```bash
./testgrid generate-test-plan \
        --product <PRODUCT> \
        --file ${JOB_CONFIG_YAML_PATH}
```
E.g:<br>
  _./testgrid generate-test-plan --product WSOIS-5.4.0  --file $TESTGRID_HOME/jobs/wsois-5.4.0/job-config.yaml_
  
  
#### Command #2 - run-testplan

This command will create the infrastructure specified in the test-plan-file, deploy the product and  run scenario tests.
  
  ```bash
  ./testgrid run-testplan \
         --product <PRODUCT> \
         --file <test-plan-file>"
  ```
  
  E.g: <br> 
  _./testgird run-test-plan --product WSOIS-5.4.0 --file $TESTGRID_HOME/jobs/WSOIS-5.4.0/test-plans/test-plan-01.yaml_

The complete details of test results are available at `$TESTGRID_HOME/<PRODUCT>/<DEPLOYMENT_PATTERN>/<INFRA_PARAMETER_ID>/<LATEST_NUMBER>/test-run.log`


#### Command #3 - finalize-run-testplan
This will finalize erroneous states in test plans (if any)

```bash
./testgrid finalize-run-testplan \
      --product <PRODUCT> 
```
#### Command #4 - generate-report

This generates the final reports with test statuses. The generated report can be found at `$TESTGRID_HOME/<PRODUCT>/<PRODUCT>-<TIMESTAMP>-SCENARIO.html`

```bash
./testgrid generate-report \
      --product <PRODUCT> \
      --groupBy scenario
```