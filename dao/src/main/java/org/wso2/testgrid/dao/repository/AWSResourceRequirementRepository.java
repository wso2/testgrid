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
import org.wso2.testgrid.common.infrastructure.AWSResourceRequirement;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Repository class for {@link AWSResourceRequirement} table.
 *
 * @since 1.0.0
 */
public class AWSResourceRequirementRepository extends
        AbstractRepository<AWSResourceRequirement> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public AWSResourceRequirementRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link AWSResourceRequirement} instance in the database.
     *
     * @param entity TestPlan to persist in the database
     * @return added or updated {@link AWSResourceRequirement} instance
     * @throws TestGridDAOException thrown when error on persisting the TestPlan instance
     */
    public AWSResourceRequirement persist(AWSResourceRequirement entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Persists an {@link AWSResourceRequirement} instance in the database.
     *
     * @param resourceRequirementList The list of {@link AWSResourceRequirement} to persist to the database
     * @throws TestGridDAOException thrown when error on persisting
     */
    public void persistResourceRequirements(List<AWSResourceRequirement> resourceRequirementList)
            throws TestGridDAOException {
        String selectQuery = "SELECT * FROM aws_resource_requirement WHERE cfn_md5_hash=? FOR UPDATE;";
        List resultList;
        try {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            resultList = entityManager.createNativeQuery(selectQuery, AWSResourceRequirement.class)
                    .setParameter(1, resourceRequirementList.get(0).getCfnMD5Hash())
                    .getResultList();
            if (!resultList.isEmpty()) {
                resourceRequirementList = EntityManagerHelper.refreshResultList(entityManager, resultList);
            }
            for (AWSResourceRequirement resourceRequirement : resourceRequirementList) {
                entityManager.persist(resourceRequirement);
            }
            transaction.commit();
        } catch (Exception e) {
            throw new TestGridDAOException("Error while executing query in database", e);
        }
    }

    /**
     * Removes an {@link AWSResourceRequirement} instance from database.
     *
     * @param entity TestPlan instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(AWSResourceRequirement entity) throws TestGridDAOException {
        super.delete(entity);
    }

    /**
     * Find a specific {@link AWSResourceRequirement} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link AWSResourceRequirement} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public AWSResourceRequirement findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(AWSResourceRequirement.class, id);
    }

    /**
     * Returns a list of {@link AWSResourceRequirement} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<AWSResourceRequirement> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(AWSResourceRequirement.class, params);
    }

    /**
     * Returns all the entries from the AWSResourceRequirement table.
     *
     * @return List<TestPlan> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<AWSResourceRequirement> findAll() throws TestGridDAOException {
        return super.findAll(AWSResourceRequirement.class);
    }

    /**
     * Returns a list of {@link AWSResourceRequirement} instances ordered accordingly by the given fields.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @param fields map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of {@link AWSResourceRequirement} instances for the matched criteria
     *         ordered accordingly by the
     * given fields
     */
    public List<AWSResourceRequirement> orderByFields(Map<String, Object> params,
                                                      LinkedListMultimap<SortOrder, String> fields) {
        return super.orderByFields(AWSResourceRequirement.class, params, fields);
    }
}
