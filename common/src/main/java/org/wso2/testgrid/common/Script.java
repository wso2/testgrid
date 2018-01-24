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

package org.wso2.testgrid.common;

import java.io.Serializable;
import java.util.Properties;

/**
 * Defines a model object for a provided custom script.
 */
public class Script implements Serializable {

    private static final long serialVersionUID = -3977238740076938871L;

    private String filePath;
    private String name;
    private int order;
    private ScriptType scriptType;
    private Properties scriptParameters;
    private Properties environmentScriptParameters;

    /**
     * This defines the supported executable script types.
     */
    public enum ScriptType {

        /**
         * Defines the AWS cloud-formation script type.
         */
        CLOUD_FORMATION("Cloud Formation"),

        /**
         * Defines the create-infra shell script type.
         */
        INFRA_CREATE("Infra Create"),

        /**
         * Defines the deploy shell script type.
         */
        DEPLOY("Deploy"),

        /**
         * Defines the destroy-infra shell script type.
         */
        INFRA_DESTROY("Infra Destroy");

        private final String name;

        ScriptType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public Properties getScriptParameters() {
        return scriptParameters != null ? scriptParameters : new Properties();
    }

    public void setScriptParameters(Properties scriptParameters) {
        this.scriptParameters = scriptParameters;
    }

    /**
     * Get Script parameters that needs to be retrieved as Environment variables.
     *
     * @return script parameters
     */
    public Properties getEnvironmentScriptParameters() {
        return environmentScriptParameters != null ? environmentScriptParameters : new Properties();
    }

    /**
     * Set Script parameters that needs to be retrieved as Environment variables.
     *
     * @param environmentScriptParameters {@link Properties} object populated by script parameters
     */
    public void setEnvironmentScriptParameters(Properties environmentScriptParameters) {
        this.environmentScriptParameters = environmentScriptParameters;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }
}
