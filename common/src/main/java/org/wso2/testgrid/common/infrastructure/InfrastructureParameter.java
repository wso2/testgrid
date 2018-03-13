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

package org.wso2.testgrid.common.infrastructure;

import org.wso2.testgrid.common.AbstractUUIDEntity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * This represents the InfrastructureParameter entity.
 * An InfrastructureParameter provides details about a given
 * infrastructure - name, type, and list of its properties.
 * <p>
 * Type is defined by the @{@link Type} enum.
 *
 * @since 1.0
 */
@Entity
@Table(
        name = InfrastructureParameter.INFRASTRUCTURE_PARAMETER_TABLE,
        uniqueConstraints = {
                @UniqueConstraint(columnNames = InfrastructureParameter.INFRASTRUCTURE_PARAMETER_NAME_COLUMN)
        }
)
public class InfrastructureParameter extends AbstractUUIDEntity implements
        Serializable, Comparable<InfrastructureParameter> {

    public static final String INFRASTRUCTURE_PARAMETER_NAME_COLUMN = "name";

    /**
     * We need to replace these metamodel names by generating the JPA Metamodel for this entity.
     * This needs to happen after merging common, dao, and core modules into one.
     * TODO: testgrid#413
     */
    public static final String INFRASTRUCTURE_PARAMETER_TYPE_METAMODEL_NAME = "type";
    public static final String INFRASTRUCTURE_PARAMETER_READY_FOR_TESTGRID_METAMODEL_NAME = "readyForTestGrid";
    static final String INFRASTRUCTURE_PARAMETER_TABLE = "infrastructure_parameter";

    private static final long serialVersionUID = 4714791395656165784L;

    @Column
    private String name;

    @Column
    private String type;

    @Column
    private String properties;

    @Column(name = "ready_for_testgrid")
    private boolean readyForTestGrid;

    public InfrastructureParameter(String name, String type, String properties, boolean readyForTestGrid) {
        this.name = name;
        this.type = type;
        this.properties = properties;
        this.readyForTestGrid = readyForTestGrid;
    }

    public InfrastructureParameter() {
    }

    /**
     * Name is a unique identifier for a given infrastructure.
     *
     * @return @{@link InfrastructureParameter} name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The properties list make include information about this {@link InfrastructureParameter}.
     * Typical properties may include the AMI_ID, RDS_ID etc.
     *
     * @return list of properties of this {@link InfrastructureParameter}
     */
    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public boolean isReadyForTestGrid() {
        return readyForTestGrid;
    }

    public void setReadyForTestGrid(boolean readyForTestGrid) {
        this.readyForTestGrid = readyForTestGrid;
    }

    @Override
    public String toString() {
        return "InfrastructureParameter{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", readyForTestGrid=" + readyForTestGrid +
                "}\n";
    }

    @Override
    public int compareTo(InfrastructureParameter o) {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        int comparison = name.compareTo(o.name);
        if (comparison != equal) {
            return comparison;
        }

        comparison = type.compareTo(o.type);
        if (comparison != equal) {
            return comparison;
        }

        comparison = properties.compareTo(o.properties);
        if (comparison != equal) {
            return comparison;
        }

        if (!readyForTestGrid && o.readyForTestGrid) {
            return before;
        }

        if (readyForTestGrid && !o.readyForTestGrid) {
            return after;
        }

        return equal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InfrastructureParameter that = (InfrastructureParameter) o;

        return readyForTestGrid == that.readyForTestGrid
                && name.equals(that.name)
                && Objects.equals(type, that.type)
                && (properties != null ? properties.equals(that.properties) : that.properties == null);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (readyForTestGrid ? 1 : 0);
        return result;
    }

}
