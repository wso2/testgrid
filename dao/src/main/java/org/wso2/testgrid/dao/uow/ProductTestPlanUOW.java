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
package org.wso2.testgrid.dao.uow;

import com.google.common.collect.LinkedListMultimap;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.ProductTestPlanRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class defines the Unit of work related to a {@link ProductTestPlan}.
 *
 * @since 1.0.0
 */
public class ProductTestPlanUOW {

    private final ProductTestPlanRepository productTestPlanRepository;

    /**
     * Constructs an instance of {@link ProductTestPlanUOW} to manager use cases related to product test plan.
     */
    public ProductTestPlanUOW() {
        productTestPlanRepository = new ProductTestPlanRepository();
    }

    /**
     * Returns an instance of {@link ProductTestPlan} for the given product name and product version.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product test plan channel
     * @return an instance of {@link ProductTestPlan} for the given product name and product version
     */
    public Optional<ProductTestPlan> getProductTestPlan(String productName, String productVersion,
                                                        ProductTestPlan.Channel channel) {
        // Search criteria parameters
        Map<String, Object> params = new HashMap<>();
        params.put(ProductTestPlan.PRODUCT_NAME_COLUMN, productName);
        params.put(ProductTestPlan.PRODUCT_VERSION_COLUMN, productVersion);
        params.put(ProductTestPlan.CHANNEL_COLUMN, channel);

        // Ordering criteria
        LinkedListMultimap<SortOrder, String> orderFieldMap = LinkedListMultimap.create();
        orderFieldMap.put(SortOrder.DESCENDING, ProductTestPlan.MODIFIED_TIMESTAMP_COLUMN);

        List<ProductTestPlan> productTestPlans = productTestPlanRepository.orderByFields(params, orderFieldMap);
        if (productTestPlans.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(productTestPlans.get(0)); // First element in the list is the latest product test plan
    }

    /**
     * Returns all the product test plans.
     *
     * @return list of {@link ProductTestPlan} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public List<ProductTestPlan> getAllProductTestPlans() throws TestGridDAOException {
        return productTestPlanRepository.findAll();
    }

    /**
     * Returns the {@link ProductTestPlan} instance for the given id.
     *
     * @param id primary key of the product test plan
     * @return matching {@link ProductTestPlan} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public ProductTestPlan getProductTestPlanById(String id) throws TestGridDAOException {
        return productTestPlanRepository.findByPrimaryKey(id);
    }

    /**
     * This method persists a {@link ProductTestPlan} to the database.
     *
     * @param productTestPlan Populated ProductTestPlan object
     * @return persisted {@link ProductTestPlan} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public ProductTestPlan persistProductTestPlan(ProductTestPlan productTestPlan) throws TestGridDAOException {
        return productTestPlanRepository.persist(productTestPlan);
    }
}
