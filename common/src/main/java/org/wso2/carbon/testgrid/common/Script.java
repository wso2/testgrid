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

    @Element(description = "defines the provided scripts type (not necessary)")
    private ScriptType scriptType;
    @Element(description = "defines the file name of provided script")
    private String fileName;
    @Element(description = "defines the order in which the script should be executed")
    private int order;

    public enum ScriptType {
        CLOUD_FORMATION ("Cloud Formation"), SHELL ("Shell");

        private final String name;

        ScriptType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
