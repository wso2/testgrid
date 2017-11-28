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
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.ProductTestPlanRepository;
import org.wso2.testgrid.dao.repository.TestCaseRepository;
import org.wso2.testgrid.dao.repository.TestPlanRepository;
import org.wso2.testgrid.dao.repository.TestScenarioRepository;
import org.wso2.testgrid.dao.util.DAOUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Unit of work class to handle to data base transactions when generating reports.
 *
 * @since 1.0.0
 */
public class ReportGenerationUOW {

    private final EntityManagerFactory entityManagerFactory;

    /**
     * Initialises the entity manager factory when constructing an instance of this type.
     */
    public ReportGenerationUOW() {
        entityManagerFactory = DAOUtil.getEntityManagerFactory();
    }

    /**
     * Returns an instance of {@link ProductTestPlan} for the given product name and product version.
     *
     * @param productName    product name
     * @param productVersion product version
     * @return an instance of {@link ProductTestPlan} for the given product name and product version
     * @throws TestGridDAOException thrown when error on obtaining the product test plan
     */
    public ProductTestPlan getProductTestPlan(String productName, String productVersion) throws TestGridDAOException {
        ProductTestPlanRepository productTestPlanRepository = new ProductTestPlanRepository(entityManagerFactory);

        // JPQL  query to get the latest modifies product test plan for product name and version
        String sqlQuery = StringUtil.concatStrings("SELECT c FROM ", ProductTestPlan.class.getSimpleName(),
                " c WHERE c.productName=\"", productName, "\" AND ",
                "c.productVersion=\"", productVersion, "\" ORDER BY c.modifiedTimestamp");
        return productTestPlanRepository.executeTypedQuary(sqlQuery, ProductTestPlan.class, 1);
    }

    /**
     * Returns a list of test plans associated with the product test plan.
     *
     * @param productTestPlan product test plan to obtain test plans
     * @return a list of {@link TestPlan} instances associated with the product name and product version
     * @throws TestGridDAOException thrown when error on obtaining records for the given params
     */
    public List<TestPlan> getTestPlanListForProductTest(ProductTestPlan productTestPlan) throws TestGridDAOException {
        TestPlanRepository testPlanRepository = new TestPlanRepository(entityManagerFactory);

        // Get test plans to the product test plan
        Map<String, Object> params = new HashMap<>();
        params.put(TestPlan.PRODUCT_TEST_PLAN_COLUMN, productTestPlan);
        return testPlanRepository.findByFields(params);
    }

    /**
     * Returns a list of test scenarios associated with the test plan.
     *
     * @param testPlan test plan to obtain the test scenarios
     * @return a list of {@link TestScenario} instances associated with the test plan
     * @throws TestGridDAOException thrown when error on obtaining test scenarios from the given test plan
     */
    public List<TestScenario> getTestScenariosForTestPlan(TestPlan testPlan) throws TestGridDAOException {
        TestScenarioRepository testScenarioRepository = new TestScenarioRepository(entityManagerFactory);

        // Get test scenarios for the test plan
        Map<String, Object> params = Collections.singletonMap(TestScenario.TEST_PLAN_COLUMN, testPlan);
        return testScenarioRepository.findByFields(params);
    }

    /**
     * Returns a list of test cases associated with the test scenario.
     *
     * @param testScenario test scenario to retrieve the test cases
     * @return a list of {@link TestCase} instances associated with the test scenario
     * @throws TestGridDAOException thrown when error on retrieving test cases
     */
    public List<TestCase> getTestCasesForTestScenario(TestScenario testScenario) throws TestGridDAOException {
        TestCaseRepository testCaseRepository = new TestCaseRepository(entityManagerFactory);

        // Get test cases for the test scenario
        Map<String, Object> params = Collections.singletonMap(TestCase.TEST_SCENARIO_COLUMN, testScenario);
        return testCaseRepository.findByFields(params);
    }
}
