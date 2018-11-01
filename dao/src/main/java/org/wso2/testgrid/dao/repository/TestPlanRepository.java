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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.dto.TestCaseFailureResultDTO;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

/**
 * Repository class for {@link TestPlan} table.
 *
 * @since 1.0.0
 */
public class TestPlanRepository extends AbstractRepository<TestPlan> {
    private static final Logger logger = LoggerFactory.getLogger(TestPlanRepository.class);

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
            @SuppressWarnings("unchecked")
            List<TestPlan> resultList = (List<TestPlan>) query.getResultList();
            return EntityManagerHelper.refreshResultList(entityManager, resultList);
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
     */
    public TestPlan getLastFailure(Product product) {
        String sql = "SELECT  t.* from test_plan t INNER JOIN (SELECT tp.infra_parameters," +
                "max(tp.modified_timestamp) AS time, dp.name FROM test_plan tp INNER JOIN " +
                "deployment_pattern dp ON tp.DEPLOYMENTPATTERN_id=dp.id AND tp.status='FAIL' " +
                "AND  tp.DEPLOYMENTPATTERN_id IN (SELECT id FROM deployment_pattern WHERE " +
                "PRODUCT_id = ?) GROUP BY tp.infra_parameters,dp.name) as x ON " +
                "t.infra_parameters=x.infra_parameters AND t.modified_timestamp=x.time ORDER BY time DESC LIMIT 1";

        List resultList = entityManager.createNativeQuery(sql, TestPlan.class)
                .setParameter(1, product.getId())
                .getResultList();
        if (!resultList.isEmpty()) {
            return (TestPlan) EntityManagerHelper.refreshResult(entityManager, resultList.get(0));
        } else {
            return null;
        }
    }

    /**
     * This method returns the last build TestPlan for a given product.
     *
     * @param product the product being queried
     * @return instance of {@link TestPlan} for the last build
     */
    public TestPlan getLastBuild(Product product) {
        String sql = "select  t.* from test_plan t inner join (select tp.infra_parameters," +
                "max(tp.modified_timestamp) AS time, dp.name from test_plan tp inner join " +
                "deployment_pattern dp on tp.DEPLOYMENTPATTERN_id=dp.id and  tp.DEPLOYMENTPATTERN_id " +
                "in (select id from deployment_pattern where PRODUCT_id=?)" +
                "group by tp.infra_parameters,dp.name) AS x on t.infra_parameters=x.infra_parameters " +
                "AND t.modified_timestamp=x.time order by time desc limit 1";

        List resultList = entityManager.createNativeQuery(sql, TestPlan.class)
                .setParameter(1, product.getId())
                .getResultList();
        if (!resultList.isEmpty()) {
            return (TestPlan) EntityManagerHelper.refreshResult(entityManager, resultList.get(0));
        } else {
            return null;
        }
    }

    /**
     * This method retrives a list of TestPlans for a given product that represent the latest builds for
     * distinct infra combinations.
     *
     * @param product the product being queried
     * @return a list of {@link TestPlan}
     */
    public List<TestPlan> getLatestTestPlans(Product product) {
        String deploymentIdsRetrievingQuery = "select id from deployment_pattern where PRODUCT_id= ?;";
        @SuppressWarnings("unchecked")
        List<String> deploymentIds = (List<String>) entityManager
                .createNativeQuery(deploymentIdsRetrievingQuery).setParameter(1, product.getId()).getResultList();

        StringBuilder sql = new StringBuilder(
                "select tp.* from test_plan tp inner join (Select distinct infra_parameters, max(test_run_number) "
                        + "as test_run_number from test_plan where  DEPLOYMENTPATTERN_id in (");
        if (deploymentIds.isEmpty()) {
            return Collections.emptyList();
        } else {
            int deploymentIdLen = deploymentIds.size();
            for (int i = 0; i < deploymentIdLen - 1; i++) {
                sql.append("?, ");
            }
            sql.append("?) group by infra_parameters, DEPLOYMENTPATTERN_id) as latest_test_run_nums on "
                    + "tp.infra_parameters=latest_test_run_nums.infra_parameters and "
                    + "tp.test_run_number=latest_test_run_nums.test_run_number and tp.DEPLOYMENTPATTERN_id in (");
            for (int i = 0; i < deploymentIdLen - 1; i++) {
                sql.append("?, ");
            }
            sql.append("?);");
            Query query = entityManager.createNativeQuery(sql.toString(), TestPlan.class);
            int index = 1;
            for (int i = 0; i < 2; i++) {
                for (String s : deploymentIds) {
                    query.setParameter(index++, s);
                }
            }
            @SuppressWarnings("unchecked")
            List<TestPlan> resultList = (List<TestPlan>) query.getResultList();
            return EntityManagerHelper.refreshResultList(entityManager, resultList);
        }
    }

    /**
     * This method finds the last failed build for a given infrastructure combination of a TestPlan.
     *
     * @param testPlan the TestPlan containing the infrastructure combination
     * @return a {@link TestPlan} representing the last failed build
     */
    public TestPlan getLastFailure(TestPlan testPlan) {
        String sql = "select * from test_plan where infra_parameters= ?  AND DEPLOYMENTPATTERN_id=? " +
                " AND status='FAIL' order by modified_timestamp desc limit 1";

        List resultList = entityManager.createNativeQuery(sql, TestPlan.class)
                .setParameter(1, testPlan.getInfraParameters())
                .setParameter(2, testPlan.getDeploymentPattern().getId())
                .getResultList();

        if (!resultList.isEmpty()) {
            return (TestPlan) EntityManagerHelper.refreshResult(entityManager, resultList.get(0));
        } else {
            return null;
        }
    }

    /**
     * This method returns all the test plans that belongs to same infrastructure set,same deployment pattern
     * and same product.
     *
     * @param testPlan testPlan being queried
     * @return a List of {@link TestPlan} representing the history of that test plan
     */
    public List<TestPlan> getTestPlanHistory(TestPlan testPlan) {
        String sql = " select t.* from test_plan t inner join deployment_pattern dp inner " +
                "join product p on p.id=dp.PRODUCT_id and dp.id=t.DEPLOYMENTPATTERN_id " +
                "where t.infra_parameters=? AND dp.id=? AND p.id=? ORDER BY modified_timestamp DESC";

        @SuppressWarnings("unchecked")
        List<TestPlan> resultList = (List<TestPlan>) entityManager.createNativeQuery(sql, TestPlan.class)
                .setParameter(1, testPlan.getInfraParameters())
                .setParameter(2, testPlan.getDeploymentPattern().getId())
                .setParameter(3, testPlan.getDeploymentPattern().getProduct().getId())
                .getResultList();
        return EntityManagerHelper.refreshResultList(entityManager, resultList);
    }

    /**
     * This method returns all the test plans that are older than a specified duration.
     *
     * @param duration duration in hours/minutes or any other time unit
     * @param timeUnit unit of time for duration
     * @return a List of testplans that are older than the specified time
     */
    public List<TestPlan> getTestPlanOlderThan(String duration, String timeUnit) {
        String sql = StringUtil.concatStrings(
                "select t.* from test_plan t where t.created_timestamp < NOW() - INTERVAL ",
                duration , " ", timeUnit, " ", " and t.status = 'PENDING' or t.status = 'RUNNING' ");
        @SuppressWarnings("unchecked")
        List<TestPlan> resultList = (List<TestPlan>) entityManager.createNativeQuery(sql, TestPlan.class)
                .getResultList();
        return EntityManagerHelper.refreshResultList(entityManager, resultList);
    }

    /**
     * This method returns test failure summary for given test plan ids. (i.e for a given build job).
     *
     * @param testPlanIds test plan ids
     * @return a List of {@link TestCaseFailureResultDTO} representing the test case failure results for a given build
     */
    public List<TestCaseFailureResultDTO> getTestFailureSummaryByTPId(List<String> testPlanIds)
            throws TestGridDAOException {
        StringBuilder sql = new StringBuilder("select failed_tc.test_name as name, failed_tc.failure_message as "
                + "failureMessage, tp.infra_parameters as infraParametrs from test_plan tp join (select tc"
                + ".test_name, tc.failure_message, ts.TESTPLAN_id  from test_case tc inner join test_scenario ts on "
                + "ts.id=tc.TESTSCENARIO_id and tc.status = 'FAIL' and ts.TESTPLAN_id in (");
        for (int i = 0; i < testPlanIds.size() - 1; i++) {
            sql.append("?, ");
        }
        sql.append("?)) failed_tc on tp.id = failed_tc.TESTPLAN_id;");
        Query query = entityManager.createNativeQuery(sql.toString());
        int index = 1;
        for (String s : testPlanIds) {
            query.setParameter(index++, s);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> records = query.getResultList();
        return mapObject(TestCaseFailureResultDTO.class, records);
    }

    /**
     * This method returns the testplans need to be deleted
     *
     * @param count number of builds that need be saved for each infra combination in each job
     * @return a List of {@link String} testpan ids
     */
    public List<String> getTestPlansToCleanup(int count) {


        List<String> toDelete = new ArrayList<String>();

        String sql = "select distinct infra_parameters from test_plan";
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<String> infraCombinations = (List<String>) query.getResultList();

        sql = "select distinct name from product";
        query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<String> products = (List<String>) query.getResultList();

        String [] createTemp = {"drop table if exists temp ;", "drop table if exists temp2 ;",
                "create temporary table temp (id VARCHAR(255), modified_timestamp TIMESTAMP, name VARCHAR(50), " +
                "infra_parameters  VARCHAR(255)); ",
                "INSERT INTO  temp (id,name,modified_timestamp,infra_parameters ) " +
                        "select test_plan.id, product.name, test_plan.modified_timestamp, test_plan.infra_parameters " +
                        "from test_plan,product,deployment_pattern " +
                        "where test_plan.DEPLOYMENTPATTERN_id = deployment_pattern.id and " +
                        "deployment_pattern.PRODUCT_id = product.id;",
                "create temporary table temp2 (id VARCHAR(255), modified_timestamp TIMESTAMP, name  VARCHAR(50), " +
                        "infra_parameters  VARCHAR(255));",
                "insert  temp2 SELECT * FROM temp;"};

        EntityTransaction txn = entityManager.getTransaction();
        txn.begin();
        for (String sqlQ : createTemp) {
            entityManager.createNativeQuery(sqlQ).executeUpdate();
        }
        txn.commit();

        @SuppressWarnings("unchecked")
        List<String> quaryResultOfInfra;
        for (String product : products) {
            for (String infra : infraCombinations) {
                logger.info(StringUtil.concatStrings("identifying testplans that need to be deleted in" +
                        " product ", product, "infra combination : ", infra));
                sql = "select id from temp2 as tp left join (select id from temp where name = ? and " +
                        "infra_parameters = ? order by (modified_timestamp) DESC limit ?)p2 USING(id) " +
                        "WHERE p2.id IS NULL and infra_parameters = ?  and name = ? order by (modified_timestamp) DESC";

                query = entityManager.createNativeQuery(sql);
                query.setParameter(1, product);
                query.setParameter(2, infra);
                query.setParameter(3, count);
                query.setParameter(4, infra);
                query.setParameter(5, product);
                quaryResultOfInfra = (List<String>) query.getResultList();
                for (String data : quaryResultOfInfra) {
                    logger.info(data);
                    toDelete.add(data);
                }
            }
        }
        return toDelete;
    }

    /**
     * Delete a list of test_plans from db
     * @param testPlans list of test plan ids that need to be deleted
     */
    public void deleteTestPlans(List<String> testPlans) {
        String sql;
        EntityTransaction txn = null;
        try {
            txn = entityManager.getTransaction();
            txn.begin();

            for (String testplan : testPlans) {
                sql = "delete from test_plan where id = '" + testplan + "'";
                entityManager.createNativeQuery(sql).executeUpdate();
            }
            txn.commit();
        } catch (Exception e) {
            if (txn != null) {
                logger.error("Transaction is being rolled back due to error : \n", e);
                txn.rollback();
            }
        }
    }

    /**
     * This method returns the test execution summary for given test plan ids(i.e for a given build job).
     *
     * @param testPlanIds test plan ids of a specific build job
     * @return a List of {@link String} representing statuses of given test plans
     */
    public List<String> getTestPlanStatuses(List<String> testPlanIds) {
        StringBuilder sql = new StringBuilder("select status from test_plan where id in (");
        for (int i = 0; i < testPlanIds.size() - 1; i++) {
            sql.append("?, ");
        }
        sql.append("?);");
        Query query = entityManager.createNativeQuery(sql.toString());
        int index = 1;
        for (String s : testPlanIds) {
            query.setParameter(index++, s);
        }
        @SuppressWarnings("unchecked")
        List<String> statuses = (List<String>) query.getResultList();
        return statuses;
    }

    /**
     * This method returns the history of test execution summary for given product.
     *
     * @param productId id of the product
     * @param from starting point of the considering time range
     * @param to end point of the considering time range
     * @return a List of {@link TestPlan} representing executed test plans of a given product for a given time range
     */
    public List<TestPlan> getTestExecutionHistory(String productId, String from, String to) {
        String sql = "select tp.* from test_plan tp inner join (Select distinct infra_parameters from test_plan where  "
                + "DEPLOYMENTPATTERN_id in (select id from deployment_pattern where PRODUCT_id=?)) as rn on "
                + "tp.infra_parameters=rn.infra_parameters and tp.DEPLOYMENTPATTERN_id "
                + "in (select id from deployment_pattern where PRODUCT_id=?) and modified_timestamp between ? and ?;";

        @SuppressWarnings("unchecked")
        List<TestPlan> resultList = (List<TestPlan>) entityManager.createNativeQuery(sql, TestPlan.class)
                .setParameter(1, productId)
                .setParameter(2, productId)
                .setParameter(3, from)
                .setParameter(4, to)
                .getResultList();
        return EntityManagerHelper.refreshResultList(entityManager, resultList);
        }

    /**
     * This method is responsible to map list of objects to a given class.
     *
     * @param type    Mapping class
     * @param records lst of objects that are mapping to instance of the given class
     * @return a List of mapped objects
     */
    public static <T> List<T> mapObject(Class<T> type, List<Object[]> records) throws TestGridDAOException {
        List<T> result = new LinkedList<>();
        for (Object[] record : records) {
            List<Class<?>> tupleTypes = new ArrayList<>();
            for (Object field : record) {
                //if a filed contains null value assign empty string. If null values in the either infra_combination
                // column or test case name column or test case description column, null value could be passed to here.
                if (field == null) {
                    field = "";
                }
                tupleTypes.add(field.getClass());
            }
            Constructor<T> ctor;
            try {
                ctor = type.getConstructor(tupleTypes.toArray(new Class<?>[record.length]));
                result.add(ctor.newInstance(record));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                    InstantiationException e) {
                throw new TestGridDAOException("Error occured while mapping object to TestCaseFailureResultDTO object",
                        e);
            }

        }
        return result;
    }
}
