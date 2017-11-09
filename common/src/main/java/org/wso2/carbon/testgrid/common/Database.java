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
 *  Defines a model object of Database with required attributes.
 */
public class Database {

    @Element(description = "defines the database engine type")
    private DatabaseEngine engine;
    @Element(description = "defines the database version")
    private String version;

    public DatabaseEngine getEngine() {
        return engine;
    }

    public void setEngine(DatabaseEngine engine) {
        this.engine = engine;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public enum DatabaseEngine {
        MYSQL ("MySQL"), DB2 ("DB2"), ORACLE ("Oracle"), SQL_SERVER ("SQL Server"), POSTGRESQL ("PostgreSQL"), H2("H2"),
        MariaDB("Maria DB");

        private final String name;

        DatabaseEngine(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    @Override
    public String toString() {
        return this.engine.toString() + ", version:" + version;
    }
}
