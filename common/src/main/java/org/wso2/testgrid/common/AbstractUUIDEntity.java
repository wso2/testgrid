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

import java.sql.Timestamp;
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

    /**
     * Column names of the table.
     */
    public static final String ID_COLUMN = "id";

    @Id
    @Column(name = ID_COLUMN, length = 255)
    private String id;

    @Column(name = "created_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdTimestamp;

    @Column(name = "modified_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp modifiedTimestamp;

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

    /**
     * Returns the created timestamp.
     *
     * @return created timestamp
     */
    public Timestamp getCreatedTimestamp() {
        if (createdTimestamp == null) {
            return null;
        }
        return new Timestamp(createdTimestamp.getTime());
    }

    /**
     * Sets the created time stamp.
     *
     * @param createdTimestamp created time stamp
     */
    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = new Timestamp(createdTimestamp.getTime());
    }

    /**
     * Returns the modified timestamp of the test case.
     *
     * @return modified test case timestamp
     */
    public Timestamp getModifiedTimestamp() {
        if (modifiedTimestamp == null) {
            return null;
        }
        return new Timestamp(modifiedTimestamp.getTime());
    }

    /**
     * Sets the modified timestamp of the test case.
     *
     * @param modifiedTimestamp modified test case timestamp
     */
    public void setModifiedTimestamp(Timestamp modifiedTimestamp) {
        this.modifiedTimestamp = new Timestamp(modifiedTimestamp.getTime());
    }
}
