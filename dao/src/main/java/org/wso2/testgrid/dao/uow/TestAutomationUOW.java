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

import javax.persistence.EntityManagerFactory;

/**
 * Unit of work class to handle to data base transactions related to test cases.
 *
 * @since 1.0.0
 */
public class TestAutomationUOW {

    private final EntityManagerFactory entityManagerFactory;

    /**
     * Initialises the entity manager factory when constructing an instance of this type.
     */
    public TestAutomationUOW() {
        entityManagerFactory = DAOUtil.getEntityManagerFactory();
    }

    /**
     * Persists a pending test case for the given params.
     *
     * @param testName     name of the test case
     * @param testScenario {@link TestScenario} instance associated with test case
     * @throws TestGridDAOException thrown when error on persisting
     */
    public void persistPendingTestCase(String testName, TestScenario testScenario)
            throws TestGridDAOException {
        TestCaseRepository testCaseRepository = new TestCaseRepository(entityManagerFactory);

        TestCase testCase = new TestCase();
        testCase.setName(testName);
        testCase.setStatus(TestCase.Status.TESTCASE_PENDING);
        testCase.setTestScenario(testScenario);
        testCaseRepository.persist(testCase);
    }
}
