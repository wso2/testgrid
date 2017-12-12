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

import org.wso2.carbon.config.annotation.Element;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Defines a model object of Database with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(
        name = Database.DATABASE_TABLE,
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {Database.ENGINE_COLUMN, Database.VERSION_COLUMN})
        })
public class Database extends AbstractUUIDEntity implements Serializable {

    /**
     * Database table name.
     */
    public static final String DATABASE_TABLE = "database_engine";

    /**
     * Column names of the table.
     */
    public static final String ENGINE_COLUMN = "engine";
    public static final String VERSION_COLUMN = "version";

    private static final long serialVersionUID = 3648225690542176032L;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine", nullable = false)
    @Element(description = "defines the database engine type")
    private DatabaseEngine engine;

    @Column(name = "version", length = 20, nullable = false)
    @Element(description = "defines the database version")
    private String version;

    /**
     * Returns the database engine name.
     *
     * @return database engine name
     */
    public DatabaseEngine getEngine() {
        return engine;
    }

    /**
     * Sets the database engine name.
     *
     * @param engine database engine name
     */
    public void setEngine(DatabaseEngine engine) {
        this.engine = engine;
    }

    /**
     * Returns the database version.
     *
     * @return database version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the database version.
     *
     * @param version database version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Database)) {
            return false;
        }
        Database database = (Database) object;
        return engine == database.engine && Objects.equals(version, database.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engine, version);
    }

    @Override
    public String toString() {
        return StringUtil.concatStrings("Database{",
                "id='", this.getId(), "\'",
                ", engine='", engine, "\'",
                ", version='", version, "\'",
                '}');
    }

    /**
     * Defines the database engines.
     *
     * @since 1.0.0
     */
    public enum DatabaseEngine {
        MYSQL("MySQL"),
        DB2("DB2"),
        ORACLE("Oracle"),
        SQL_SERVER("SQL Server"),
        POSTGRESQL("PostgreSQL"),
        H2("H2"),
        MariaDB("Maria DB");

        private final String engine;

        /**
         * Sets the database engine name.
         *
         * @param engine database engine name
         */
        DatabaseEngine(String engine) {
            this.engine = engine;
        }

        @Override
        public String toString() {
            return this.engine;
        }
    }
}
