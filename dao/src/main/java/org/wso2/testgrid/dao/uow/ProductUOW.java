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
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.ProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;

/**
 * This class defines the Unit of work related to a {@link Product}.
 *
 * @since 1.0.0
 */
public class ProductUOW {

    private final ProductRepository productRepository;

    /**
     * Constructs an instance of {@link ProductUOW} to manager use cases related to product test plan.
     */
    public ProductUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        productRepository = new ProductRepository(entityManager);
    }

    /**
     * Returns an instance of {@link Product} for the given product name and product version.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product test plan channel
     * @return an instance of {@link Product} for the given product name and product version
     */
    public Optional<Product> getProduct(String productName, String productVersion,
                                                        Product.Channel channel) throws TestGridDAOException {
        // Search criteria parameters
        Map<String, Object> params = new HashMap<>();
        params.put(Product.PRODUCT_NAME_COLUMN, productName);
        params.put(Product.PRODUCT_VERSION_COLUMN, productVersion);
        params.put(Product.CHANNEL_COLUMN, channel);

        List<Product> products = productRepository.findByFields(params);
        if (products.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(products.get(0));
    }

    /**
     * Returns all the product test plans.
     *
     * @return list of {@link Product} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public List<Product> getAllProductTestPlans() throws TestGridDAOException {
        return productRepository.findAll();
    }

    /**
     * Returns the {@link Product} instance for the given id.
     *
     * @param id primary key of the product test plan
     * @return matching {@link Product} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public Product getProductTestPlanById(String id) throws TestGridDAOException {
        return productRepository.findByPrimaryKey(id);
    }

    /**
     * This method persists a {@link Product} to the database.
     *
     * @param product Populated Product object
     * @return persisted {@link Product} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public Product persistProduct(Product product) throws TestGridDAOException {
        return productRepository.persist(product);
    }

    //
    public List getProductSummary(String sqlQuery) throws TestGridDAOException {
        return productRepository.executeTypedQuery(sqlQuery);
    }
}
