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
package org.wso2.testgrid.dao.repository;

import com.google.common.collect.LinkedListMultimap;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.ProductTestStatus;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Repository class for {@link Product} table.
 *
 * @since 1.0.0
 */
public class ProductRepository extends AbstractRepository<Product> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public ProductRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link Product} instance in the database.
     *
     * @param entity Product to persist in the database
     * @return added or updated {@link Product} instance
     * @throws TestGridDAOException thrown when error on persisting the Product instance
     */
    public Product persist(Product entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link Product} instance from database.
     *
     * @param entity Product instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(Product entity) throws TestGridDAOException {
        super.delete(entity);
        entity.setDeploymentPatterns(null);
    }

    /**
     * Find a specific {@link Product} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link Product} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public Product findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(Product.class, id);
    }

    /**
     * Returns a list of {@link Product} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<Product> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(Product.class, params);
    }

    /**
     * Returns all the entries from the Product table.
     *
     * @return List<Product> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<Product> findAll() throws TestGridDAOException {
        return super.findAll(Product.class);
    }

    /**
     * Returns a list of {@link Product} instances ordered accordingly by the given fields.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @param fields map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of {@link Product} instances for the matched criteria ordered accordingly by the given
     * fields
     */
    public List<Product> orderByFields(Map<String, Object> params,
                                               LinkedListMultimap<SortOrder, String> fields) {
        return super.orderByFields(Product.class, params, fields);
    }

    /**
     * Returns a list of {@link ProductTestStatus} instances matching the given criteria.
     *
     * @param date timestamp up to
     * @return result list after executing the query
     */
    public List<ProductTestStatus> getProductTestHistory(Timestamp date) throws TestGridDAOException {
        String queryStr = "SELECT p.id, p.name, p.version, p.channel, dp.id AS deploymentPatternId, dp.name AS " +
                "deploymentPattern, tp.status, tp.created_timestamp AS testExecutionTime FROM product AS p" +
                " INNER JOIN deployment_pattern AS dp ON p.id = dp.PRODUCT_id INNER JOIN test_plan AS tp ON " +
                "dp.id = tp.DEPLOYMENTPATTERN_id WHERE tp.created_timestamp >= '" + date + "' ORDER BY p.name, " +
                "p.version, p.channel, tp.created_timestamp DESC;";
        try {
            Query query = entityManager.createNativeQuery(queryStr);
            return this.getProductTestStatuses(query.getResultList());
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error on executing the native SQL" +
                    " query [", queryStr, "]"), e);
        }
    }

    private List<ProductTestStatus> getProductTestStatuses(List<Object[]> results) {
        List<ProductTestStatus> productTestStatuses = new ArrayList<>();

        for (Object []result : results) {
            productTestStatuses.add(new ProductTestStatus((String) result[0], (String) result[1], (String) result[2],
                                                             (String) result[3], (String) result[4],
                                                        (String) result[5], (String) result[6], (Timestamp) result[7]));
        }
        return productTestStatuses;
    }
}
