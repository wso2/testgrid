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
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Class to test the functionality of {@link ProductRepository}.
 *
 * @since 1.0.0
 */
public class ProductTestPlanRepositoryTest {

    private ProductRepository productTestPlanRepository;

    @BeforeTest
    public void setUp() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager("testgrid_h2");
        productTestPlanRepository = new ProductRepository(entityManager);
    }

    @Test(description = "Test persist data in the repository.")
    public void persistTest() throws TestGridDAOException {
        // Product test plan
        Product product = new Product();
        product.setProductName("WSO2 IS");
        product.setProductVersion("5.4.0");
        product.setChannel(Product.Channel.LTS);

        DeploymentPattern deploymentPattern = new DeploymentPattern();
        deploymentPattern.setName("single-node");
        deploymentPattern.setTestSuccess(true);
        deploymentPattern.setProduct(product);
        /*// Test plan
        TestPlan testPlan = new TestPlan();
        testPlan.setName("TestPlan1");
        testPlan.setStatus(TestPlan.Status.TESTPLAN_PENDING);
        testPlan.setDeploymentPattern("deployment pattern 1");
        testPlan.setDescription("Description 1");
        testPlan.setDeploymentPattern(product);*/

        // Product test plan -> Test plan mapping
        product.setDeploymentPatterns(Collections.singletonList(deploymentPattern));

        // Persist
        product = productTestPlanRepository.persist(product);

        // Assertion
        Product foundedProductTestPlan = productTestPlanRepository.findByPrimaryKey(product.getId());

        // Assert product test plan
        Assert.assertNotNull(foundedProductTestPlan);
        Assert.assertEquals(foundedProductTestPlan.getId(), product.getId());
        Assert.assertEquals(foundedProductTestPlan.getProductName(), product.getProductName());
        Assert.assertEquals(foundedProductTestPlan.getProductVersion(), product.getProductVersion());
        Assert.assertEquals(foundedProductTestPlan.getChannel(), product.getChannel());

        // Assert test plan
        List<DeploymentPattern> foundedDeploymentPatterns = foundedProductTestPlan.getDeploymentPatterns();
        Assert.assertEquals(foundedDeploymentPatterns.size(), 1);

        DeploymentPattern foundedTestPlan = foundedDeploymentPatterns.get(0);

        Assert.assertEquals(deploymentPattern.getName(), foundedTestPlan.getName());
        Assert.assertEquals(deploymentPattern.getProduct(), foundedTestPlan.getProduct());
        Assert.assertEquals(deploymentPattern.getTestSuccessStatus(), foundedTestPlan.getTestSuccessStatus());
        Assert.assertEquals(product.getId(), foundedTestPlan.getProduct().getId());
    }

    @Test(description = "Test find all records from the repository.",
          dependsOnMethods = "persistTest")
    public void findAllTest() throws TestGridDAOException {
        List<Product> productTestPlans = productTestPlanRepository.findAll();
        Assert.assertEquals(productTestPlans.size(), 1);

        List<DeploymentPattern> deploymentPatterns = productTestPlans.get(0).getDeploymentPatterns();
        Assert.assertEquals(deploymentPatterns.size(), 1);
    }
}
