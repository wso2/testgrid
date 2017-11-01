/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.testgrid.infrastructure;

import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.TestScenario;

/**
 * Created by harshan on 10/30/17.
 */
public class InfrastructureProviderService {

    boolean getScript(String repo) throws TestGridInfrastructureException {
        return false;
    }

    public Deployment createTestEnvironment(TestPlan scenario) throws TestGridInfrastructureException {
        return null;
    }

    public boolean removeTestEnvironment(TestScenario scenario) throws TestGridInfrastructureException {
        return false;
    }
}
