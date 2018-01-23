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

import org.wso2.testgrid.common.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * This represents a model of the Product which includes the name, version and the channel.
 * All the test-configs will be mapped to a product based on the configuration.
 * <p>
 * A single product will have multiple deployment patterns
 *
 * @since 1.0.0
 */
@SqlResultSetMapping(name = "ProductTestStatusMapping",
        classes = {
                @ConstructorResult(targetClass = ProductTestStatus.class,
                        columns = { @ColumnResult(name = "id"), @ColumnResult(name = "name"),
                                @ColumnResult(name = "deploymentPatternId"), @ColumnResult(name = "deploymentPattern"),
                                @ColumnResult(name = "status"), @ColumnResult(name = "testExecutionTime") }) })
@Entity
@Table(
        name = Product.PRODUCT_TABLE,
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {Product.NAME_COLUMN})
        })
public class Product extends AbstractUUIDEntity implements Serializable {

    /**
     * Product test plan table name.
     */
    public static final String PRODUCT_TABLE = "product";

    /**
     * Column names of the table.
     */
    public static final String NAME_COLUMN = "name";

    private static final long serialVersionUID = 5812347338918334430L;

    @Column(name = "name", nullable = false, length = 50)
    private String name;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeploymentPattern> deploymentPatterns = new ArrayList<>();

    /**
     * Returns the product name.
     *
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product name.
     *
     * @param name product name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the associated test plans list.
     *
     * @return associated test plans list
     */
    public List<DeploymentPattern> getDeploymentPatterns() {
        return deploymentPatterns;
    }

    /**
     * Sets the associated test plans list.
     *
     * @param deploymentPatterns associated test plans list
     */
    public void setDeploymentPatterns(List<DeploymentPattern> deploymentPatterns) {
        this.deploymentPatterns = deploymentPatterns;
    }

    @Override
    public String toString() {
        String id = this.getId() != null ? this.getId() : "";
        String createdTimestamp = this.getCreatedTimestamp() != null ? this.getCreatedTimestamp().toString() : "";
        String modifiedTimestamp = this.getModifiedTimestamp() != null ? this.getModifiedTimestamp().toString() : "";
        return StringUtil.concatStrings("Product{",
                "id='", id, "\'",
                ", name='", name, "\'",
                ", createdTimestamp='", createdTimestamp, "\'",
                ", modifiedTimestamp='", modifiedTimestamp, "\'",
                '}');
    }
}
