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

import org.wso2.testgrid.dao.TestGridDAOException;

import java.io.Closeable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

/**
 * Abstract class containing the common behaviour of a database repository.
 *
 * @param <T> type of the entity the repository handles
 */
public abstract class AbstractRepository<T> implements Closeable {

    private final EntityManagerFactory entityManagerFactory;

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManagerFactory {@link EntityManagerFactory} instance
     */
    public AbstractRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Persists an entity in the database.
     *
     * @param entity entity to persist in the database
     * @throws TestGridDAOException thrown when error on persisting entity
     */
    void persist(T entity) throws TestGridDAOException {
        try {
            // Begin entity manager transaction
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();

            entityManager.persist(entity);

            // Commit transaction
            entityManager.getTransaction().commit();
            entityManager.close();
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
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();

            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));

            // Commit transaction
            entityManager.getTransaction().commit();
            entityManager.close();
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
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            T entity = entityManager.find(classType, id);
            entityManager.close();
            return entity;
        } catch (Exception e) {
            throw new TestGridDAOException("Error occurred when searching for entity.", e);
        }
    }

    /**
     * Find the entity by a specific field and field value.
     *
     * @param entityClass type of the entity to find
     * @param field       name of the database field to lookup
     * @param fieldValue  value of the field to be matched for
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    List<T> findByField(Class<T> entityClass, String field, Object fieldValue) throws TestGridDAOException {
        try {
            // From table name criteria
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<T> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(root);

            // Where criteria
            ParameterExpression<Object> params = criteriaBuilder.parameter(Object.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get(field), params));
            TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
            query.setParameter(params, fieldValue);

            List<T> resultList = query.getResultList();
            entityManager.close();
            return resultList;
        } catch (Exception e) {
            throw new TestGridDAOException("Error occurred when searching for entity.", e);
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
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityType);

            // Select all criteria
            Root<T> rootEntry = criteriaQuery.from(entityType);
            CriteriaQuery<T> criteriaQueryAll = criteriaQuery.select(rootEntry);
            TypedQuery<T> allQuery = entityManager.createQuery(criteriaQueryAll);

            List<T> resultList = allQuery.getResultList();
            entityManager.close();
            return resultList;
        } catch (Exception e) {
            throw new TestGridDAOException("Error occurred when searching for entity.", e);
        }
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }
}
