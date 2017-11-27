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

import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Repository class for {@link org.wso2.testgrid.common.InfraCombination} table.
 *
 * @since 1.0.0
 */
public class InfraCombinationRepository extends AbstractRepository<InfraCombination> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManagerFactory {@link EntityManagerFactory} instance
     */
    public InfraCombinationRepository(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    /**
     * Persists an {@link InfraCombination} instance in the database.
     *
     * @param entity InfraCombination to persist in the database
     * @throws TestGridDAOException thrown when error on persisting the InfraCombination instance
     */
    public InfraCombination persist(InfraCombination entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link InfraCombination} instance from database.
     *
     * @param entity InfraCombination instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(InfraCombination entity) throws TestGridDAOException {
        super.delete(entity);
    }

    /**
     * Find a specific {@link InfraCombination} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link InfraCombination} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public InfraCombination findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(InfraCombination.class, id);
    }

    /**
     * Returns a list of {@link InfraCombination} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<InfraCombination> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(InfraCombination.class, params);
    }

    /**
     * Returns all the entries from the InfraCombination table.
     *
     * @return List<InfraCombination> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<InfraCombination> findAll() throws TestGridDAOException {
        return super.findAll(InfraCombination.class);
    }
}
