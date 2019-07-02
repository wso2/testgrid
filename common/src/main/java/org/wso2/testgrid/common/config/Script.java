/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.testgrid.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridError;

import java.io.Serializable;
import java.util.Properties;

/**
 * Defines a model object for a provided custom script.
 */
public class Script implements Serializable, Cloneable {

    private static final long serialVersionUID = 6547552538295691010L;
    private static final Logger logger = LoggerFactory.getLogger(Script.class);

    private String name;
    private ScriptType type;
    private Phase phase;
    private String description;
    private String file;
    private Properties inputParameters = new Properties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Properties getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(Properties inputParameters) {
        this.inputParameters = inputParameters;
    }

    public ScriptType getType() {
        return type;
    }

    public void setType(ScriptType type) {
        this.type = type;
    }

    public Phase getPhase() {
        if (phase == null && type == ScriptType.CLOUDFORMATION) {
            return Phase.CREATE_AND_DELETE;
        }
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    /**
     * This defines the supported executable script types.
     */
    public enum Phase {
        /**
         * Defines the provision-infra shell script type.
         */
        CREATE("Create"),

        /**
         * Defines the provision-infra and destroy shell script type.
         */
        CREATE_AND_DELETE("Create_And_Delete"),

        /**
         * Defines the deploy shell script type.
         */
        DEPLOY("Deploy"),

        /**
         * Defines the destroy-infra shell script type.
         */
        DESTROY("Destroy");

        private final String name;

        Phase(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    /**
     * This defines the supported executable script types.
     */
    public enum ScriptType {

        /**
         * Defines the AWS cloud-formation script type.
         * TODO: think how we can remove this.
         */
        CLOUDFORMATION("Cloud Formation"),

        OPENSTACK("OpenStack"),

        /**
         * Defines the Kubernetes script type
         */

        KUBERNETES("KUBERNETES"),

        /**
         * Defines the Helm script type
         */

        HELM("HELM"),

        /**
         * Defines the AWS cloud-formation script type.
         * TODO: think how we can remove this.
         */
        SHELL("SHELL");

        private final String name;

        ScriptType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    @Override
    public Script clone() {
        try {
            return (Script) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new TestGridError("Error occurred while cloning Script object.", e);
        }
    }
}
