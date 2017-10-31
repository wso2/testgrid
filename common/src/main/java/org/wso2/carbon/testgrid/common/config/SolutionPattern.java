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

package org.wso2.carbon.testgrid.common.config;

/**
 * Defines the configuration of a single solution pattern.
 */
public class SolutionPattern {

    private String name;
    private boolean enabled;
    private boolean setupRequired;
    private String infraProvider;//ex. AWS, OpenStack, GCC
    private String scriptType; //ex. EC2, ECS, K8S
    private String automationEngine; //ex. puppet, Ansible
    private String testType; //ex. JMeter, TestNG

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getInfraProvider() {
        return infraProvider;
    }

    public void setInfraProvider(String infraProvider) {
        this.infraProvider = infraProvider;
    }

    public String getScriptType() {
        return scriptType;
    }

    public boolean isSetupRequired() {
        return setupRequired;
    }

    public void setSetupRequired(boolean setupRequired) {
        this.setupRequired = setupRequired;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public String getAutomationEngine() {
        return automationEngine;
    }

    public void setAutomationEngine(String automationEngine) {
        this.automationEngine = automationEngine;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }
}
