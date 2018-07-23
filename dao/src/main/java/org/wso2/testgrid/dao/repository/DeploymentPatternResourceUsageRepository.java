/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.testgrid.common.infrastructure.DeploymentPatternResourceUsage;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * Repository class for {@link DeploymentPatternResourceUsage} table.
 *
 * @since 1.0.0
 */
public class DeploymentPatternResourceUsageRepository extends
        AbstractRepository<DeploymentPatternResourceUsage> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public DeploymentPatternResourceUsageRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link DeploymentPatternResourceUsage} instance in the database.
     *
     * @param entity TestPlan to persist in the database
     * @return added or updated {@link DeploymentPatternResourceUsage} instance
     * @throws TestGridDAOException thrown when error on persisting the TestPlan instance
     */
    public DeploymentPatternResourceUsage persist(DeploymentPatternResourceUsage entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link DeploymentPatternResourceUsage} instance from database.
     *
     * @param entity TestPlan instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(DeploymentPatternResourceUsage entity) throws TestGridDAOException {
        super.delete(entity);
    }

    /**
     * Find a specific {@link DeploymentPatternResourceUsage} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link DeploymentPatternResourceUsage} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public DeploymentPatternResourceUsage findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(DeploymentPatternResourceUsage.class, id);
    }

    /**
     * Returns a list of {@link DeploymentPatternResourceUsage} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<DeploymentPatternResourceUsage> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(DeploymentPatternResourceUsage.class, params);
    }

    /**
     * Returns all the entries from the DeploymentPatternResourceUsage table.
     *
     * @return List<TestPlan> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<DeploymentPatternResourceUsage> findAll() throws TestGridDAOException {
        return super.findAll(DeploymentPatternResourceUsage.class);
    }

    /**
     * Returns a list of {@link DeploymentPatternResourceUsage} instances ordered accordingly by the given fields.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @param fields map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of {@link DeploymentPatternResourceUsage} instances for the matched criteria
     *         ordered accordingly by the
     * given fields
     */
    public List<DeploymentPatternResourceUsage> orderByFields(Map<String, Object> params,
                                                              LinkedListMultimap<SortOrder, String> fields) {
        return super.orderByFields(DeploymentPatternResourceUsage.class, params, fields);
    }
}
