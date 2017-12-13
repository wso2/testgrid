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
import org.wso2.testgrid.common.Database;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * Repository class for {@link org.wso2.testgrid.common.Database} table.
 *
 * @since 1.0.0
 */
public class DatabaseRepository extends AbstractRepository<Database> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public DatabaseRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link Database} instance in the database.
     *
     * @param entity database to persist in the database
     * @return added or updated {@link Database} instance
     * @throws TestGridDAOException thrown when error on persisting the database instance
     */
    public Database persist(Database entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link Database} instance from database.
     *
     * @param entity database instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(Database entity) throws TestGridDAOException {
        super.delete(entity);
    }

    /**
     * Find a specific {@link Database} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link Database} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public Database findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(Database.class, id);
    }

    /**
     * Returns a list of {@link Database} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<Database> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(Database.class, params);
    }

    /**
     * Returns all the entries from the Database table.
     *
     * @return List<Database> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<Database> findAll() throws TestGridDAOException {
        return super.findAll(Database.class);
    }

    /**
     * Returns a list of {@link Database} instances ordered accordingly by the given fields.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @param fields map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of {@link Database} instances for the matched criteria ordered accordingly by the given fields
     */
    public List<Database> orderByFields(Map<String, Object> params, LinkedListMultimap<SortOrder, String> fields) {
        return super.orderByFields(Database.class, params, fields);
    }
}
