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
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

/**
 * Abstract class containing the common behaviour of a database repository.
 *
 * @param <T> type of the entity the repository handles
 */
abstract class AbstractRepository<T> {

    private final EntityManager entityManager;

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    AbstractRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Persists or updates an entity in the database.
     *
     * @param entity entity to persist in the database
     * @return added or updated entity instance
     * @throws TestGridDAOException thrown when error on persisting entity
     */
    T persist(T entity) throws TestGridDAOException {
        try {
            // Begin entity manager transaction
            entityManager.getTransaction().begin();

            T merge = entityManager.merge(entity);

            // Commit transaction
            entityManager.getTransaction().commit();
            return merge;
        } catch (Exception e) {
            throw new TestGridDAOException("Error occurred when persisting entity in database.", e);
        }
    }

    /**
     * Removes an entity from database.
     *
     * @param entity entity to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    void delete(T entity) throws TestGridDAOException {
        try {
            // Begin entity manager transaction
            entityManager.getTransaction().begin();

            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));

            // Commit transaction
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            throw new TestGridDAOException("Error occurred when deleting entry from database.", e);
        }
    }

    /**
     * Find a specific entity from database of the given class type for the given primary key.
     *
     * @param classType type of the entity
     * @param id        primary key of the entity to be searched for
     * @return T of entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    T findByPrimaryKey(Class<T> classType, String id) throws TestGridDAOException {
        try {
            return entityManager.find(classType, id);
        } catch (Exception e) {
            throw new TestGridDAOException("Error occurred when searching for entity.", e);
        }
    }

    /**
     * Returns a list of the specified entity class matching the given criteria.
     *
     * @param entityClass type of the entity to find
     * @param params      parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    List<T> findByFields(Class<T> entityClass, Map<String, Object> params) throws TestGridDAOException {
        try {
            // From table name criteria
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<T> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(root);

            // Where criteria
            ParameterExpression<Object> parameterExpression = criteriaBuilder.parameter(Object.class);
            // In case if params are empty the query can be still valid
            TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                criteriaQuery.where(criteriaBuilder.equal(root.get(entry.getKey()), parameterExpression));
                query = entityManager.createQuery(criteriaQuery);
                query.setParameter(parameterExpression, entry.getValue());
            }
            return query.getResultList();
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil
                    .concatStrings("Error when searching for entities with the params: ", params), e);
        }
    }

    /**
     * Returns all the entries from the table matching the given entity type.
     * <p>
     * This function is similar to the SELECT * FROM [TABLE_NAME] in an SQL statement
     *
     * @param entityType type of the entity
     * @return List<T> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    List<T> findAll(Class<T> entityType) throws TestGridDAOException {
        try {
            // From table name criteria
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityType);

            // Select all criteria
            Root<T> rootEntry = criteriaQuery.from(entityType);
            CriteriaQuery<T> criteriaQueryAll = criteriaQuery.select(rootEntry);
            TypedQuery<T> allQuery = entityManager.createQuery(criteriaQueryAll);
            return allQuery.getResultList();
        } catch (Exception e) {
            throw new TestGridDAOException("Error occurred when searching for entity.", e);
        }
    }

    /**
     * Returns a result list ordered by the given fields.
     *
     * @param entityType type of the result list
     * @param params     parameters (map of field name and values) for obtaining the result list
     * @param fields     map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of values for the matched criteria ordered by the given fields
     */
    List<T> orderByFields(Class<T> entityType, Map<String, Object> params,
                          LinkedListMultimap<SortOrder, String> fields) {
        // From table name criteria
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityType);

        // Select all criteria
        Root<T> rootEntry = criteriaQuery.from(entityType);
        criteriaQuery.select(rootEntry);

        // Order by criteria
        List<Order> orderList = new ArrayList<>();
        for (Map.Entry<SortOrder, String> entry : fields.entries()) {
            if (entry.getKey().equals(SortOrder.ASCENDING)) {
                orderList.add(criteriaBuilder.asc(rootEntry.get(entry.getValue())));
            } else {
                orderList.add(criteriaBuilder.desc(rootEntry.get(entry.getValue())));
            }
        }
        criteriaQuery.orderBy(orderList);

        // Where criteria
        ParameterExpression<Object> parameterExpression = criteriaBuilder.parameter(Object.class);
        // In case if params are empty the query can be still valid
        TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            criteriaQuery.where(criteriaBuilder.equal(rootEntry.get(entry.getKey()), parameterExpression));
            query = entityManager.createQuery(criteriaQuery);
            query.setParameter(parameterExpression, entry.getValue());
        }
        return query.getResultList();
    }
}
