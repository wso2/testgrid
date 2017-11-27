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

import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Repository class for {@link org.wso2.testgrid.common.TestCase} table.
 *
 * @since 1.0.0
 */
public class TestCaseRepository extends AbstractRepository<TestCase> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManagerFactory {@link EntityManagerFactory} instance
     */
    public TestCaseRepository(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    /**
     * Persists an {@link TestCase} instance in the database.
     *
     * @param entity TestCase to persist in the database
     * @throws TestGridDAOException thrown when error on persisting the TestCase instance
     */
    public TestCase persist(TestCase entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link TestCase} instance from database.
     *
     * @param entity TestCase instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(TestCase entity) throws TestGridDAOException {
        super.delete(entity);
    }

    /**
     * Find a specific {@link TestCase} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link TestCase} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public TestCase findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(TestCase.class, id);
    }

    /**
     * Returns a list of {@link TestCase} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<TestCase> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(TestCase.class, params);
    }

    /**
     * Returns all the entries from the TestCase table.
     *
     * @return List<TestCase> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<TestCase> findAll() throws TestGridDAOException {
        return super.findAll(TestCase.class);
    }
}
