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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Defines a model object of Operating system with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(
        name = "operating_system",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "version"})
        })
public class OperatingSystem extends AbstractUUIDEntity implements Serializable {

    private static final long serialVersionUID = 1587798651636567846L;

    @Column(name = "name", nullable = false)
    @Element(description = "defines the name of the required OS")
    private String name;

    @Column(name = "version", length = 20, nullable = false)
    @Element(description = "defines the version of the required OS")
    private String version;

    /**
     * Returns the name of the operating system.
     *
     * @return name of the operating system
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the operating system.
     *
     * @param name name of the operating system
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the operating system version.
     *
     * @return operating system version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the operating system version.
     *
     * @param version operating system version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Operating System [name=" + name + ", version=" + version + "]";
    }
}
