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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestScenario;
import org.wso2.carbon.testgrid.infrastructure.openstack.ClusterDeployer;
import org.wso2.carbon.testgrid.infrastructure.openstack.DeployerConstants;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * Created by harshan on 10/30/17.
 */
public class InfrastructureProviderService {
    private static final Log log = LogFactory.getLog(ClusterDeployer.class);
    private static InputStream inputStream;

    boolean getScript(String repo) throws TestGridInfrastructureException {
        return false;
    }

    public boolean createTestEnvironment(TestScenario scenario) throws TestGridInfrastructureException {
//        log.info("Initializing terraform...");
        System.out.println("Initializing terraform...");
        try {
            executeCommand("terraform init " + scenario.getScenarioLocation() + "/Scripts/OpenStack");
            System.out.println("$$$$$$$$$$$$$$$$$$$$$ terraform init " + scenario.getScenarioLocation() + "/Scripts/OpenStack");
            System.out.println("Destroy existing cluster (if any)...");

            executeCommand("sh " + scenario.getScenarioLocation() + "/Scripts/OpenStack/cluster-destroy.sh");

            System.out.println("Creating instances and deploying Kubernetes cluster...");
            executeCommand("terraform init " + scenario.getScenarioLocation() + "/Scripts/OpenStack");
            executeCommand("bash " + scenario.getScenarioLocation() + "/Scripts/OpenStack/infra.sh");

            System.out.println("Setting KUBERNETES_MASTER environment variable...");
            setKubernetesMasterEnvVariable(scenario.getScenarioLocation()+ "/Scripts/OpenStack");
            scenario.setStatus(TestScenario.TestScenarioStatus.DEPLOYMENT_PREPARATION);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeTestEnvironment(TestScenario scenario) throws TestGridInfrastructureException {
        return false;
    }

    private boolean executeCommand(String command) throws IOException {

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = pb.start();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ( (line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();
        System.out.println(result);

        return true;
    }

    public static void setKubernetesMasterEnvVariable (String path) throws IOException {
        Properties prop = new Properties();
        inputStream = new FileInputStream(path + "/k8s.properties");
        prop.load(inputStream);

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "export");
        Map<String, String> env = pb.environment();
        env.put("KUBERNETES_MASTER", prop.getProperty("KUBERNETES_MASTER"));
    }

}
