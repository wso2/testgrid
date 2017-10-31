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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestScenario;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by harshan on 10/30/17.
 */
public class DeployerService {

    public void init() {

    }

    public Deployment deploy(TestScenario testScenario) throws TestGridDeployerException {
       /* try {
            String target = new String(testScenario.getScenarioLocation() + "/Scripts/OpenStack/deploy.sh");
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(target);
            proc.waitFor();
            StringBuffer output = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            System.out.println("### " + output);
        } catch (Throwable t) {
            t.printStackTrace();
        }*/

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "bash " +
                testScenario.getScenarioLocation() + "/Scripts/OpenStack/wso2is/deploy.sh");
        Process process = null;
        try {
            process = pb.start();

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(testScenario.getScenarioLocation() + "/Scripts/OpenStack/wso2is/deployment.json");
        try {
            return mapper.readValue(file, Deployment.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean unDeploy(TestScenario testScenario) throws TestGridDeployerException {
        /*try {
            String target = new String(testScenario.getScenarioLocation() + "/Scripts/OpenStack/undeploy.sh");
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(target);
            proc.waitFor();
            StringBuffer output = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            System.out.println("### " + output);
        } catch (Throwable t) {
            t.printStackTrace();
        }*/

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "bash " +
                testScenario.getScenarioLocation() + "/Scripts/OpenStack/wso2is/undeploy.sh");
        Process process = null;
        try {
            process = pb.start();

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
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


}
