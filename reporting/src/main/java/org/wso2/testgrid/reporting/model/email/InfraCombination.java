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
 */

package org.wso2.testgrid.reporting.model.email;

/**
 * This defines a single infra combination.
 *
 * @since 1.0.0
 */
public class InfraCombination {
    private String os;
    private String jdk;
    private String dbEngine;

    /**
     * Returns name of the operating system.
     *
     * @return OS name + OS version
     */
    public String getOs() {
        return os;
    }

    /**
     * Sets name of the operating system.
     *
     * @param os OS name + OS version
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * Returns jdk name.
     *
     * @return JDK name
     */
    public String getJdk() {
        return jdk;
    }

    /**
     * Sets jdk name.
     *
     * @param jdk JDK name
     */
    public void setJdk(String jdk) {
        this.jdk = jdk;
    }

    /**
     * Returns database engine name.
     *
     * @return DB engine name + DB engine version
     */
    public String getDbEngine() {
        return dbEngine;
    }

    /**
     * Sets database engine name.
     *
     * @param dbEngine DB engine name + DB engine version
     */
    public void setDbEngine(String dbEngine) {
        this.dbEngine = dbEngine;
    }

}
