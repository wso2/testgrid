/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.testgrid.automation.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.beans.Deployment;
import org.wso2.carbon.testgrid.automation.beans.Port;
import org.wso2.carbon.testgrid.automation.beans.TestLink;
import org.wso2.carbon.testgrid.automation.FrameworkConstants;
import org.wso2.carbon.testgrid.automation.TestLinkConstants;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Reader for deployment.yaml for deployment information
 */
public class DeploymentConfigurationReader {

    private static final Log log = LogFactory.getLog(DeploymentConfigurationReader.class);
    private static DeploymentConfigurationReader deploymentConfigurationReader = null;
    private static HashMap<String, Deployment> deploymentHashMap;
    private static TestLink testlinkConfig;

    //Get the only object available
    private DeploymentConfigurationReader() {
    }

    public static DeploymentConfigurationReader readConfiguration() throws IOException {
        synchronized (DeploymentConfigurationReader.class) {
            if (deploymentConfigurationReader == null) {
                deploymentConfigurationReader = new DeploymentConfigurationReader();
                deploymentHashMap = readConfigurationYaml();
                testlinkConfig = deploymentConfigurationReader.readTestLinkConfigs();
            }
        }
        return deploymentConfigurationReader;
    }

    private static HashMap<String, Deployment> readConfigurationYaml() throws IOException {
        Map<String, Object> map = getDeploymentObjectMap();
        HashMap<String, Deployment> deploymentHashMap = new HashMap<>();
        ArrayList<Object> deploymentList = (ArrayList<Object>) map.get(DeploymentYamlConstants.YAML_DEPLOYMENTS);
        HashMap<String, String> instanceList;
        for (Object deploymentObj : deploymentList) {

            Deployment deployment = new Deployment();
            deployment.setName(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_NAME).toString());
            deployment.setDeployScripts(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_SCRIPT).toString());
            deployment.setRepository(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_REPO).toString());

            deployment.setSuite(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_SUITE).toString());

            deployment.setUnDeployScripts(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_UNDEPLOYMENT_SCRIPT).toString());

            deployment.setEnable(Boolean.parseBoolean(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_UNDEPLOYMENT_SCRIPT).toString()));

            deployment.setFilePath(
                    ((LinkedHashMap) deploymentObj).get(DeploymentYamlConstants.YAML_DEPLOYMENT_URL_FILE_PATH)
                            .toString());
            instanceList = (HashMap<String, String>) ((ArrayList<Object>) ((LinkedHashMap) deploymentObj)
                    .get(DeploymentYamlConstants.YAML_DEPLOYMENT_INSTANCE_MAP)).get(0);
            deployment.setInstanceMap(instanceList);

            deploymentHashMap.put(deployment.getName(), deployment);
        }

        return deploymentHashMap;
    }

    private static TestLink readTestLinkConfigs() throws IOException {
        TestLink testLinkConf = new TestLink();
        ArrayList<Object> testLinkConfigMap = (ArrayList<Object>) getTestLinkConfigurationObject()
                .get(TestLinkConstants.TESTLINK_Server_INFO);
        HashMap<String, Object> map = (HashMap<String, Object>) testLinkConfigMap.get(0);

        testLinkConf.setUrl(map.get(TestLinkConstants.TESTLINK_SERVER_HOST).toString());
        testLinkConf.setDevkey(map.get(TestLinkConstants.TESTLINK_DEV_KEY).toString());
        testLinkConf.setEnabled((Boolean) map.get(TestLinkConstants.ENABLE_TESTLINK_FETCHING));
        testLinkConf.setProjectName(map.get(TestLinkConstants.TESTLINK_PROJECT).toString());
        testLinkConf.setTestPlan(map.get(TestLinkConstants.TESTLINK_TESTPLAN).toString());
        testLinkConf.setTestLinkCustomField(map.get(TestLinkConstants.TESTLINK_CUSTOM_FIELD).toString());

        return testLinkConf;
    }

    private static Map<String, String> envVariableMap(Map<String, ArrayList<String>> instanceMap) {
        Map<String, String> envVariableMap = null;
        Iterator<Entry<String, ArrayList<String>>> itr = instanceMap.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, ArrayList<String>> instanceEntry = itr.next();
            if (instanceEntry.getKey().equals(DeploymentYamlConstants.YAML_DEPLOYMENT_INSTANCES_ENV_MAP)) {
                try {
                    Object valueObject = instanceEntry.getValue().get(0);
                    envVariableMap = (LinkedHashMap) valueObject;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return envVariableMap;
    }

    private static List<Port> portList(Map<String, ArrayList<String>> instanceMap) {
        Map<String, String> portValuedMap;
        Iterator<Entry<String, ArrayList<String>>> instanceIterator = instanceMap.entrySet().iterator();
        List<Port> portList = new ArrayList<>();
        while (instanceIterator.hasNext()) {
            Entry<String, ArrayList<String>> instanceEntry = instanceIterator.next();
            if (instanceEntry.getKey().equals(DeploymentYamlConstants.YAML_DEPLOYMENT_PORTS)) {
                for (Object valueObject : instanceEntry.getValue()) {
                    portValuedMap = (LinkedHashMap) valueObject;
                    Iterator<Entry<String, String>> portIterator = portValuedMap.entrySet().iterator();
                    Port port = new Port();
                    while (portIterator.hasNext()) {
                        Entry<String, String> portEntry = portIterator.next();
                        if (portEntry.getKey().equals(DeploymentYamlConstants.YAML_DEPLOYMENT_PORT_NAME)) {
                            port.setName(portEntry.getValue());
                        } else if (portEntry.getKey().equals(DeploymentYamlConstants.YAML_DEPLOYMENT_PORT_PORT)) {
                            port.setPort(Integer.parseInt(String.valueOf(portEntry.getValue())));
                        } else if (portEntry.getKey().equals(DeploymentYamlConstants.YAML_DEPLOYMENT_PORT_NODE_PORT)) {
                            port.setNodePort(Integer.parseInt(String.valueOf(portEntry.getValue())));
                        } else if (portEntry.getKey()
                                .equals(DeploymentYamlConstants.YAML_DEPLOYMENT_PORT_TARGET_PORT)) {
                            port.setTargetPort(Integer.parseInt(String.valueOf(portEntry.getValue())));
                        } else if (portEntry.getKey().equals(DeploymentYamlConstants.YAML_DEPLOYMENT_PORT_PROTOCOL)) {
                            port.setProtocol(portEntry.getValue());
                        }
                    }
                    portList.add(port);
                }
            }
        }

        return portList;
    }

    private static Map<String, Object> getDeploymentObjectMap() throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION)
                    + DeploymentYamlConstants.DEPLYMENT_YAML_FILE_NAME);
            Yaml yaml = new Yaml();
            return (Map<String, Object>) yaml.load(fis);
        } finally {

            if (fis != null) {
                fis.close();
            }
        }
    }

    private static Map<String, Object> getTestLinkConfigurationObject() throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION)
                    + TestLinkConstants.TESTLINK_CONFIG_FILE_NAME);
            Yaml yaml = new Yaml();
            return (Map<String, Object>) yaml.load(fis);
        } finally {

            if (fis != null) {
                fis.close();
            }
        }
    }

    public HashMap getDeploymentInstanceMap(String pattern) throws IOException {

        return getDeploymentHashMap().get(pattern).getInstanceMap();
    }

    public HashMap<String, Deployment> getDeploymentHashMap() throws IOException {
        if (deploymentConfigurationReader == null) {
            readConfiguration();
        }
        return deploymentHashMap;
    }

    public TestLink getTestLinkConfigurations() throws IOException {
        if (deploymentConfigurationReader == null) {
            readConfiguration();
        }
        return testlinkConfig;
    }

}