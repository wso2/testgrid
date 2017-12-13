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
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * Repository class for {@link org.wso2.testgrid.common.TestPlan} table.
 *
 * @since 1.0.0
 */
public class TestPlanRepository extends AbstractRepository<TestPlan> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public TestPlanRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link TestPlan} instance in the database.
     *
     * @param entity TestPlan to persist in the database
     * @return added or updated {@link TestPlan} instance
     * @throws TestGridDAOException thrown when error on persisting the TestPlan instance
     */
    public TestPlan persist(TestPlan entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link TestPlan} instance from database.
     *
     * @param entity TestPlan instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(TestPlan entity) throws TestGridDAOException {
        super.delete(entity);
        entity.setTestScenarios(null);
        entity.setInfraResult(null);
    }

    /**
     * Find a specific {@link TestPlan} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link TestPlan} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public TestPlan findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(TestPlan.class, id);
    }

    /**
     * Returns a list of {@link TestPlan} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<TestPlan> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(TestPlan.class, params);
    }

    /**
     * Returns all the entries from the TestPlan table.
     *
     * @return List<TestPlan> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<TestPlan> findAll() throws TestGridDAOException {
        return super.findAll(TestPlan.class);
    }

    /**
     * Returns a list of {@link TestPlan} instances ordered accordingly by the given fields.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @param fields map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of {@link TestPlan} instances for the matched criteria ordered accordingly by the given fields
     */
    public List<TestPlan> orderByFields(Map<String, Object> params, LinkedListMultimap<SortOrder, String> fields) {
        return super.orderByFields(TestPlan.class, params, fields);
    }
}
