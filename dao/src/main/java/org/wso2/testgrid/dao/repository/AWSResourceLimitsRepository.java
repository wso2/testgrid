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
import org.wso2.testgrid.common.infrastructure.AWSResourceLimit;
import org.wso2.testgrid.common.infrastructure.DeploymentPatternResourceUsage;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * Repository class for {@link AWSResourceLimit} table.
 *
 * @since 1.0.0
 */
public class AWSResourceLimitsRepository extends AbstractRepository<AWSResourceLimit> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public AWSResourceLimitsRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link AWSResourceLimit} instance in the database.
     *
     * @param entity AWSResourceLimit to persist in the database
     * @return added or updated {@link AWSResourceLimit} instance
     * @throws TestGridDAOException thrown when error on persisting
     */
    public AWSResourceLimit persist(AWSResourceLimit entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link AWSResourceLimit} instance from database.
     *
     * @param entity AWSResourceLimit instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(AWSResourceLimit entity) throws TestGridDAOException {
        super.delete(entity);
    }

    /**
     * Find a specific {@link AWSResourceLimit} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link AWSResourceLimit} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public AWSResourceLimit findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(AWSResourceLimit.class, id);
    }

    /**
     * Returns a list of {@link AWSResourceLimit} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<AWSResourceLimit> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(AWSResourceLimit.class, params);
    }

    /**
     * Returns all the entries from the AWSResourceLimit table.
     *
     * @return List<TestPlan> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<AWSResourceLimit> findAll() throws TestGridDAOException {
        return super.findAll(AWSResourceLimit.class);
    }

    /**
     * Returns a list of {@link AWSResourceLimit} instances ordered accordingly by the given fields.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @param fields map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of {@link AWSResourceLimit} instances for the matched criteria ordered accordingly by the
     * given fields
     */
    public List<AWSResourceLimit> orderByFields(Map<String, Object> params,
                                                LinkedListMultimap<SortOrder, String> fields) {
        return super.orderByFields(AWSResourceLimit.class, params, fields);
    }

    /**
     * Returns the list of distinct AWS regions available in the database
     * @return String List of distinct regions
     * @throws TestGridDAOException
     */
    public List<String> findRegions() throws TestGridDAOException {
        String queryStr = "SELECT DISTINCT region from aws_resource_limit";
        try {
            @SuppressWarnings("unchecked")
            List<String> regions = (List<String>) entityManager.createNativeQuery(queryStr).getResultList();
            return regions;
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error on executing the native SQL " +
                    "query [", queryStr, "]"), e);
        }
    }

    /**
     * Returns a {@link AWSResourceLimit} in a particular region if available
     * @param resourceUsage an instance of {@link DeploymentPatternResourceUsage}
     * @param region an AWS region
     * @return an instance of {@link AWSResourceLimit}
     * @throws TestGridDAOException
     */
    public AWSResourceLimit getAvailableResource(DeploymentPatternResourceUsage resourceUsage, String region)
            throws TestGridDAOException {
        String queryStr = "SELECT * from aws_resource_limit where service_name=? AND " +
                "limit_name=? AND region=? AND (max_allowed_limit-current_usage) >= ?;";
        try {
            @SuppressWarnings("unchecked")
            List resultList = entityManager.createNativeQuery(queryStr, AWSResourceLimit.class)
                    .setParameter(1, resourceUsage.getServiceName())
                    .setParameter(2, resourceUsage.getLimitName())
                    .setParameter(3, region)
                    .setParameter(4, resourceUsage.getRequiredCount())
                    .getResultList();
            if (!resultList.isEmpty()) {
                return (AWSResourceLimit) EntityManagerHelper.refreshResult(entityManager, resultList.get(0));
            }
            return null;
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error on executing the native SQL " +
                    "query [", queryStr, "]"), e);
        }
    }
}
