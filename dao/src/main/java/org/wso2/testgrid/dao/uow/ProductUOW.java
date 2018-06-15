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

import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.ProductTestStatus;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.ProductRepository;

import java.sql.Timestamp;
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
        this(EntityManagerHelper.getEntityManager());
    }

    /**
     * Constructs an instance of {@link ProductUOW} to manager use cases related to product test plan.
     */
    public ProductUOW(EntityManager entityManager) {
        productRepository = new ProductRepository(entityManager);
    }

    /**
     * Returns an instance of {@link Product} for the given product name and product version.
     *
     * @param name    product name
     * @return an instance of {@link Product} for the given product name and product version
     */
    public Optional<Product> getProduct(String name)
            throws TestGridDAOException {
        // Search criteria parameters
        Map<String, Object> params = new HashMap<>();
        params.put(Product.NAME_COLUMN, name);

        List<Product> products = productRepository.findByFields(params);
        if (products.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(products.get(0));
    }

    /**
     * Returns a List of {@link Product} instances.
     *
     * @return a List of {@link Product} instances
     */
    public List<Product> getProducts() throws TestGridDAOException {
        return productRepository.findAll();
    }

    /**
     * This method persists a {@link Product} instance to the database.
     *
     * @param name    product name
     * @return persisted {@link Product} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public Product persistProduct(String name) throws TestGridDAOException {
        Optional<Product> optionalProduct = getProduct(name);
        if (optionalProduct.isPresent()) {
            return optionalProduct.get();
        }

        // Create a new product and persist if the product doesn't exist already.
        Product product = new Product();
        product.setName(name);
        return productRepository.persist(product);
    }

    /**
     * Returns a List of {@link ProductTestStatus} instances which includes all the products with their test history
     * status up to the given date.
     *
     * @param date date up to the history should be looked
     * @return a List of {@link ProductTestStatus} instances
     */
    public List<ProductTestStatus> getProductTestHistory(Timestamp date) throws TestGridDAOException {
        return productRepository.getProductTestHistory(date);
    }

    /**
     * This method update the last success timestamp or last failure timestamp according to the product status.
     *
     * @param status status of the product
     * @param productId Id of the product
     */
    public void updateProductStatusTimestamp(Status status, String productId) throws TestGridDAOException {
        productRepository.updateProductStatusTimestamp(status, new Timestamp(System.currentTimeMillis()), productId);
    }
}
