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

package org.wso2.carbon.testgrid.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.infrastructure.InfrastructureProviderServiceImpl;
import org.wso2.carbon.testgrid.utils.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;


/**
 * This class handles the deployment of the artifacts in the cluster.
 */
public class DeployerServiceImpl implements DeployerService{

    private static final Log log = LogFactory.getLog(InfrastructureProviderServiceImpl.class);

    public void init() {

    }

    @Override
    public Deployment deploy(TestPlan testPlan) throws TestGridDeployerException {
        String testPlanLocation = testPlan.getTestScenarios().get(0).getScenarioLocation();
        String scriptLocation = testPlanLocation + "/Scripts/OpenStack/wso2is/deploy.sh";
        try {
            Util.executeCommand("chmod -R 777 " + testPlanLocation, null);
            System.setProperty("user.dir", testPlanLocation + "/Scripts/OpenStack/wso2is" );
            File file = new File(System.getProperty("user.dir"));

            System.out.println("Setting KUBERNETES_MASTER environment variable...");
            log.info("Setting KUBERNETES_MASTER environment variable...");
//            setKubernetesMasterEnvVariable(testPlanLocation + "/Scripts/OpenStack");
//            Util.executeCommand("export KUBERNETES_MASTER=http://192.168.58.71:8080", file);
            System.out.println("Deploying kubernetes artifacts...");
            log.info("Deploying kubernetes artifacts...");

            String secret = "kubectl create secret docker-registry registrykey " +
                    "--docker-server=dockerhub.private.wso2.com " +
                    "--docker-username=asmaj --docker-password=###" +
                    " --docker-email=asmaj@wso2.com ";

            Util.executeCommand(secret, file);

//            Util.executeCommand("bash " + scriptLocation + " " + getKubernetesMaster(testPlanLocation + "/Scripts/OpenStack"));
            Util.executeCommand("./deploy.sh" + getKubernetesMaster(testPlanLocation + "/Scripts/OpenStack"), file);
            return DeploymentUtil.getDeploymentInfo(testPlanLocation);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public boolean unDeploy(TestPlan testPlan) throws TestGridDeployerException {
        String testPlanLocation = testPlan.getTestScenarios().get(0).getScenarioLocation();
        String scriptLocation = testPlanLocation + "/Scripts/OpenStack/wso2is/undeploy.sh";

        Util.executeCommand("chmod -R 777 " + testPlanLocation, null);

        System.setProperty("user.dir", testPlanLocation + "/Scripts/OpenStack/wso2is" );
        File file = new File(System.getProperty("user.dir"));

        if(Util.executeCommand(/*"bash " + scriptLocation*/"./undeploy.sh", file)) {
            return true;
        }
        return false;
    }

    private void setKubernetesMasterEnvVariable (String path) throws IOException {
      /*  Properties prop = new Properties();
        InputStream inputStream = new FileInputStream(path + "/k8s.properties");
        prop.load(inputStream);
        System.out.println(inputStream);
        log.info(inputStream);*/

        Util.executeCommand("export KUBERNETES_MASTER=http://192.168.58.7:8080" /*+ prop.getProperty("KUBERNETES_MASTER")*/, null);

      /*  ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "export");
        Map<String, String> env = pb.environment();
        env.put("KUBERNETES_MASTER", prop.getProperty("KUBERNETES_MASTER"));
        System.out.println("==============================="+pb.environment().get("KUBERNETES_MASTER"));
        pb.start();*/
    }

    private String getKubernetesMaster(String location){
        Properties prop = new Properties();
        try {
            InputStream inputStream = new FileInputStream(location + "/k8s.properties");
            prop.load(inputStream);

            System.out.println(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop.getProperty("KUBERNETES_MASTER");
    }
}
