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

package org.wso2.testgrid.web.api;

import org.wso2.testgrid.web.bean.Product;
import org.wso2.testgrid.web.bean.TestCase;
import org.wso2.testgrid.web.bean.TestPlan;
import org.wso2.testgrid.web.bean.TestScenario;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class which holds utility methods required for TestGrid APIs.
 */
public class APIUtil {

    /**
     * Util method to convert Product DTO to a Product Bean object.
     *
     * @param productTestPlan Product DTO to be converted
     * @return Product bean object with necessary information
     */
    static Product getProductTestPlanBean(org.wso2.testgrid.common.ProductTestPlan productTestPlan) {
        Product productBean = new Product();
        productBean.setId(productTestPlan.getId());
        productBean.setName(productTestPlan.getProductName());
        productBean.setVersion(productTestPlan.getProductVersion());
        //productTestPlanBean.setChannel();
        return productBean;
    }

    /**
     * Util method to convert list of Product DTOs to a list of Product Bean objects.
     *
     * @param productTestPlans list of Product DTOs to be converted
     * @return List of Product bean objects with necessary information
     */
    static List<Product> getProductTestPlanBeans(List<org.wso2.testgrid.common.ProductTestPlan>
                                                                 productTestPlans) {
        List<Product> plans = new ArrayList<>();

        for (org.wso2.testgrid.common.ProductTestPlan productTestPlan : productTestPlans) {
            plans.add(getProductTestPlanBean(productTestPlan));
        }
        return plans;
    }

    /**
     * Util method to convert TestPlan DTO to a TestPlan Bean object.
     *
     * @param testPlan TestPlan DTO to be converted
     * @return TestPlan bean object with necessary information
     */
    static TestPlan getTestPlanBean(org.wso2.testgrid.common.TestPlan testPlan) {
        TestPlan testPlan1 = new TestPlan();
        testPlan1.setId(testPlan.getId());
        testPlan1.setDeploymentPattern(testPlan.getDeploymentPattern());
        testPlan1.setStatus(testPlan.getStatus().toString());
        return testPlan1;
    }

    /**
     * Util method to convert list of TestPlan DTOs to a list of TestPlan Bean objects.
     *
     * @param testPlans list of TestPlan DTOs to be converted
     * @return List of TestPlan bean objects with necessary information
     */
    static List<TestPlan> getTestPlanBeans(List<org.wso2.testgrid.common.TestPlan> testPlans) {
        List<TestPlan> plans = new ArrayList<>();

        for (org.wso2.testgrid.common.TestPlan testPlan : testPlans) {
            plans.add(getTestPlanBean(testPlan));
        }
        return plans;
    }

    /**
     * Util method to convert TestScenario DTO to a TestScenario Bean object.
     *
     * @param testScenario TestScenario DTO to be converted
     * @return TestScenario bean object with necessary information
     */
    static TestScenario getTestScenarioBean(org.wso2.testgrid.common.TestScenario testScenario) {
        TestScenario testScenario1 = new TestScenario();
        testScenario1.setId(testScenario.getId());
        testScenario1.setName(testScenario.getName());
        testScenario1.setStatus(testScenario.getStatus().toString());
        return testScenario1;
    }

    /**
     * Util method to convert list of TestScenario DTOs to a list of TestScenario Bean objects.
     *
     * @param testScenarios list of TestScenario DTOs to be converted
     * @return List of TestScenario bean objects with necessary information
     */
    static List<TestScenario> getTestScenarioBeans(List<org.wso2.testgrid.common.TestScenario> testScenarios) {
        List<TestScenario> testScenarios1 = new ArrayList<>();

        for (org.wso2.testgrid.common.TestScenario testScenario : testScenarios) {
            testScenarios1.add(getTestScenarioBean(testScenario));
        }
        return testScenarios1;
    }

    /**
     * Util method to convert TestCase DTO to a TestCase Bean object.
     *
     * @param testCase TestCase DTO to be converted
     * @return TestCase bean object with necessary information
     */
    static TestCase getTestCaseBean(org.wso2.testgrid.common.TestCase testCase) {
        TestCase testCase1 = new TestCase();
        testCase1.setId(testCase.getId());
        testCase1.setName(testCase.getName());
        testCase1.setSuccess(testCase.isTestSuccess());
        testCase1.setModifiedTimestamp(testCase.getModifiedTimestamp().getTime());
        testCase1.setCreatedTimestamp(testCase.getStartTimestamp().getTime());
        return testCase1;
    }

    /**
     * Util method to convert list of TestCase DTOs to a list of TestCase Bean objects.
     *
     * @param testCases list of TestCase DTOs to be converted
     * @return List of TestCase bean objects with necessary information
     */
    static List<TestCase> getTestCaseBeans(List<org.wso2.testgrid.common.TestCase> testCases) {
        List<TestCase> testCases1 = new ArrayList<>();

        for (org.wso2.testgrid.common.TestCase testCase : testCases) {
            testCases1.add(getTestCaseBean(testCase));
        }
        return testCases1;
    }
}
