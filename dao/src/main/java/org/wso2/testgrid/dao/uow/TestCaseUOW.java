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
package org.wso2.testgrid.dao.uow;

import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.TestCaseRepository;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;

/**
 * This class defines the Unit of work related to a {@link TestCase}.
 *
 * @since 1.0.0
 */
public class TestCaseUOW {

    private final TestCaseRepository testCaseRepository;

    /**
     * Constructs an instance of {@link TestCaseUOW} to manager use cases related to test cases.
     */
    public TestCaseUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        testCaseRepository = new TestCaseRepository(entityManager);
    }

    /**
     * Persists the {@link TestCase} instance for the given params.
     *
     * @param testCase        test case to persist
     * @throws TestGridDAOException thrown when error on persisting
     */
    public TestCase persistTestCase(TestCase testCase) throws TestGridDAOException {
        return testCaseRepository.persist(testCase);
    }

    /**
     * Returns the {@link TestCase} instance for the given id.
     *
     * @param id primary key of the test case
     * @return matching {@link TestCase} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public Optional<TestCase> getTestCaseById(String id) throws TestGridDAOException {
        TestCase testCase = testCaseRepository.findByPrimaryKey(id);
        if (testCase == null) {
            return Optional.empty();
        }
        return Optional.of(testCase);
    }

    /**
     * Checks if there are any failed test cases pertaining to a scenario.
     *
     * @param testScenario test scenario
     * @return boolean - true if there exists failed tests and false otherwise
     * @throws TestGridDAOException thrown when error processing native query
     */
    @SuppressWarnings("unchecked")
    public boolean isExistsFailedTests(TestScenario testScenario) throws TestGridDAOException {
        List<Object> resultObject = testCaseRepository.executeTypedQuery("SELECT * FROM test_case "
                + "WHERE TESTSCENARIO_id = '" + testScenario.getId() + "' AND is_success = FALSE;");

        return resultObject.isEmpty();
    }
}
