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

import org.wso2.testgrid.common.DeploymentPatternTestFailureStat;
import org.wso2.testgrid.web.bean.DeploymentPattern;
import org.wso2.testgrid.web.bean.Product;
import org.wso2.testgrid.web.bean.TestCase;
import org.wso2.testgrid.web.bean.TestPlan;
import org.wso2.testgrid.web.bean.TestScenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class which holds utility methods required for TestGrid APIs.
 */
class APIUtil {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAILURE_STATUS = "FAILURE";

    /**
     * Util method to convert {@link org.wso2.testgrid.common.Product} instance to a {@link Product} instance.
     *
     * @param product {@link org.wso2.testgrid.common.Product} instance to be converted
     * @return {@link Product} instance with necessary information
     */
    static Product getProductBean(org.wso2.testgrid.common.Product product) {
        Product productBean = new Product();
        productBean.setId(product.getId());
        productBean.setName(product.getName());
        productBean.setVersion(product.getVersion());
        productBean.setChannel(product.getChannel().name());
        return productBean;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.Product} instances to a list of {@link Product}
     * instances.
     *
     * @param products list of {@link org.wso2.testgrid.common.Product} instances to be converted
     * @return List of {@link Product} instances with necessary information
     */
    static List<Product> getProductBeans(List<org.wso2.testgrid.common.Product> products) {
        List<Product> plans = new ArrayList<>();

        for (org.wso2.testgrid.common.Product product : products) {
            plans.add(getProductBean(product));
        }
        return plans;
    }

    /**
     * Util method to convert {@link org.wso2.testgrid.common.DeploymentPattern} instance to a
     * {@link DeploymentPattern} instance.
     *
     * @param deploymentPattern {@link org.wso2.testgrid.common.DeploymentPattern} instance to be converted
     * @param status Status of the last test execution
     * @return {@link DeploymentPattern} instance with necessary information
     */
    static DeploymentPattern getDeploymentPatternBean(org.wso2.testgrid.common.DeploymentPattern deploymentPattern,
                                                      String status) {
        DeploymentPattern deploymentPattern1 = new DeploymentPattern();
        deploymentPattern1.setId(deploymentPattern.getId());
        deploymentPattern1.setName(deploymentPattern.getName());
        deploymentPattern1.setTestStatus(status);
        return deploymentPattern1;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.DeploymentPattern} instances to a list of
     * {@link DeploymentPattern} instances.
     *
     * @param deploymentPatterns list of {@link org.wso2.testgrid.common.DeploymentPattern} instances to be converted
     * @param stats list of {@link DeploymentPatternTestFailureStat} instances including test-stats
     * @return List of {@link DeploymentPattern} instances with necessary information
     */
    static List<DeploymentPattern> getDeploymentPatternBeans(List<org.wso2.testgrid.common.DeploymentPattern>
                                                                     deploymentPatterns,
                                                             List<DeploymentPatternTestFailureStat> stats) {
        List<DeploymentPattern> deploymentPatterns1 = new ArrayList<>();
        //Convert List to a Map
        Map<String, Integer> failureStats = new HashMap<>();
        for (DeploymentPatternTestFailureStat stat : stats) {
            failureStats.put(stat.getDeploymentPatternId() , stat.getFailureCount());
        }

        for (org.wso2.testgrid.common.DeploymentPattern deploymentPattern : deploymentPatterns) {

            if (failureStats.containsKey(deploymentPattern.getId())) {
                deploymentPatterns1.add(getDeploymentPatternBean(deploymentPattern, FAILURE_STATUS));
            } else {
                deploymentPatterns1.add(getDeploymentPatternBean(deploymentPattern, SUCCESS_STATUS));
            }
        }
        return deploymentPatterns1;
    }

    /**
     * Util method to convert {@link org.wso2.testgrid.common.TestPlan} instance to a {@link TestPlan} instance
     *
     * @param testPlan {@link org.wso2.testgrid.common.TestPlan} instance to be converted
     * @param requireTestScenarios boolean flag to indicate whether to include test-scenario info
     * @return {@link TestPlan} instance  with necessary information
     */
    static TestPlan getTestPlanBean(org.wso2.testgrid.common.TestPlan testPlan, boolean requireTestScenarios) {
        TestPlan testPlan1 = new TestPlan();
        testPlan1.setId(testPlan.getId());
        testPlan1.setDeploymentPattern(testPlan.getDeploymentPattern().getName());
        testPlan1.setDeploymentPatternId(testPlan.getDeploymentPattern().getId());
        testPlan1.setInfraParams(testPlan.getInfraParameters());
        testPlan1.setStatus(testPlan.getStatus().toString());
        if (requireTestScenarios) {
            testPlan1.setTestScenarios(getTestScenarioBeans(testPlan.getTestScenarios(), false));
        }
        return testPlan1;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.TestPlan} instances to a list of {@link TestPlan}
     * instances.
     *
     * @param testPlans list of {@link org.wso2.testgrid.common.TestPlan} instances to be converted
     * @param requireTestScenarios boolean flag to indicate whether to include test-scenario info
     * @return List of {@link TestPlan} instances with necessary information
     */
    static List<TestPlan> getTestPlanBeans(List<org.wso2.testgrid.common.TestPlan> testPlans,
                                           boolean requireTestScenarios) {
        List<TestPlan> plans = new ArrayList<>();

        for (org.wso2.testgrid.common.TestPlan testPlan : testPlans) {
            plans.add(getTestPlanBean(testPlan, requireTestScenarios));
        }
        return plans;
    }

    /**
     * Util method to convert {@link org.wso2.testgrid.common.TestScenario} instance to a {@link TestScenario} instance.
     *
     * @param testScenario {@link org.wso2.testgrid.common.TestScenario} instance to be converted
     * @param requireTestCases boolean flag to indicate whether to include test-case info
     * @return {@link TestScenario} instance with necessary information
     */
    static TestScenario getTestScenarioBean(org.wso2.testgrid.common.TestScenario testScenario,
                                            boolean requireTestCases) {
        TestScenario testScenario1 = new TestScenario();
        testScenario1.setId(testScenario.getId());
        testScenario1.setName(testScenario.getName());
        testScenario1.setStatus(testScenario.getStatus().toString());
        if (requireTestCases) {
            testScenario1.setTestCases(getTestCaseBeans(testScenario.getTestCases()));
        }
        return testScenario1;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.TestScenario} instances to a list of
     * {@link Product} instances.
     *
     * @param testScenarios list of {@link org.wso2.testgrid.common.TestScenario} instances to be converted
     * @param requireTestCases boolean flag to indicate whether to include test-case info
     * @return List of {@link TestScenario} instances with necessary information
     */
    static List<TestScenario> getTestScenarioBeans(List<org.wso2.testgrid.common.TestScenario> testScenarios,
                                                   boolean requireTestCases) {
        List<TestScenario> testScenarios1 = new ArrayList<>();

        for (org.wso2.testgrid.common.TestScenario testScenario : testScenarios) {
            testScenarios1.add(getTestScenarioBean(testScenario, requireTestCases));
        }
        return testScenarios1;
    }

    /**
     * Util method to convert {@link org.wso2.testgrid.common.TestCase} instance to a {@link TestCase} instance.
     *
     * @param testCase {@link org.wso2.testgrid.common.TestCase} instance to be converted
     * @return {@link TestCase} instance with necessary information
     */
    static TestCase getTestCaseBean(org.wso2.testgrid.common.TestCase testCase) {
        TestCase testCase1 = new TestCase();
        testCase1.setId(testCase.getId());
        testCase1.setName(testCase.getName());
        testCase1.setSuccess(testCase.isSuccess());
        testCase1.setModifiedTimestamp(testCase.getModifiedTimestamp().getTime());
        testCase1.setCreatedTimestamp(testCase.getCreatedTimestamp().getTime());
        return testCase1;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.TestCase} instances to a list of {@link TestCase}
     * instances.
     *
     * @param testCases list of {@link org.wso2.testgrid.common.TestCase} instances to be converted
     * @return List of {@link TestCase} instances with necessary information
     */
    static List<TestCase> getTestCaseBeans(List<org.wso2.testgrid.common.TestCase> testCases) {
        List<TestCase> testCases1 = new ArrayList<>();

        for (org.wso2.testgrid.common.TestCase testCase : testCases) {
            testCases1.add(getTestCaseBean(testCase));
        }
        return testCases1;
    }
}
