# Infrastructure / Deployment / and Scenarios Repository Structure - Testgrid's perspective

This document describes what Testgrid need to know from the repositories
it is working on. Testgrid require three distinct type of repositories
for its three-step WSO2 product deployment validation process:

1. [Infrastructure provisioning repositories](#infrastructure-provisioners)
2. [Product deployment repositories](#deploying-products)
3. [Scenario test script provider repositories](#running-scenario-tests)

## Infrastructure provisioners

To validate the WSO2 product deployment, we first need to provision the
required infrastructure. This is an optional step in the WSO2 product
deployment validation process. Alternatively, users directly jump into
the second step by providing the details of an already provisioned
infrastructure.

What does it mean by infrastructure provisioning? In case of bare-metal
infrastructure provider like AWS, this is about provisioning
EC2 instances, RDS databases, configuring application load balancer etc.
In case of container
orchestration engines such as Kubernetes, this is about provisioning the
underlying infrastructure provider (say, Google Cloud or OpenStack) and
deploying a kubernetes cluster on top of that.  This is a pre-requisites for
a WSO2 product deployment

To provision an infrastructure, Testgrid will first read a metadata file
called **testgrid.yaml** that needs to be placed in the root of the repository.
Then, it'll invoke whatever the 'CREATE' scripts mentioned in the _testgrid.yaml_.
ATM, testgrid support any infrastructure provisioner thru Bash/Shell scripts.

It has specialized support for invocation of AWS Cloudformation scripts, and k8s
provisioners (thru Kubespray/Kargo) out of the box. This is necessary to pass-in
credentials and other specialized input parameters. For example, it
support feeding in the AWS access keys from a Testgrid managed password vault.
Nevertheless, it supports any Infrastructure as Code providers including
CloudFormation and Terraform.

##### What a product team need to do?
Product teams need to provide a git repository that contain IaC templates
along with the testgrid.yaml metadata file.

This file need to contain following information:

1. Infrastructure as Code (IaC) provider : CloudFormation / Terraform / ...
2. Supported infrastructure provider    : AWS / Azure / GCP / ...
3. Container orchestration engine (if applicable) : Kubernetes / DockerSwarm / ...

Ideally, one repository need to contain only one IaC script. If more are
needed, then, those need to be mentioned as appropriate in the metadata file.

The format of this metadata may be as follows. Actual format may get
changed depending on the input from the Installation Experience Team.

**testgrid.yaml :: infrastructureConfig:**
```yaml
infrastructureConfig:
  iacProvider: <string {CloudFormation, Terraform, None}>
  infrastructureProvider: <string {AWS, OpenStack, SHELL}>
  containerOrchestrationEngine: <string {K8S, DockerSwarm, None}> (optional)
  provisioners:
    - name: 0X-<string>
      description: <string>
      scripts:
        - name: <string>
          type: <string {SHELL, CloudFormation}>
          phase: <string {CREATE, DEPLOY, DESTROY}>
          file: <string>
          inputParameters:
```

Example testgrid.yaml for infrastructure repos:
```yaml
infrastructureConfig:
  iacProvider: CloudFormation
  infrastructureProvider: SHELL
  containerOrchestrationEngine: None
  provisioners:
    - name: 01-aws-infra-provisioner
      description: Provision Infra for IS product
      dir: .
      scripts:
        - name: infra-for-aws-is-deployment
          description: Creates infrastructure for IS deployment.
          type: SHELL
          phase: CREATE
          file: infra-provision.sh
          inputParameters:
            - awsRegion: us-east-1
        - name: destroy
          file: infra-destroy.sh
          type: SHELL
          phase: DESTROY
```

Above is merely a list of metadata that describes the repo. Testgrid would depend
on this to know what to do.

The infrastructure provisioning is orthogonal to product deployment. Hence, this
should not know what product or deployment pattern is going to be deployed into
this infrastructure. But, this raises the question: How can we provision
an infrastructure without knowing number of VM instances required? Read on to
the next section to find out. :-)

## Deploying products

Now that the infrastructure provisioning is done, the
next step is to deploy WSO2 products in this infrastructure.
The products can be deployed via a configuration mgt tool like Ansible/Puppet.

The product teams need to include a metadata file named **testgrid.yaml**
that describe how to run the repo. This file (testgrid.yaml) needs to be placed
at the root of the deployment repository. This configuration takes the following
format:

### testgrid.yaml :: deploymentConfig

```yaml
deploymentConfig:
  deploymentPatterns:
    - name: <string>
      description: <string>
      dir: <string>
      scripts:
        - type: <string>
          description: <string>
          file: </path/to/deploy.sh>
          name: <string>
          phase: <string {CREATE, DEPLOY, DESTROY}>
          inputParameters:

```

Example testgrid.yaml for deployment repos:
```yaml
deploymentConfig:
  deploymentPatterns:
    - name: 01-testgrid-is-deployment
      description: Deploys an IS node in AWS
      dir: .
      scripts:
        - type: SHELL
          file: "deploy.sh"
          name: "deploy"
          phase: CREATE
```

<!-- Note: the metadata under _inputsForInfrastructurefile_ element are fed into -->
<!-- the infrastructure provisioning script. This needs to be done because even though -->
<!-- the infrastructure and WSO2 product deployment are orthogonal, there is some-level -->
<!-- of coupling between the two. One good example is the number of VM machines required -->
<!-- for a given deployment pattern. But, this should not, however, specify any -->
<!-- infrastructure _provider_ specific parameters. -->


## Running scenario tests

This is the third and final step of the product deployment verification.
Once the deployment is completed, Testgrid starts to run scenario tests under the
scenario repository against this deployment.

Testgrid get to know how to execute the scenario test scripts by looking at the
_scenarioConfig_ fragment in a metadata file called **testgrid.yaml**. Following
is the testgrid's view of the scenario test repositories.

### Scripts
The scenario test repository may contain following files:

#### init.sh
This script will run before all the scenario tests.
This is intended to do a set of common tasks (such as tenant creation) that is
applicable for all scenarios. This should be specified with the phase *CREATE* in testgrid.yaml.

Scenario tests might need additional infrastructure other than the product deployment such as a tomcat deployment or an MSF4J service. 
These are facilitated by TestGrid through `init.sh`.

#### run-scenarios.sh
This script will invoke the **`init.sh`**, and then
**`run-scenario.sh`** (note the singular form) of each scenario sequentially.

#### run-scenario.sh
There will be one run-scenario.sh file for each scenario.
This script will do the following:

1. Invoke any pre- processing shell scripts (`0X-pre-scenario-steps.sh`)
2. Invoke jmeter/testng/... scripts (`0X-<script-name>.jmx`)
3. Invoke post- processing shell scripts. (`0X-post-scenario-steps.sh`)

#### cleanup.sh
This is last script that will run after the execution of all scenario tests.
This can be used to destroy the additional infrastructure created in `init.sh`, delete the tenants created, remove any populated databases etc.
This should be specified with the phase *DESTROY* in testgrid.yaml

### testgrid.yaml :: scenarioConfig

This is a metadata file that describes a scenario repository. The syntax will be
as follows:


#### The testgrid.yaml for scenario test configuration v1:

Until config-value-sets come into the picture, we can maintain a configuration
like following to let TestGrid know what scenarios are ready:
```yaml
scenarioConfig:
  scenarios:
    - name: <string>
      description: <string>
      dir: "</dir/to/scenario>"
    -

```

#### The testgrid.yaml for scenario test configuration v2:

```yaml
scenarioConfig:
    scenarios:
      - name: <string>
        description: <string>
        dir: scenario<string>
      - ...
    scenariosDir: <string>    <-- (optional)
    configChangeSets:
      - name: config<string>
        description: <string>
      - name: config<string>
        description: config<string>
```

#### The testgrid.yaml for scenario test configuration v3:

```yaml
scenarioConfig:
    scenarios:
      - name: <string>
        description: <string>
        dir: scenario<string>
      - ...
    scenariosDir: <string>    <-- (optional)
    configChangeSets:
      - name: config<string>
        description: <string>
      - name: config<string>
        description: config<string>
    scripts:
      -
        file: <name of infra creation file. eg. init.sh>
        phase: CREATE
      -
        file: <name of infra destruction file. eg. cleanup.sh>
        phase: DESTROY
```


'scenarios' and 'scenariosDir' are mutually exclusive. You can either list all
scenarios one-by-one or point to the directory that contains all the scenarios.

Example testgrid.yaml for v3 of scenario test repos:
```yaml
scenarioConfig:
  scenarios:
    - name: scenario01
      description: Tests Identity Server's SAML SSO usecases.
      dir: scenarios/scenario01
    - ...
  configChangeSet:
    - name: config01
      description: applies the cache-enabling config changeset into a deployment.
    - name: config02
      description: applies the facebook authenticator into a deployment.
  scripts:
    -
      file: init.sh
      phase: CREATE
    -
      file: cleanup.sh
      phase: DESTROY
```
##### Deploy Config Change Sets:
Sometimes, we have a need to run the same scenario test scripts but with
different server configuration. There are few usecases for this:

* We need to run the same set of tests with and without caching enabled.
* Some scenario tests require configuration changes to the server before
they are executed.

We have followed a straight-forward approach to address this problem.
The scenario test writers can place these product-level config change
sets under the config-sets (or change-sets) directory in the scenario tests repository.
Test writers can place any artifact
and files under this directory. Only requirement from testgrid side is to
have an entrypoint into this change-set, say, a shell script named **apply-config.sh**.
Then , during a test-run, Testgrid will archive the change-set content, upload it to each
server and execute the entrypoint script: _apply-config.sh_.

More information on how these scenario test scripts are run in Testgrid and
how you can test them locally can be found in the
[solution-test-toolkit docs](https://github.com/wso2-incubator/solution-test-toolkit).

## Summary

In summary, we discussed execution scripts and metadata configuration files here.
Following chart describes everything under one place:

| Repository type | File          |  Description |
| -------------   |:------------- | :------------|
| Infrastructure  | infra-provision.sh     | Shell script that provisions an infrastructure. |
|                 | testgrid.yaml :: (infrastructureConfig fragment) | Metadata file that describe the repository |
| Deployment      | deploy.sh               | Shell script that deploy a product    |
|                 | testgrid.yaml :: (deploymentConfig fragment)          | Metadata file that describe the repository    |
| Scenarios       | run-scenarios.sh        | Shell script at root of the repository that executes all scenarios    |
|                 | run-scenario.sh         | Shell script within a scenario dir that executes the scenario    |
|                 | base-setup.sh           | Shell script that is run before all other scenarios    |
|                 | cleanup.sh              | Shell script that is run after all other scenarios    |
|                 | testgrid.yaml :: (scenarioConfig fragment)        | Metadata file that describe the repository   |
