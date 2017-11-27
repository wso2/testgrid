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

import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Repository class for {@link org.wso2.testgrid.common.ProductTestPlan} table.
 *
 * @since 1.0.0
 */
public class ProductTestPlanRepository extends AbstractRepository<ProductTestPlan> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManagerFactory {@link EntityManagerFactory} instance
     */
    public ProductTestPlanRepository(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    /**
     * Persists an {@link ProductTestPlan} instance in the database.
     *
     * @param entity ProductTestPlan to persist in the database
     * @throws TestGridDAOException thrown when error on persisting the ProductTestPlan instance
     */
    public ProductTestPlan persist(ProductTestPlan entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link ProductTestPlan} instance from database.
     *
     * @param entity ProductTestPlan instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(ProductTestPlan entity) throws TestGridDAOException {
        super.delete(entity);
    }

    /**
     * Find a specific {@link ProductTestPlan} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link ProductTestPlan} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public ProductTestPlan findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(ProductTestPlan.class, id);
    }

    /**
     * Returns a list of {@link ProductTestPlan} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<ProductTestPlan> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(ProductTestPlan.class, params);
    }

    /**
     * Returns all the entries from the ProductTestPlan table.
     *
     * @return List<ProductTestPlan> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<ProductTestPlan> findAll() throws TestGridDAOException {
        return super.findAll(ProductTestPlan.class);
    }
}
