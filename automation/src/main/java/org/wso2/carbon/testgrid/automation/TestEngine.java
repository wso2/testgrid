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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.core.TestManager;
import org.wso2.carbon.testgrid.automation.exceptions.TestEngineException;
import org.wso2.carbon.testgrid.automation.exceptions.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.exceptions.TestManagerException;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Host;
import org.wso2.carbon.testgrid.common.Port;
import org.wso2.carbon.testgrid.common.TestScenario;

import java.util.Arrays;

/**This class is Responsible for initiating the core processes of TestGrid.
 */
public class TestEngine {

    private static final Log log = LogFactory.getLog(TestEngine.class);

    /**
     *
     * @param scenario The Test scenario that is being executed.
     * @return true if all the processes finished.
     * @throws TestEngineException when there is an error in the process.
     */
    public boolean runScenario(TestScenario scenario, Deployment deployment) throws TestEngineException {

        //-----------------dummy deployement object----------------------------------//
        Deployment deployment1 = new Deployment();
        deployment.setName("Is_One_Node");

        Host host1 = new Host();
        host1.setIp("localhost");
        host1.setLabel("server_host");

        Port port = new Port();
        port.setProtocol("server_port");
        port.setPortNumber(9443);
        host1.setPorts(Arrays.asList(port));
        deployment.setHosts(Arrays.asList(host1));

        //-----------------end of dummy deployement object----------------------------------//
        log.info("Executing Solution Pattern : " + scenario.getSolutionPattern());
        TestManager testManager = new TestManager();
        try {
            testManager.init(scenario.getScenarioLocation(), deployment);
            testManager.executeTests();

        } catch (TestManagerException ex) {
            throw new TestEngineException("Error while initiating the TestManager", ex);
        } catch (TestGridExecuteException ex) {
            throw new TestEngineException("Error while Executing Tests", ex);
        }
        return true;
    }

    public boolean abortScenario(TestScenario scenario) throws TestEngineException {
        return false;
    }
}
