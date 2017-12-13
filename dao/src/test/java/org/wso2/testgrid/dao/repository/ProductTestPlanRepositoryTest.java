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

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Class to test the functionality of {@link ProductTestPlanRepository}.
 *
 * @since 1.0.0
 */
public class ProductTestPlanRepositoryTest {

    private ProductTestPlanRepository productTestPlanRepository;

    @BeforeTest
    public void setUp() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager("testgrid_h2");
        productTestPlanRepository = new ProductTestPlanRepository(entityManager);
    }

    @Test(description = "Test persist data in the repository.")
    public void persistTest() throws TestGridDAOException {
        // Product test plan
        ProductTestPlan productTestPlan = new ProductTestPlan();
        productTestPlan.setProductName("WSO2 IS");
        productTestPlan.setProductVersion("5.4.0");
        productTestPlan.setChannel(ProductTestPlan.Channel.LTS);
        productTestPlan.setStatus(ProductTestPlan.Status.PRODUCT_TEST_PLAN_PENDING);

        // Test plan
        TestPlan testPlan = new TestPlan();
        testPlan.setName("TestPlan1");
        testPlan.setStatus(TestPlan.Status.TESTPLAN_PENDING);
        testPlan.setDeploymentPattern("deployment pattern 1");
        testPlan.setDescription("Description 1");
        testPlan.setProductTestPlan(productTestPlan);

        // Product test plan -> Test plan mapping
        productTestPlan.setTestPlans(Collections.singletonList(testPlan));

        // Persist
        productTestPlan = productTestPlanRepository.persist(productTestPlan);

        // Assertion
        ProductTestPlan foundedProductTestPlan = productTestPlanRepository.findByPrimaryKey(productTestPlan.getId());

        // Assert product test plan
        Assert.assertNotNull(foundedProductTestPlan);
        Assert.assertEquals(foundedProductTestPlan.getId(), productTestPlan.getId());
        Assert.assertEquals(foundedProductTestPlan.getProductName(), productTestPlan.getProductName());
        Assert.assertEquals(foundedProductTestPlan.getProductVersion(), productTestPlan.getProductVersion());
        Assert.assertEquals(foundedProductTestPlan.getChannel(), productTestPlan.getChannel());
        Assert.assertEquals(foundedProductTestPlan.getStatus(), productTestPlan.getStatus());
        Assert.assertEquals(foundedProductTestPlan.getStartTimestamp(), productTestPlan.getStartTimestamp());
        Assert.assertEquals(foundedProductTestPlan.getModifiedTimestamp(), productTestPlan.getModifiedTimestamp());

        // Assert test plan
        List<TestPlan> foundedTestPlanList = foundedProductTestPlan.getTestPlans();
        Assert.assertEquals(foundedTestPlanList.size(), 1);

        TestPlan foundedTestPlan = foundedTestPlanList.get(0);

        Assert.assertEquals(testPlan.getName(), foundedTestPlan.getName());
        Assert.assertEquals(testPlan.getStatus(), foundedTestPlan.getStatus());
        Assert.assertEquals(testPlan.getDeploymentPattern(), foundedTestPlan.getDeploymentPattern());
        Assert.assertEquals(testPlan.getDescription(), foundedTestPlan.getDescription());
        Assert.assertEquals(testPlan.getStartTimestamp(), foundedTestPlan.getStartTimestamp());
        Assert.assertEquals(testPlan.getModifiedTimestamp(), foundedTestPlan.getModifiedTimestamp());
        Assert.assertEquals(productTestPlan.getId(), foundedTestPlan.getProductTestPlan().getId());
    }

    @Test(description = "Test find all records from the repository.",
          dependsOnMethods = "persistTest")
    public void findAllTest() throws TestGridDAOException {
        List<ProductTestPlan> productTestPlans = productTestPlanRepository.findAll();
        Assert.assertEquals(productTestPlans.size(), 1);

        List<TestPlan> testPlans = productTestPlans.get(0).getTestPlans();
        Assert.assertEquals(testPlans.size(), 1);
    }
}
