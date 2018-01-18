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
            persisted.setDeployment(testPlan.getDeployment());
            persisted.setTestRepoDir(testPlan.getTestRepoDir());
            persisted.setInfraRepoDir(testPlan.getInfraRepoDir());
            persisted.setInfrastructureScript(testPlan.getInfrastructureScript());
            persisted.setDeploymentScript(testPlan.getDeploymentScript());
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
     * @param date created before date
     * @return matching {@link TestPlan} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public List<TestPlan> getTestPlansByDeploymentIdAndDate(String deploymentId, Timestamp date) throws
            TestGridDAOException {
        return testPlanRepository.findByDeploymentPatternAndDate(deploymentId, date);
    }

    /**
     * Returns a {@link TestPlan} object representing the last failed build for a product.
     * @param product the product being queried
     * @return a TestPlan object for last failed build
     * @throws TestGridDAOException when there is an error querying the data
     */
    public TestPlan getLastFailure(Product product) throws TestGridDAOException {
        return testPlanRepository.getLastFailure(product);
    }

    /**
     * Returns a {@link TestPlan} object representing the last build for a given product.
     * @param product the product being queried
     * @return a TestPlan object for last build
     * @throws TestGridDAOException when there is an erorr querying the data
     */
    public TestPlan getLastBuild(Product product) throws TestGridDAOException {
        return testPlanRepository.getLastBuild(product);
    }

    /**
     * Returns a final Status for a product after considering all distinct infrastructure combination
     * statuses.
     * @param product the product being queried
     * @return a {@link Status} for the product
     * @throws TestGridDAOException when there is an error querying the data
     */
    public Status getCurrentStatus(Product product) throws TestGridDAOException {
        List<TestPlan> testPlans = testPlanRepository.getLatestTestPlans(product);
        List<TestPlan> succesfulPlans = testPlans.stream().filter(testPlan ->
                testPlan.getStatus().equals(Status.SUCCESS)
        ).collect(Collectors.toList());
        return succesfulPlans.size() == testPlans.size() ? Status.SUCCESS : Status.FAIL;
    }

    /**
     * Returns a list of latest TestPlans for a given product.
     *
     * @param product the product being queried
     * @return a list of {@link TestPlan}s for latest builds
     * @throws TestGridDAOException when there is an error querying the data
     */
    public List<TestPlan> getLatestTestPlans(Product product) throws TestGridDAOException {
        return testPlanRepository.getLatestTestPlans(product);
    }

    /**
     * Returns the latest failed TestPlan for a given infrastructure type.
     *
     * @param testPlan TestPlan representing a distinct infrastructure combination
     * @return a {@link TestPlan} for the latest failed build
     * @throws TestGridDAOException when there is an error querying the data
     */
    public TestPlan getLastFailure(TestPlan testPlan) throws TestGridDAOException {
        return testPlanRepository.getLastFailure(testPlan);
    }
}
