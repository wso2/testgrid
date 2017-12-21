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

import java.util.List;

/**
 * This represents a model of the Product with its recent test statuses.
 *
 * @since 1.0.0
 */
public class ProductTestStatus {

    private Product product;
    private List<TestPlan> testPlans;

    /**
     * Returns the product.
     *
     * @return product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Sets the product.
     *
     * @param product product
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Returns the recent test-plans associated with this product.
     *
     * @return recent test-plans associated with this product
     */
    public List<TestPlan> getTestPlans() {
        return testPlans;
    }


    /**
     * Sets the recent test-plans associated with this product.
     *
     * @param testPlans recent test-plans associated with this product
     */
    public void setTestPlans(List<TestPlan> testPlans) {
        this.testPlans = testPlans;
    }
}
