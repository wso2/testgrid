# How to Pass Data to Next Steps
Testgrid has a three-step process to validate
WSO2 product deployments:

1. [Provision Infrastructure](#infrastructure)
2. [Create Product deployment](#deploying-products)
3. [Execute (Intg/Scenario/Perf/Long Running/Chaos) test scripts](#running-scenario-tests)

These three steps run sequentially using the information received from the
previous step. For example, the scenario test scripts need to receive the
information about the deployment from the 'deployment-creation' step.
In this guide, we're going to describe how this data passing can happen.

The data passing to next steps will happen via data buckets. The data buckets
are modeled as file-system directories to provide a simplified abstract design.
This can be diagrammed as below:

![testgrid data buckets](data-buckets-based-data-passing-testgrid.png)

