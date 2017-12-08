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
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.TestCaseRepository;
import org.wso2.testgrid.dao.util.DAOUtil;

import java.io.Closeable;
import javax.persistence.EntityManagerFactory;

/**
 * This class defines the Unit of work related to a {@link TestCase}.
 *
 * @since 1.0.0
 */
public class TestCaseUOW implements Closeable {

    private final TestCaseRepository testCaseRepository;

    /**
     * Constructs an instance of {@link TestCaseUOW} to manager use cases related to test cases.
     */
    public TestCaseUOW() {
        EntityManagerFactory entityManagerFactory = DAOUtil.getEntityManagerFactory();
        testCaseRepository = new TestCaseRepository(entityManagerFactory);
    }

    /**
     * Persists the {@link TestCase} instance for the given params.
     *
     * @param testName        name of the test case
     * @param testScenario    {@link TestScenario} instance associated with test case
     * @param isSuccess       status of the TestCase
     * @param responseMessage response message of the test case
     * @throws TestGridDAOException thrown when error on persisting
     */
    public TestCase persistTestCase(String testName, TestScenario testScenario, boolean isSuccess,
                                    String responseMessage) throws TestGridDAOException {
        TestCase.Status status = isSuccess ? TestCase.Status.TESTCASE_COMPLETED : TestCase.Status.TESTCASE_ERROR;

        // Create test case instance
        TestCase testCase = new TestCase();
        testCase.setName(testName);
        testCase.setStatus(status);
        testCase.setTestScenario(testScenario);
        testCase.setFailureMessage(responseMessage);

        // Persist test case
        return testCaseRepository.persist(testCase);
    }

    /**
     * Returns the {@link TestCase} instance for the given id.
     *
     * @param id primary key of the test case
     * @return matching {@link TestCase} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public TestCase getTestCaseById(String id) throws TestGridDAOException {
        return testCaseRepository.findByPrimaryKey(id);
    }

    @Override
    public void close() {
        testCaseRepository.close();
    }
}
