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

import org.wso2.testgrid.common.infrastructure.AWSResourceLimit;
import org.wso2.testgrid.common.infrastructure.AWSResourceRequirement;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

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
     * Returns the list of distinct AWS regions available in the database.
     *
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
     * Returns an available region for the test plan.
     *
     * @param resourceRequirements list of AWS resource requirements
     * @return available region
     * @throws TestGridDAOException if persistence to database fails
     */
    public String getAvailableRegion(List<AWSResourceRequirement> resourceRequirements) throws TestGridDAOException {
        boolean isRegionAvailable;

        String selectQuery = "SELECT * from aws_resource_limit where service_name=? AND " +
                "limit_name=? AND region=? AND (max_allowed_limit - current_usage) >= ? FOR UPDATE;";
        String updateQuery = "UPDATE aws_resource_limit a SET a.current_usage = a.current_usage + ? " +
                "WHERE a.service_name=? AND a.limit_name=? AND a.region=?";
        List resultList;
        EntityTransaction transaction;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            List<String> regions = findRegions();
            for (String region : regions) {
                isRegionAvailable = true;
                for (AWSResourceRequirement resourceRequirement : resourceRequirements) {
                    resultList = entityManager.createNativeQuery(selectQuery, AWSResourceLimit.class)
                            .setParameter(1, resourceRequirement.getServiceName())
                            .setParameter(2, resourceRequirement.getLimitName())
                            .setParameter(3, region)
                            .setParameter(4, resourceRequirement.getRequiredCount())
                            .getResultList();

                    //Go to next region if a service is not available
                    if (resultList.isEmpty()) {
                        isRegionAvailable = false;
                        break;
                    }
                }
                //Acquire resources if region is available
                if (isRegionAvailable) {
                    for (AWSResourceRequirement resourceRequirement : resourceRequirements) {
                        entityManager.createNativeQuery(updateQuery)
                                .setParameter(1, resourceRequirement.getRequiredCount())
                                .setParameter(2, resourceRequirement.getServiceName())
                                .setParameter(3, resourceRequirement.getLimitName())
                                .setParameter(4, region)
                                .executeUpdate();
                    }
                    transaction.commit();
                    return region;
                }
            }
        } catch (Exception e) {
            throw new TestGridDAOException("Error while executing query on database. ", e);
        }
        transaction.commit();
        return null;
    }

    /**
     * Persists initial AWS resource limits.
     *
     * @param awsResourceLimitsList list of all available resources with their maximum limits
     * @return list of persisted resources
     * @throws TestGridDAOException if persistence fails
     */
    public List<AWSResourceLimit> persistInitialLimits(
            List<AWSResourceLimit> awsResourceLimitsList) throws TestGridDAOException {
        EntityTransaction transaction;

        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            for (AWSResourceLimit awsResourceLimit : awsResourceLimitsList) {
                Map<String, Object> params = new HashMap<>();
                params.put(AWSResourceLimit.REGION_COLUMN, awsResourceLimit.getRegion());
                params.put(AWSResourceLimit.SERVICE_NAME_COLUMN, awsResourceLimit.getServiceName());
                params.put(AWSResourceLimit.LIMIT_NAME_COLUMN, awsResourceLimit.getLimitName());
                if (findByFields(params).isEmpty()) {
                    entityManager.persist(awsResourceLimit);
                }
            }
            transaction.commit();
            return awsResourceLimitsList;
        } catch (Exception e) {
            throw new TestGridDAOException("Error while executing query on database. ", e);
        }
    }

    /**
     * Releases acquired AWS resources.
     *
     * @param resourceRequirements list of resources to release
     * @param region AWS region to release from
     * @throws TestGridDAOException if persistence to database fails
     */
    public void releaseResources(List<AWSResourceRequirement> resourceRequirements, String region)
            throws TestGridDAOException {
        String updateQuery = "UPDATE aws_resource_limit a SET a.current_usage = a.current_usage - ? " +
                "WHERE a.service_name=? AND a.limit_name=? AND a.region=?";
        EntityTransaction transaction;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            for (AWSResourceRequirement resourceRequirement : resourceRequirements) {
                entityManager.createNativeQuery(updateQuery)
                        .setParameter(1, resourceRequirement.getRequiredCount())
                        .setParameter(2, resourceRequirement.getServiceName())
                        .setParameter(3, resourceRequirement.getLimitName())
                        .setParameter(4, region)
                        .executeUpdate();
            }
            transaction.commit();
        } catch (Exception e) {
            throw new TestGridDAOException("Error while executing native query on database. ", e);
        }
    }
}
