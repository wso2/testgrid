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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.dao.uow;

import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.dto.TestCaseFailureResultDTO;
import org.wso2.testgrid.dao.repository.TestPlanRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

/**
 * This class defines the Unit of work related to a {@link TestPlan}.
 *
 * @since 1.0.0
 */
public class TestPlanUOW {

    private final TestPlanRepository testPlanRepository;

    /**
     * Constructs an instance of {@link TestPlanUOW} to manager use cases related to test plans.
     */
    public TestPlanUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        testPlanRepository = new TestPlanRepository(entityManager);
    }

    /**
     * This method persists a single {@link TestPlan} object to the database.
     *
     * @param testPlan Populated TestPlan object
     * @return The persisted TestPlan object with additional details added
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public TestPlan persistTestPlan(TestPlan testPlan) throws TestGridDAOException {
        TestPlan persisted = testPlanRepository.persist(testPlan);
        if (persisted != null) {
            persisted.setDeployerType(testPlan.getDeployerType());
            persisted.setScenarioTestsRepository(testPlan.getScenarioTestsRepository());
            persisted.setInfrastructureRepository(testPlan.getInfrastructureRepository());
            persisted.setDeploymentRepository(testPlan.getDeploymentRepository());
            persisted.setConfigChangeSetRepository(testPlan.getConfigChangeSetRepository());
            persisted.setConfigChangeSetBranchName(testPlan.getConfigChangeSetBranchName());
            persisted.setInfrastructureConfig(testPlan.getInfrastructureConfig());
            persisted.setDeploymentConfig(testPlan.getDeploymentConfig());
            persisted.setScenarioConfig(testPlan.getScenarioConfig());
        }
        return persisted;
    }

    /**
     * Returns the {@link TestPlan} instance for the given id.
     *
     * @param id primary key of the test plan
     * @return matching {@link TestPlan} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public Optional<TestPlan> getTestPlanById(String id) throws TestGridDAOException {
        TestPlan testPlan = testPlanRepository.findByPrimaryKey(id);
        if (testPlan == null) {
            return Optional.empty();
        }
        return Optional.of(testPlan);
    }

    /**
     * Returns a list of {@link TestPlan} instances for the given deployment-pattern-id and date.
     *
     * @param deploymentId id of the associated deployment-pattern
     * @param date         created before date
     * @return matching {@link TestPlan} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public List<TestPlan> getTestPlansByDeploymentIdAndDate(String deploymentId, Timestamp date) throws
            TestGridDAOException {
        return testPlanRepository.findByDeploymentPatternAndDate(deploymentId, date);
    }

    /**
     * Returns a {@link TestPlan} object representing the last failed build for a product.
     *
     * @param product the product being queried
     * @return a TestPlan object for last failed build
     */
    public TestPlan getLastFailure(Product product) {
        return testPlanRepository.getLastFailure(product);
    }

    /**
     * Returns a {@link TestPlan} object representing the last build for a given product.
     *
     * @param product the product being queried
     * @return a TestPlan object for last build
     */
    public TestPlan getLastBuild(Product product) {
        return testPlanRepository.getLastBuild(product);
    }

    /**
     * Returns a final Status for a product after considering all distinct infrastructure combination
     * statuses.
     *
     * @param product the product being queried
     * @return a {@link Status} for the product
     */
    public Status getCurrentStatus(Product product) {
        List<TestPlan> testPlans = testPlanRepository.getLatestTestPlans(product);
        List<TestPlan> succesfulPlans = testPlans.stream().filter(testPlan ->
                testPlan.getStatus().equals(Status.SUCCESS)
        ).collect(Collectors.toList());
        boolean running = testPlans.stream().anyMatch(testPlan -> Status.RUNNING.equals(testPlan.getStatus()));
        //check if there are pending test_plans
        if (running) {
            return Status.RUNNING;
        } else {
            return succesfulPlans.size() == testPlans.size() ? Status.SUCCESS : Status.FAIL;
        }
    }

    /**
     * Returns a list of latest TestPlans for a given product.
     *
     * @param product the product being queried
     * @return a list of {@link TestPlan}s for latest builds
     */
    public List<TestPlan> getLatestTestPlans(Product product) {
        return testPlanRepository.getLatestTestPlans(product);
    }

    /**
     * Returns the latest failed TestPlan for a given infrastructure type.
     *
     * @param testPlan TestPlan representing a distinct infrastructure combination
     * @return a {@link TestPlan} for the latest failed build
     */
    public TestPlan getLastFailure(TestPlan testPlan) {
        return testPlanRepository.getLastFailure(testPlan);
    }

    /**
     * Returns the test plans history for a give type of infrastructure combination and deployment
     * pattern.
     *
     * @param testPlan a {@link TestPlan} object containing the infra combination and deployment
     *                 pattern
     * @return a List of TestPlans corresponding to the query
     */
    public List<TestPlan> getTestPlanHistory(TestPlan testPlan) {
        return testPlanRepository.getTestPlanHistory(testPlan);
    }

    /**
     * Returns the test plans older than a specified period of time.
     * This will be used to resolve the statuses of testplans with erroneous statuses.
     *
     * @return a List of TestPlans corresponding to the query
     */
    public List<TestPlan> getTestPlansOlderThan(String duration, String timeUnit) {
        return testPlanRepository.getTestPlanOlderThan(duration, timeUnit);
    }

    /**
     * Returns the test plan statuses for given test plan ids.
     *
     * @return a List of Test Plan statuses.
     */
    public List<String> getTestExecutionSummary(List<String> tpIds) {
        return testPlanRepository.getTestExecutionSummaryByTPId(tpIds);
    }

    /**
     * Returns the representation of failed test cases with test case name, description and infra combination for given
     * test plan ids. (I.e for a given build job)
     *
     * @return a List of TestCaseFailureResultDTO which represent test cases failure for given test plan ids.
     */
    public List<TestCaseFailureResultDTO> getTestFailureSummary(List<String> tpIds) throws TestGridDAOException {
        return testPlanRepository.getTestFailureSummaryByTPId(tpIds);
    }

    /**
     * Returns the representation of test execution history for q given product in a given time range
     *
     * @return a List of TestPlan which represent executed test plans in the given time range for a given product.
     */
    public List<TestPlan> getTestExecutionHistory(String productId, String from, String to) {
        return testPlanRepository.getTestExecutionHistory(productId, from, to);
    }
}
