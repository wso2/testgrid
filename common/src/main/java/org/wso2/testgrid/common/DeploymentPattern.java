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

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.config.annotation.Ignore;
import org.wso2.testgrid.common.util.StringUtil;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

import static org.wso2.testgrid.common.AbstractUUIDEntity.ID_COLUMN;

/**
 * This represents a model of the DeploymentPattern which includes all the necessary data to run the required SolutionPatterns.
 * <p>
 * A single deployment will have  multiple infra Results.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = DeploymentPattern.DEPLOYMNET_PATTERN_TABLE)
@Configuration(namespace = "wso2.testgrid.deploymentpattern",
        description = "TestGrid Deployment Pattern Configuration Parameters")
public class DeploymentPattern extends AbstractUUIDEntity implements Serializable {

    /**
     * Deployment pattern table name.
     */
    public static final String DEPLOYMNET_PATTERN_TABLE = "deployment_pattern";

    /**
     * Column names of the table.
     */
    public static final String NAME_COLUMN = "name";
    public static final String STATUS_COLUMN = "testSuccessStatus";
    public static final String PRODUCT_COLUMN = "product";

    private static final long serialVersionUID = -4345126378695708155L;

    @Element(description = "value to uniquely identify the TestPlan")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "testSuccessStatus", nullable = false)
    private boolean testSuccessStatus;

    @Ignore
    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = Product.class,
            fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "PRODUCT_id", referencedColumnName = ID_COLUMN)
    private Product product;

    @OneToMany(mappedBy = "deploymentPattern", cascade = CascadeType.ALL, orphanRemoval = true)
    @Element(description = "list of test scenarios to be executed")
    private List<TestPlan> testPlans;

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
     * Returns whether the test is successful or failed.
     *
     * @return {@code true} if the test case is successful, {@code false} otherwise
     */
    public boolean getTestSuccessStatus() {
        return testSuccessStatus;
    }

    /**
     * Sets whether the test is successful or failed.
     *
     * @param testSuccess whether the test is successful or failed
     */
    public void setTestSuccess(boolean testSuccess) {
        testSuccessStatus = testSuccess;
    }

    /**
     * Returns the product test plan associated with the test plan.
     *
     * @return product test plan associated with the test plan
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Sets the product test plan associated with the test plan.
     *
     * @param product product test plan associated with the test plan
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Returns the product test plan associated with the test plan.
     *
     * @return product test plan associated with the test plan
     */
    public List<TestPlan> getTestPlans() {
        return testPlans;
    }

    /**
     * Sets the product test plan associated with the test plan.
     *
     * @param testPlans product test plan associated with the test plan
     */
    public void setTestPlans(List<TestPlan> testPlans) {
        this.testPlans = testPlans;
    }

    @Override
    public String toString() {
        return StringUtil.concatStrings("DeploymentPattern{",
                "id='", this.getId(), "\'",
                ", name='", name, "\'",
                ", product='", product, "\'",
                ", testSuccessStatus=", testSuccessStatus,
                '}');
    }
}
