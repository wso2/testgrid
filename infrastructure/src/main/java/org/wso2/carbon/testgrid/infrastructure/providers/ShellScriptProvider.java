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

package org.wso2.carbon.testgrid.infrastructure.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.*;
import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;

import java.nio.file.Paths;

/**
 * This class creates the infrastructure for running tests
 */
public class ShellScriptProvider implements InfrastructureProvider {
    private static final Log log = LogFactory.getLog(ShellScriptProvider.class);
    private final static String SHELL_SCRIPT_PROVIDER = "Infra Create";
    private String testPlanLocation;

    @Override
    public String getProviderName() {
        return SHELL_SCRIPT_PROVIDER;
    }

    @Override
    public boolean canHandle(Infrastructure infrastructure) {
        boolean isScriptsAvailable = true;
        for (Script script : infrastructure.getScripts()){
            if (script.getScriptType() != Script.ScriptType.INFRA_CREATE &&
                    script.getScriptType() != Script.ScriptType.INFRA_DESTROY){
                isScriptsAvailable = false;
            }

        }
        return isScriptsAvailable;
    }

    @Override
    public Deployment createInfrastructure(Infrastructure infrastructure, String infraRepoDir) throws TestGridInfrastructureException {
        testPlanLocation = Paths.get(infraRepoDir, "DeploymentPatterns" , infrastructure.getName()).toString();

//        log.info("Initializing terraform...");
//        Utils.executeCommand("terraform init " + testPlanLocation + "/OpenStack", null);

        log.info("Creating the Kubernetes cluster...");
        Utils.executeCommand("bash " + Paths.get(testPlanLocation, infrastructure.getScripts().get(0).getFileName()).toString(), null);
//        infrastructure.setStatus(TestPlan.Status.INFRASTRUCTURE_READY);

        return null;
    }

    @Override
    public boolean removeInfrastructure(Deployment deployment, String infraRepoDir) throws TestGridInfrastructureException {
        log.info("Destroying test environment...");
        if(Utils.executeCommand("sh " + testPlanLocation + "/OpenStack/cluster-destroy.sh", null)) {
            return true;
        }
        return false;
    }
}
