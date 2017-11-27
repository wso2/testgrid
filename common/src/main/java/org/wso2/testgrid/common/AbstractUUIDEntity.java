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

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

/**
 * Abstract class containing the common behaviour for entity classes.
 *
 * @since 1.0.0
 */
@MappedSuperclass
public abstract class AbstractUUIDEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Sets an UUID for the id field (primary key of the table) if not set.
     * <p>
     * The length of the generated UUID is always 36
     */
    @PrePersist
    public void generateUUID() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    /**
     * Returns the primary key value of the entity.
     *
     * @return primary key value of the entity
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the primary key value of the entity.
     *
     * @param id primary key value of the entity
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!OperatingSystem.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final OperatingSystem other = (OperatingSystem) obj;
        return this.id != null && other.getId() != null && this.id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
