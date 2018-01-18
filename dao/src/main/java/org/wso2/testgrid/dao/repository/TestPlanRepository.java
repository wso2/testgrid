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


import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Repository class for {@link TestPlan} table.
 *
 * @since 1.0.0
 */
public class TestPlanRepository extends AbstractRepository<TestPlan> {



    /**
     * Constructs an instance of the repository class.
     *
     * @param entityManager {@link EntityManager} instance
     */
    public TestPlanRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Persists an {@link TestPlan} instance in the database.
     *
     * @param entity TestPlan to persist in the database
     * @return added or updated {@link TestPlan} instance
     * @throws TestGridDAOException thrown when error on persisting the TestPlan instance
     */
    public TestPlan persist(TestPlan entity) throws TestGridDAOException {
        return super.persist(entity);
    }

    /**
     * Removes an {@link TestPlan} instance from database.
     *
     * @param entity TestPlan instance to be removed from database.
     * @throws TestGridDAOException thrown when error on removing entry from database
     */
    public void delete(TestPlan entity) throws TestGridDAOException {
        super.delete(entity);
        entity.setTestScenarios(null);
        entity.setInfraParameters(null);
    }

    /**
     * Find a specific {@link TestPlan} instance from database for the given primary key.
     *
     * @param id primary key of the entity to be searched for
     * @return instance of an {@link TestPlan} matching the given primary key
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public TestPlan findByPrimaryKey(String id) throws TestGridDAOException {
        return findByPrimaryKey(TestPlan.class, id);
    }

    /**
     * Returns a list of {@link TestPlan} instances matching the given criteria.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @return a list of values for the matched criteria
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<TestPlan> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return super.findByFields(TestPlan.class, params);
    }

    /**
     * Returns all the entries from the TestPlan table.
     *
     * @return List<TestPlan> all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<TestPlan> findAll() throws TestGridDAOException {
        return super.findAll(TestPlan.class);
    }

    /**
     * Returns a list of {@link TestPlan} instances ordered accordingly by the given fields.
     *
     * @param params parameters (map of field name and values) for obtaining the result list
     * @param fields map of fields [Ascending / Descending, Field name> to sort ascending or descending
     * @return a list of {@link TestPlan} instances for the matched criteria ordered accordingly by the given fields
     */
    public List<TestPlan> orderByFields(Map<String, Object> params, LinkedListMultimap<SortOrder, String> fields) {
        return super.orderByFields(TestPlan.class, params, fields);
    }

    /**
     * Returns a list of distinct {@link TestPlan} instances for given deploymentId and created before given date.
     *
     * @param deploymentId deployment id of the required TestPlans
     * @param date         created before date
     * @return a list of {@link TestPlan} instances for given deploymentId and created after given date
     */
    public List<TestPlan> findByDeploymentPatternAndDate(String deploymentId, Timestamp date) throws
            TestGridDAOException {
        String queryStr = "SELECT tp.id, tp.DEPLOYMENTPATTERN_id, tp.infra_parameters, tp.status FROM (SELECT " +
                "infra_parameters, max(created_timestamp) AS maxtime, DEPLOYMENTPATTERN_id FROM test_plan WHERE " +
                "created_timestamp <= '" + date + "' AND DEPLOYMENTPATTERN_id = '" + deploymentId + "' GROUP BY " +
                "infra_parameters) AS r INNER JOIN test_plan AS tp on tp.infra_parameters = r.infra_parameters AND " +
                "tp.created_timestamp = r.maxtime AND tp.DEPLOYMENTPATTERN_id = r.DEPLOYMENTPATTERN_id;";
        try {
            Query query = entityManager.createNativeQuery(queryStr, TestPlan.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error on executing the native SQL" +
                    " query [", queryStr, "]"), e);
        }
    }

    /**
     * This method returns the last failed {@link TestPlan} for a given product.
     *
     * @param product A Product object being queried.
     * @return instance of a {@link TestPlan} representing the last failed test plan.
     * @throws TestGridDAOException when an error occurs while executing the native sql query.
     */
    public TestPlan getLastFailure(Product product) throws TestGridDAOException {
        String sql = "SELECT  t.* from test_plan t INNER JOIN (SELECT tp.infra_parameters," +
                "max(tp.modified_timestamp) AS time, dp.name FROM test_plan tp INNER JOIN " +
                "deployment_pattern dp ON tp.DEPLOYMENTPATTERN_id=dp.id AND tp.status='FAIL' " +
                "AND  tp.DEPLOYMENTPATTERN_id IN (SELECT id FROM deployment_pattern WHERE " +
                "PRODUCT_id = ?) GROUP BY tp.infra_parameters,dp.name) as x ON " +
                "t.infra_parameters=x.infra_parameters AND t.modified_timestamp=x.time ORDER BY time DESC LIMIT 1";
        try {
            return (TestPlan) entityManager.createNativeQuery(sql, TestPlan.class)
                    .setParameter(1 , product.getId())
                    .getSingleResult();
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings(" Error occured while executing the SQL " +
                    "query [", sql, "]"), e);
        }
    }

    /**
     * This method returns the last build TestPlan for a given product.
     *
     * @param product the product being queried
     * @return instance of {@link TestPlan} for the last build
     * @throws TestGridDAOException when an error occurs executing the native query
     */
    public TestPlan getLastBuild(Product product) throws TestGridDAOException {
        String sql = "select  t.* from test_plan t inner join (select tp.infra_parameters," +
                "max(tp.modified_timestamp) AS time, dp.name from test_plan tp inner join " +
                "deployment_pattern dp on tp.DEPLOYMENTPATTERN_id=dp.id and  tp.DEPLOYMENTPATTERN_id " +
                "in (select id from deployment_pattern where PRODUCT_id=?)" +
                "group by tp.infra_parameters,dp.name) AS x on t.infra_parameters=x.infra_parameters " +
                "AND t.modified_timestamp=x.time order by time desc limit 1";
        try {
            return (TestPlan) entityManager.createNativeQuery(sql, TestPlan.class)
                    .setParameter(1, product.getId())
                    .getSingleResult();
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error occured while executing SQL " +
                    "query [ ", sql, " ]"), e);
        }
    }

    /**
     * This method retrives a list of TestPlans for a given product that represent the latest builds for
     * distinct infra combinations.
     *
     * @param product the product being queried
     * @return a list of {@link TestPlan}
     * @throws TestGridDAOException when an error occurs while executing the native query
     */
    public List<TestPlan> getLatestTestPlans(Product product) throws TestGridDAOException {
        String sql = "select t.* from test_plan t inner join (select tp.infra_parameters," +
                "max(tp.modified_timestamp) AS time, dp.name from test_plan tp inner join " +
                "deployment_pattern dp on tp.DEPLOYMENTPATTERN_id=dp.id  and tp.DEPLOYMENTPATTERN_id in " +
                "(select id from deployment_pattern where PRODUCT_id= ? ) " +
                "group by tp.infra_parameters,dp.name) as x on t.infra_parameters=x.infra_parameters " +
                "AND t.modified_timestamp=x.time;";
        try {
            return (List<TestPlan>) entityManager.createNativeQuery(sql, TestPlan.class)
                    .setParameter(1, product.getId())
                    .getResultList();
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error occured while " +
                    "executing SQL query [ ", sql, " ]"), e);
        }
    }

    /**
     * This method finds the last failed build for a given infrastructure combination of a TestPlan.
     *
     * @param testPlan the TestPlan containing the infrastructure combination
     * @return a {@link TestPlan} representing the last failed build
     * @throws TestGridDAOException when an error occurs while executing the native query
     */
    public TestPlan getLastFailure(TestPlan testPlan) throws TestGridDAOException {
        String sql = "select * from test_plan where infra_parameters= ?  AND DEPLOYMENTPATTERN_id=? " +
                " AND status='FAIL' order by modified_timestamp desc limit 1";
        try {
            return (TestPlan) entityManager.createNativeQuery(sql, TestPlan.class)
                    .setParameter(1, testPlan.getInfraParameters())
                    .setParameter(2, testPlan.getDeploymentPattern().getId())
                    .getSingleResult();
        } catch (Exception e) {
            throw new TestGridDAOException(StringUtil.concatStrings("Error occured while " +
                    "executing SQL query [ ", sql, " ]"), e);
        }
    }
}
