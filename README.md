[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=testgrid/testgrid)](https://wso2.org/jenkins/job/testgrid/job/testgrid/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# WSO2 TestGrid

Welcome to main repo of WSO2 TestGrid.


TestGrid strengthens the positioning of WSO2 products and adds major value to the subscriptions WSO2 offers. Following are some benefits TestGrid provide:

1. TestGrid tests entire feature-set of our products (APIM/IS/EI/SP/IOT) against a wide-array of supported infrastructure combinations. (We are not there yet!) 
(In essence, we add value to what we have claimed at [1], [2], and more.)

> _User asks_  : Does WSO2 IS support IBM JDK 8 with DB2 database on AIX operating system?

> _WSO2 response_ : IS has been tested exactly against this set of combinations, and is proven to work. You can find the current status of this infrastructure combination in the WSO2 TestGrid's dashboard at http://testgrid-live.wso2.com/.

2. Users get to validate their WSO2 deployments thru the scenario test support we provide. 

3. Users get to see a document with a set of hypothetical user stories each having scenario test scripts. Each scenario test script will test for minor configuration variations (like caching enabled/disabled). This document will provide a single source of truth for user stories.
Currently, we only have a crude list that simply detail names and descriptions though. [3]

------------------------
Current TestGrid-Live dashboard can be found here - https://testgrid-live.private.wso2.com/

Once we have the TestGrid beta,
* You will find the TestGrid home page here - https://testgrid.wso2.com
* You will find the TestGrid Live dashboard here - https://testgrid-live.wso2.com
------------------------

[1] https://docs.wso2.com/display/compatibility/Tested+DBMSs

[2] https://docs.wso2.com/display/compatibility/Tested+Operating+Systems+and+JDKs

[3] https://github.com/wso2-incubator/identity-test-integration/blob/master/README.md


## Getting Started with WSO2 TestGrid

The [Quick Start Guide](docs/QuickStartGuide.md) contains end-to-end steps on getting started and running WSO2  TestGrid.