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

package org.wso2.carbon.testgrid.automation;

import org.wso2.carbon.testgrid.automation.core.TestManager;
import org.wso2.carbon.testgrid.automation.exceptions.TestEngineException;
import org.wso2.carbon.testgrid.automation.exceptions.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.exceptions.TestManagerException;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Host;
import org.wso2.carbon.testgrid.common.Port;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.reporting.Result;
import org.wso2.carbon.testgrid.reporting.TestReportEngine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by harshan on 10/30/17.
 */
public class TestEngine {


    public boolean runScenario(TestScenario scenario) throws TestEngineException {

        Deployment deployment = new Deployment();
        deployment.setName("Is_One_Node");

        Host host1 = new Host();
        host1.setIp("localhost");
        host1.setLabel("server_host");

        Port port = new Port();
        port.setLabel("server_port");
        port.setPortNumber(9443);
        host1.setPorts(Arrays.asList(port));
        deployment.setHosts(Arrays.asList(host1));


        TestManager testManager = new TestManager();
        try{
            scenario.setDeployment(deployment);
            testManager.init(scenario.getScenarioLocation(),deployment);
            testManager.executeTests();

        }catch(TestManagerException ex){
            ex.printStackTrace();
        } catch (TestGridExecuteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean abortScenario(TestScenario scenario) throws TestEngineException {
        return false;
    }
}
