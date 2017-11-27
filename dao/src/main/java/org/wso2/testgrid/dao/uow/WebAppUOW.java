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

import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.ProductTestPlanRepository;
import org.wso2.testgrid.dao.repository.TestCaseRepository;
import org.wso2.testgrid.dao.repository.TestPlanRepository;
import org.wso2.testgrid.dao.repository.TestScenarioRepository;
import org.wso2.testgrid.dao.util.DAOUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Unit of work class to handle to data base transactions related to web app.
 *
 * @since 1.0.0
 */
public class WebAppUOW {

    private final EntityManagerFactory entityManagerFactory;

    /**
     * Initialises the entity manager factory when constructing an instance of this type.
     */
    public WebAppUOW() {
        entityManagerFactory = DAOUtil.getEntityManagerFactory();
    }

    /**
     * Returns all the product test plans.
     *
     * @return list of {@link ProductTestPlan} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public List<ProductTestPlan> getAllProductTestPlans() throws TestGridDAOException {
        ProductTestPlanRepository productTestPlanRepository = new ProductTestPlanRepository(entityManagerFactory);
        return productTestPlanRepository.findAll();
    }

    /**
     * Returns the {@link ProductTestPlan} instance for the given id.
     *
     * @param id primary key of the product test plan
     * @return matching {@link ProductTestPlan} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public ProductTestPlan getProductTestPlanById(String id) throws TestGridDAOException {
        ProductTestPlanRepository productTestPlanRepository = new ProductTestPlanRepository(entityManagerFactory);
        return productTestPlanRepository.findByPrimaryKey(id);
    }

    /**
     * Returns the {@link TestPlan} instance for the given id.
     *
     * @param id primary key of the test plan
     * @return matching {@link TestPlan} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public TestPlan getTestPlanById(String id) throws TestGridDAOException {
        TestPlanRepository testPlanRepository = new TestPlanRepository(entityManagerFactory);
        return testPlanRepository.findByPrimaryKey(id);
    }

    /**
     * Returns the {@link TestScenario} instance for the given id.
     *
     * @param id primary key of the test plan
     * @return matching {@link TestScenario} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public TestScenario getTestScenarioById(String id) throws TestGridDAOException {
        TestScenarioRepository testScenarioRepository = new TestScenarioRepository(entityManagerFactory);
        return testScenarioRepository.findByPrimaryKey(id);
    }

    /**
     * Returns the {@link TestCase} instance for the given id.
     *
     * @param id primary key of the test case
     * @return matching {@link TestCase} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public TestCase getTestCaseById(String id) throws TestGridDAOException {
        TestCaseRepository testCaseRepository = new TestCaseRepository(entityManagerFactory);
        return testCaseRepository.findByPrimaryKey(id);
    }

    /**
     * Returns a list of {@link TestCase} instances for the given Test Scenario ID
     *
     * @param id Test Scenario ID
     * @return a list of {@link TestCase} instances for the given Test Scenario ID
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public List<TestCase> getTestCasesForTestScenario(String id) throws TestGridDAOException {
        TestCaseRepository testCaseRepository = new TestCaseRepository(entityManagerFactory);
        Map<String, Object> params = Collections.singletonMap(TestCase.TEST_SCENARIO_COLUMN, id);
        return testCaseRepository.findByFields(params);
    }
}
