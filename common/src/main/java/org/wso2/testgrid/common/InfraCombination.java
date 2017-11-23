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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Defines a model object of InfraCombination with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(
        name = "infra_combination",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"jdk", "operating_system_id", "database_id"})
        })
public class InfraCombination extends AbstractUUIDEntity implements Serializable {

    private static final long serialVersionUID = 4742489056283093423L;

    @Enumerated(EnumType.STRING)
    @Column(name = "jdk", nullable = false)
    private JDK jdk;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = OperatingSystem.class)
    @PrimaryKeyJoinColumn(name = "operating_system_id", referencedColumnName = "id")
    private OperatingSystem operatingSystem;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = Database.class)
    @PrimaryKeyJoinColumn(name = "database_engine_id", referencedColumnName = "id")
    private Database database;

    @Override
    public String toString() {
        return "Infra Combination [JDK=" + jdk + ", operating system=" + operatingSystem +
               ", database=" + database + "]";
    }

    /**
     * Returns the JDK for the infra-combination.
     *
     * @return JDK for the infra-combination
     */
    public JDK getJdk() {
        return jdk;
    }

    /**
     * Sets the JDK for the infra-combination.
     *
     * @param jdk JDK for the infra-combination
     */
    public void setJdk(JDK jdk) {
        this.jdk = jdk;
    }

    /**
     * Returns the {@link OperatingSystem} instance for the infra combination.
     *
     * @return {@link OperatingSystem} instance for the infra combination
     */
    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Sets the {@link OperatingSystem} instance for the infra combination.
     *
     * @param operatingSystem {@link OperatingSystem} instance for the infra combination
     */
    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    /**
     * Returns the {@link Database} instance for the infra combination.
     *
     * @return {@link Database} instance for the infra combination
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Sets the {@link Database} instance for the infra combination.
     *
     * @param database {@link Database} instance for the infra combination
     */
    public void setDatabase(Database database) {
        this.database = database;
    }

    /**
     * This defines the possible JDKs of the {@link InfraCombination}.
     *
     * @since 1.0.0
     */
    public enum JDK {

        ORACLE_JDK9("ORACLE_JDK9"),
        ORACLE_JDK8("ORACLE_JDK8"),
        ORACLE_JDK7("ORACLE_JDK7");

        private final String jdk;

        /**
         * Sets the JDK for the infra-combination.
         *
         * @param jdk JDK for the infra-combination
         */
        JDK(String jdk) {
            this.jdk = jdk;
        }

        @Override
        public String toString() {
            return this.jdk;
        }
    }
}
