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

package org.wso2.carbon.testgrid.common;

import org.wso2.carbon.config.annotation.Element;

/**
 *  Defines a model object for a provided custom script.
 */
public class Script {

    @Element(description = "defines the location of provided script")
    private String filePath;
    @Element(description = "defines the name of provided script")
    private String name;
    @Element(description = "defines the order in which the script should be executed")
    private int order;
    @Element(description = "defines the provided scripts type (not necessary)")
    private ScriptType scriptType;

    public enum ScriptType {
        CLOUD_FORMATION ("Cloud Formation"), INFRA_CREATE ("Infra Create"), INFRA_DESTROY ("Infra Destroy");

        private final String name;

        ScriptType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
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
