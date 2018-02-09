# Infrastructure / Deployment / and Scenarios Repository Structure - External consumers' view

This document describes the external consumer's view of the above mentioned repositories. Testgrid is one such consumer.

Overall, the testgrid test-run consists of three steps:

1. [Provisioning infrastructure](#provisioning-infrastructure) (infra-provision.sh)
2. [Deployment of products](#deploying-products) (deploy.sh)
3. [Running scenario tests](#running-scenario-tests) (run-scenarios.sh)

## Provisioning infrastructure

This provisions the infrastructure needed to deploy WSO2 products. In case
of bare-metal infrastructure provider like AWS, this is about provisioning
EC2 instances, RDS databases, configuring ALB etc. In case of container
orchestration engines such as Kubernetes, this is about provisioning the
underlying infrastructure provider (say, GCP) and deploying a kubernetes cluster
on top of that.

To provision an infrastructure, Testgrid will invoke an Infrastructure
as Code (IaC) template thru a shell script **`infra-provision.sh`**.
Product teams need to provide a
git repository that contain IaC template. For proper function of Testgrid, it
expects that each infrastructure repository provide a metadata file that
describes the repository. This file need to contain following information:

1. Infrastructure as Code (IaC) provider : CloudFormation / Terraform / ...
2. Supported infrastructure provider    : AWS / Azure / GCP / ...
3. Container ochestration engine (if applicable) : Kubernetes / Swarm / ...

Ideally, one repository need to contain only one IaC script. If more are
needed, then, those need to be mentioned as appropriate in the metadata file.

The format of this metadata may be as follows. Actual format may get
changed depending on the input from the Installation Experience Team.

**WSO2Infrastructurefile:**
```yaml
WSO2Infrastructurefile:
  IACProvider: <IACProvider>
  supportedInfrastructureProviders: <InfrastructureProvider>
  ContainerOrchestrationEngine: <ContainerOrchestrationEngine> (optional)

```

```
IACProvider enum {
  CloudFormation,
  Terraform,
  ...
}
```

```
InfrastructureProvider enum {
  AWS,
  Azure,
  GCP,
  OpenStack
}
```

```
ContainerOrchestrationEngine enum {
  N/A,
  Kubernetes,
  DockerSwarm,
  DockerCompose
}
```

Example WSO2Infrastructurefile:
```yaml
WSO2Infrastructurefile:
  IACProvider: Terraform
  supportedInfrastructureProviders: OpenStack
  ContainerOrchestrationEngine: Kubernetes
```

Above is merely a list of metadata that describes the repo. Testgrid would depend
on these when invoking the infra-provision.sh. Please note that the actual format and
syntax may get changed depending on input from the Installation Experience Team.

The infrastructure provisioning is orthogonal to product deployment. Hence, it
should not know what product or deployment pattern is going to be deployed into
this infrastructure. But, this raises the question: How can we provision
an infrastructure without knowing number of VM instances required? Read on to
the next section to find out. :-)

## Deploying products

Next step is to deploy WSO2 products in the provisioned infrastructure.
WSO2 provides a set of deployment patterns that can be used for this purpose.
The products can be deployed via a configuration mgt tool like Ansible/Puppet.

The product teams need to include a **`deploy.sh`** at the root of the respective
git repository that takes care of invoking the deployment scripts. In addition,


### WSO2Deployfile
This is syntax of the WSO2Deployfile:

```
WSO2Deployfile:
  deploymentPatterns:
    - name: <string>
      description: <string>
      dir: <string>
      inputsForInfrastructurefile:
          - requiredVMCount: <int>
          - ...
    - ...
    - ...
  InputParameters:
    - name: <string>
      type: <Type>
    - ...

```

```
Type enum {
 string,
 int,
 list
}
```

Example WSO2Deployfile:
```
WSO2Deployfile:
  deploymentPatterns:
    - name: 01-two-node-deployment
      description: Deploys a two node IS cluster
      dir: patterns/01-two-node-deployment
      inputsForInfrastructurefile:
          - requiredVMCount: 2
  InputParameters:
    - name : hostNames
      type : <list>
    - name : sshKey
      type : <string>
```

Note: the metadata under _inputsForInfrastructurefile_ element are fed into
the infrastructure provisioning script. This needs to be done because even though
the infrastructure and WSO2 product deployment are orthogonal, there is some-level
of coupling between the two. One good example is the number of VM machines required
for a given deployment pattern. But, this should not, however, specify any
infrastructure _provider_ specific parameters.


## Running scenario tests

When the deployment is completed, we can start running scenario tests against that.
The entrypoint for scenario repos is **run-scenarios.sh** script along with
**WSO2Scenariosfile** that contain a list of metadata about this repository.

### Scripts
#### init.sh
This script will run before all the scenario tests.
This is intended to do a set of common tasks (such as tenant creation) that is
applicable for all scenarios.

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
This can be used to delete the tenants created, remove any populated databases etc.

### WSO2Scenariosfile

This is a metadata file that describes a scenario repository. The syntax will be
as follows:


#### WSO2Scenariofile v1:

Until config-value-sets come into the picture, we can maintain a configuration like following to let TestGrid know what scenarios are ready:
```yaml
scenarios:
  - name: <string>
    dir: scenario<string>
  - ...

```


#### WSO2Scenariofile v2:

```yaml
scenarios:
  - name: scenario<string>
    dir: scenario<string>
  - ...
scenariosDir: <string>    <-- (optional)
config-change-sets:
  - name: config<string>
    description: config<string>
    applies-to:
      - scenario<string>
      - ...
  - name: config<string>
      description: config<string>
      excluded-from:
        - scenario<string>
        - ...
```

NOTE: 'applies-to' and 'excluded-from' are mutually exclusive.
If neither are specified, all scenarios will be run for that config changeset

'scenarios' and 'scenariosDir' are mutually exclusive. You can either list all
scenarios one-by-one or point to the directory that contains all the scenarios.

Example WSO2Scenariofile:
```yaml
scenarios:
  - name: scenario01
    description: Tests Identity Server's SAML SSO usecases.
    dir: scenarios/scenario01
  - ...
config-change-sets:
  - name: config01
    description: applies the cache-enabling config changeset into a deployment.
    applies-to:
      - scenario01
      - scenario10
  - name: config02
    description: applies the facebook authenticator into a deployment.
    excluded-from:
      - scenario20
```
#### Deploy Config Value Sets:
There will be different config value sets which have been identified as prerequisites for the one or more scenarios. These product level config changes and required artifacts are stored under config-sets directory in the integration-tests repository. Inside the each config directories there are steps mentioned as in shell scripts to deploy configs. 
TG will generate an archive for each config directory and copy it to all the server instances and extract it and then run the entrypoint(shell script file: **config.sh**) which is in the archived directory. 

More information on how these automation scripts are run in test grid and how you can test them locally can be found in the [solution-test-toolkit docs](https://github.com/wso2-incubator/solution-test-toolkit).

In summary, we discussed execution scripts and metadata configuration files here.
Following chart describes everything under one place:

| Repository type | File          |  Description |
| -------------   |:------------- | :------------|
| Infrastructure  | infra-provision.sh     | Shell script that provisions an infrastructure. |
|                 | WSO2Infrastructurefile | Metadata file that describe the repository |
| Deployment      | deploy.sh              | Shell script that deploy a product    |
|                 | WSO2Deployfile          | Metadata file that describe the repository    |
| Scenarios       | run-scenarios.sh        | Shell script at root of the repository that executes all scenarios    |
|                 | run-scenario.sh         | Shell script within a scenario dir that executes the scenario    |
|                 | base-setup.sh           | Shell script that is run before all other scenarios    |
|                 | cleanup.sh              | Shell script that is run after all other scenarios    |
|                 | WSO2Scenariofile        | Metadata file that describe the repository   |


