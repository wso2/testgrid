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
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.DeploymentPatternTestFailureStat;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Repository class for {@link DeploymentPattern} table.
 *
 * @since 1.0.0
 */
public class DeploymentPatternRepository extends AbstractRepository<DeploymentPattern> {

    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public DeploymentPatternRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link DeploymentPattern} instance in the database.
     *
     * @param entity TestPlan to persist in the database
     * @return added or updated {@link DeploymentPattern} instance
     * @throws TestGridDAOException thrown when error on persisting the TestPlan instance
     */
    public DeploymentPattern persist(DeploymentPattern entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link DeploymentPattern} instance from database.
     *
     * @param entity TestPlan instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(DeploymentPattern entity) throws TestGridDAOException {
        super.delete(entity);
        entity.setTestPlans(null);
    }

    /**
     * Find a specific {@link DeploymentPattern} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link DeploymentPattern} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public DeploymentPattern findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(DeploymentPattern.class, id);
    }

    /**
     * Returns a list of {@link DeploymentPattern} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<DeploymentPattern> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(DeploymentPattern.class, params);
    }

    /**
     * Returns all the entries from the TestPlan table.
     *
     * @return List<TestPlan> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<DeploymentPattern> findAll() throws TestGridDAOException {
        return super.findAll(DeploymentPattern.class);
    }

    /**
     * Returns a list of {@link DeploymentPattern} instances ordered accordingly by the given fields.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @param fields map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of {@link DeploymentPattern} instances for the matched criteria ordered accordingly by the
     * given fields
     */
    public List<DeploymentPattern> orderByFields(Map<String, Object> params,
                                                 LinkedListMultimap<SortOrder, String> fields) {
        return super.orderByFields(DeploymentPattern.class, params, fields);
    }

    /**
     * Returns a list of distinct {@link DeploymentPattern} instances for given product-id and test ran before given
     * date.
     *
     * @param productId product id of the associated product
     * @param date tests ran before date
     * @return a list of {@link DeploymentPattern} instances for given product-id and created after given date
     */
    public List<DeploymentPattern> findByProductAndDate(String productId, Timestamp date)
            throws TestGridDAOException {
        String queryStr = "SELECT dp.id, dp.name, dp.product_id FROM deployment_pattern AS dp INNER JOIN testplan " +
                "AS tp ON dp.id = tp.deployment_pattern_id where tp.created_time <= '" + date + "' AND " +
                "dp.product_id = '" + productId + "' GROUP BY dp.id;";
        try {
            Query query = entityManager.createNativeQuery(queryStr, DeploymentPattern.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error on executing the native SQL query " +
                    "[", queryStr, "]"), e);
        }
    }

    /**
     * Returns a list of distinct {@link DeploymentPattern} instances for given product-id and test ran before given
     * date.
     *
     * @param productId product id of the associated product
     * @param date tests ran before date
     * @return a list of {@link DeploymentPattern} instances for given product-id and created after given date
     */
    public List<DeploymentPatternTestFailureStat> findFailedTestCounts(String productId, Timestamp date)
            throws TestGridDAOException {
        String queryStr = "SELECT tp.deployment_pattern_id AS deploymentPatternId, COUNT(*) AS failureCount FROM " +
                "testplan tp WHERE tp.deployment_pattern_id IN (SELECT id from deployment_pattern where product_id = '"
                + productId + "') AND tp.created_time <= '" + date + "' AND tp.status = 'fail' GROUP BY " +
                "tp.deployment_pattern_id;";
        try {
            Query query = entityManager.createNativeQuery(queryStr, DeploymentPatternTestFailureStat.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error on executing the native SQL query " +
                    "[", queryStr, "]"), e);
        }
    }
}
