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

package org.wso2.testgrid.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * This is a DTO that contain the output of the
 * deployment creation scripts. ATM, it contain the
 * list of server URLs / hosts of wso2 products that were
 * spun during deployment creation.
 */
public class DeploymentCreationResult implements Serializable {
    private static final long serialVersionUID = 8887064358908392649L;

    private boolean success = true;
    private String name;
    private Properties properties = new Properties();
    private List<Host> hosts = Collections.emptyList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Properties getProperties() {
        if (properties == null) {
            return new Properties();
        }
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

}
