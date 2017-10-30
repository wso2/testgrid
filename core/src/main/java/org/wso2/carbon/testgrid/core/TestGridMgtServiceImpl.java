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

package org.wso2.carbon.testgrid.core;

import org.wso2.carbon.testgrid.automation.TestEngine;
import org.wso2.carbon.testgrid.automation.TestEngineException;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.common.TestScenarioStatus;
import org.wso2.carbon.testgrid.common.config.SolutionPattern;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;
import org.wso2.carbon.testgrid.common.exception.TestGridException;
import org.wso2.carbon.testgrid.deployment.DeployerService;
import org.wso2.carbon.testgrid.deployment.TestGridDeployerException;
import org.wso2.carbon.testgrid.infrastructure.InfrastructureProviderService;
import org.wso2.carbon.testgrid.infrastructure.TestGridInfrastructureException;
import org.wso2.carbon.testgrid.reporting.TestReportEngine;
import org.wso2.carbon.testgrid.reporting.TestReportingException;

/**
 * Created by harshan on 10/30/17.
 */
public class TestGridMgtServiceImpl implements TestGridMgtService {

    @Override
    public TestScenario addTest(TestConfiguration testConfiguration) throws TestGridException {
        return null;
    }

    @Override
    public boolean executeTest(TestConfiguration testConfiguration) throws TestGridException {
        for (SolutionPattern solutionPattern : testConfiguration.getPatterns()) {
            if (solutionPattern.isEnabled()) {
                //Construct a TestScenario using the SolutionPattern
                TestScenario testScenario = new TestScenario();
                try {
                    //Setup infrastructure
                    Deployment deployment = new InfrastructureProviderService().createTestEnvironment(testScenario);
                    //Trigger deployment
                    boolean status = new DeployerService().deploy(deployment);
                    if (status) {
                        //Run Tests
                        try {
                            new TestEngine().runScenario(testScenario);
                        } catch (TestEngineException e) {
                            e.printStackTrace();
                        }
                        //Generate Reports
                        try {
                            new TestReportEngine().generateReport(testScenario);
                        } catch (TestReportingException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (TestGridInfrastructureException e) {
                    e.printStackTrace();
                } catch (TestGridDeployerException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public boolean abortTest(TestScenario scenario) throws TestGridException {
        return false;
    }

    @Override
    public TestScenarioStatus getStatus(TestScenario scenario) throws TestGridException {
        return null;
    }
}
