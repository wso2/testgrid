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

import org.wso2.testgrid.common.infrastructure.DeploymentPatternResourceUsage;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * This represents a model of the DeploymentPattern.
 *
 * @since 1.0.0
 */
@SqlResultSetMapping(name = "DeploymentPatternTestFailureStatMapping",
                     classes = {
                             @ConstructorResult(targetClass = DeploymentPatternTestFailureStat.class,
                                                columns = {@ColumnResult(name = "deploymentPatternId"),
                                                           @ColumnResult(name = "failureCount")}
                             )}
)
@Entity
@Table(
        name = DeploymentPattern.DEPLOYMENT_PATTERN_TABLE,
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {DeploymentPattern.NAME_COLUMN, DeploymentPattern.PRODUCT_COLUMN})
        })
public class DeploymentPattern extends AbstractUUIDEntity implements Serializable {

    /**
     * Deployment pattern table name.
     */
    public static final String DEPLOYMENT_PATTERN_TABLE = "deployment_pattern";

    /**
     * Column names of the table.
     */
    public static final String NAME_COLUMN = "name";
    public static final String PRODUCT_COLUMN = "product";

    /**
     * Key names of property column.
     */
    public static final String MD5_HASH_PROPERTY = "md5Hash";

    private static final long serialVersionUID = -4345126378695708155L;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "properties")
    private String properties;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = Product.class,
               fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "PRODUCT_id", referencedColumnName = ID_COLUMN)
    private Product product;

    @OneToMany(mappedBy = "deploymentPattern", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestPlan> testPlans = new ArrayList<>();

    @OneToMany(mappedBy = "deploymentPattern", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeploymentPatternResourceUsage> deploymentPatternResourceUsageList = new ArrayList<>();

    /**
     * Returns the name of the test plan.
     *
     * @return the name of the test plan
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of test plan.
     *
     * @param name name of the test plan
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the product associated with the deployment pattern.
     *
     * @return product associated with the deployment pattern
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Sets the product associated with the deployment pattern.
     *
     * @param product product associated with the deployment pattern
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Returns the test plans associated with.
     *
     * @return test plans associated with
     */
    public List<TestPlan> getTestPlans() {
        return testPlans;
    }

    /**
     * Sets the test plans associated with.
     *
     * @param testPlans test plans associated with
     */
    public void setTestPlans(List<TestPlan> testPlans) {
        this.testPlans = testPlans;
    }

    /**
     * Adds a test plan to the test plans list.
     *
     * @param testPlan test plan to be added
     */
    public void addTestPlan(TestPlan testPlan) {
        testPlans.add(testPlan);
    }

    public List<DeploymentPatternResourceUsage> getDeploymentPatternResourceUsageList() {

        return deploymentPatternResourceUsageList;
    }

    public void setDeploymentPatternResourceUsageList(
            List<DeploymentPatternResourceUsage> deploymentPatternResourceUsageList) {

        this.deploymentPatternResourceUsageList = deploymentPatternResourceUsageList;
    }

    public String getProperties() {

        return properties;
    }

    public void setProperties(String properties) {

        this.properties = properties;
    }
    @Override
    public String toString() {
        String id = this.getId() != null ? this.getId() : "";
        String createdTimestamp = this.getCreatedTimestamp() != null ? this.getCreatedTimestamp().toString() : "";
        String modifiedTimestamp = this.getModifiedTimestamp() != null ? this.getModifiedTimestamp().toString() : "";
        return StringUtil.concatStrings("DeploymentPattern{",
                "id='", id, "\'",
                ", name='", name, "\'",
                ", properties='", properties, "\'",
                ", createdTimestamp='", createdTimestamp, "\'",
                ", modifiedTimestamp='", modifiedTimestamp, "\'",
                ", product='", product, "\'",
                '}');
    }
}
