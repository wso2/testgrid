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
 *
 */
package org.wso2.testgrid.dao.repository;

import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.specification.Specification;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Repository class for {@link InfrastructureParameter} table.
 *
 * @since 1.0.0
 */
public class InfrastructureParameterRepository extends AbstractRepository<InfrastructureParameter> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public InfrastructureParameterRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link InfrastructureParameter} instance in the database.
     *
     * @param entity InfrastructureParameter to persist in the database
     * @return added or updated {@link InfrastructureParameter} instance
     * @throws TestGridDAOException thrown when error on persisting the InfrastructureParameter instance
     */
    public InfrastructureParameter persist(InfrastructureParameter entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link InfrastructureParameter} instance from database.
     *
     * @param entity InfrastructureParameter instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(InfrastructureParameter entity) throws TestGridDAOException {
        super.delete(entity);
    }

    /**
     * Query the database according to the {@link Specification} and return the result-set as the U type.
     *
     * Most of the time, U will be {@link InfrastructureParameter}. You may also have any field within
     * {@link InfrastructureParameter} as U as well.
     *
     * @param spec specification that a given query need to adhere to.
     * @return List of {@link U}s.
     * @throws TestGridDAOException thrown when an error occurred while querying.
     */
    public <U> List<U> find(Specification<InfrastructureParameter, U> spec, Class<U> clazz) throws
            TestGridDAOException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<U> query = criteriaBuilder.createQuery(clazz);
        Root<InfrastructureParameter> root = query.from(InfrastructureParameter.class);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
        query.where(predicate);
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Find a specific {@link InfrastructureParameter} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link InfrastructureParameter} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public InfrastructureParameter findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(InfrastructureParameter.class, id);
    }

    /**
     * Returns a list of {@link InfrastructureParameter} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<InfrastructureParameter> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(InfrastructureParameter.class, params);
    }

    /**
     * Returns all the entries from the InfrastructureParameter table.
     *
     * @return List<InfrastructureParameter> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<InfrastructureParameter> findAll() throws TestGridDAOException {
        return super.findAll(InfrastructureParameter.class);
    }

}
