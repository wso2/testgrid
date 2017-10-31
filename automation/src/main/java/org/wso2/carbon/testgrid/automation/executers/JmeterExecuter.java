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

package org.wso2.carbon.testgrid.automation.executers;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.exceptions.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.executers.common.TestExecuter;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Host;
import org.wso2.carbon.testgrid.common.Port;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;
import org.wso2.carbon.testgrid.utils.EnvVariableUtil;

import java.io.File;
import java.io.IOException;

public class JmeterExecuter implements TestExecuter {

    private static final Log log = LogFactory.getLog(JmeterExecuter.class);

    private String jmterHome;
    private String testGridFolder;
    private String testName;

    @Override
    public void execute(String script, Deployment deployment) throws TestGridExecuteException {
        String scriptName = getScriptName(script);
        Runtime runtime = Runtime.getRuntime();
        try {
            String prop = changePropertyFile(deployment);
            String command;
            if (prop != null) {
                command = "sh " + this.jmterHome + "/bin/jmeter.sh -n -t " + script + " -l " +
                        testGridFolder + "/JMeter-Responses/" + testName + File.separator + scriptName + ".csv -p " + prop;
            } else {
                command = "sh " + this.jmterHome + "/bin/jmeter.sh -n -t " + script + " -l " +
                        testGridFolder + "/JMeter-Responses/" + testName + File.separator + scriptName + ".csv";
            }
            Process exec = runtime.exec(command);
            exec.waitFor();

        } catch (IOException e) {
            String msg = "Error occured while executing the jmeter script";
            log.error(msg,e);
            throw  new TestGridExecuteException(msg,e);
        } catch (InterruptedException e) {
            String msg = "Jmeter Script execution inturrupted";
            log.error(msg,e);
            throw  new TestGridExecuteException(msg,e);
        } catch (ConfigurationException e) {
            String msg = "Error occured while reading user.properties";
            log.error(msg,e);
            throw  new TestGridExecuteException(msg,e);
        }
    }

    @Override
    public void init(String testGridFolder, String testName) throws TestGridExecuteException {
        this.testName = testName;
        String jmeterHome = EnvVariableUtil.readEnvironmentVariable(TestGridConstants.JMETER_HOME);
        if (jmeterHome != null) {
            this.jmterHome = jmeterHome;
            this.testGridFolder = testGridFolder;
        } else {
            String msg = "Enviorenment Variable JMETER_HOME is not set";
            log.error(msg);
            throw new TestGridExecuteException(msg);
        }
    }

    private String getScriptName(String script) {
        String[] split = script.split("/");
        return split[split.length-1];
    }

    private String changePropertyFile(Deployment deployment) throws ConfigurationException {
        File file = new File(testGridFolder + File.separator + "JMeter" + File.separator + testName +
                "/src/test/resources/user.properties");
        if (file.exists()) {
            PropertiesConfiguration conf = new PropertiesConfiguration(file.getAbsolutePath());
            for (Host host : deployment.getHosts()) {
                conf.setProperty(host.getLabel(), host.getIp());
                for (Port port : host.getPorts()) {
                    conf.setProperty(port.getLabel(), port.getPortNumber());
                }
            }
            conf.save();
            return file.getAbsolutePath();

        } else {
            return null;
        }

    }
}
