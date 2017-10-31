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

import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.common.config.SolutionPattern;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;
import org.wso2.carbon.testgrid.common.exception.TestGridException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the main entry point of the TestGrid Framework.
 */
public class TestGridMgtServiceImpl implements TestGridMgtService {

    @Override
    public TestPlan addTestPlan(TestConfiguration testConfiguration) throws TestGridException {
        if (testConfiguration != null) {
            Long timeStamp = new Date().getTime();
            String path = TestGridUtil.createTestDirectory(testConfiguration, timeStamp);
            if (path != null) {
                TestPlan testPlan = new TestPlan();
                testPlan.setHome(path);
                testPlan.setCreatedTimeStamp(timeStamp);
                //Clone Test Repo
                String repoLocation = TestGridUtil.cloneRepository(testConfiguration.getTestGitRepo(), path);
                List<TestScenario> scenarioList = new ArrayList<>();
                TestScenario testScenario;
                for (SolutionPattern pattern : testConfiguration.getSolutionPatterns()) {
                    if (pattern.isEnabled()) {
                        testScenario = new TestScenario();
                        testScenario.setEnabled(true);
                        //testScenario.setDeployerType(pattern.getAutomationEngine());
                        //testScenario.setInfrastructureType(pattern.getInfraProvider());
                        //testScenario.setScriptType(pattern.getScriptType());
                        testScenario.setSolutionPattern(pattern.getName());
                        testScenario.setStatus(TestScenario.TestScenarioStatus.EXECUTION_PLANNED);
                        testScenario.setTempLocation(path);
                        testScenario.setScenarioLocation(repoLocation + File.separator + pattern.getName());
                        scenarioList.add(testScenario);
                    }
                }
                testPlan.setTestScenarios(scenarioList);
                return testPlan;
            }
        }
        return null;
    }

    @Override
    public boolean executeTestPlan(TestPlan testPlan) throws TestGridException {
        for (TestScenario testScenario : testPlan.getTestScenarios()) {
            if (testScenario.isEnabled()) {
                try {
                    //Run the ScenarioExecutor
                    new ScenarioExecutor().runScenario(testScenario);
                } catch (ScenarioExecutorException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public boolean abortTestPlan(TestPlan testPlan) throws TestGridException {
        return false;
    }

    @Override
    public TestScenario.TestScenarioStatus getStatus(TestPlan testPlan) throws TestGridException {
        return null;
    }
}
