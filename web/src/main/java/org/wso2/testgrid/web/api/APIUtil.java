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
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.web.bean.DeploymentPattern;
import org.wso2.testgrid.web.bean.Product;
import org.wso2.testgrid.web.bean.TestCase;
import org.wso2.testgrid.web.bean.TestPlan;
import org.wso2.testgrid.web.bean.TestScenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (product != null) {
            productBean.setId(product.getId());
            productBean.setName(product.getName());
            productBean.setLastSuccessTimestamp(product.getLastSuccessTimestamp());
            productBean.setLastFailureTimestamp(product.getLastFailureTimestamp());
        }
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
     * @param status            Status of the last test execution
     * @return {@link DeploymentPattern} instance with necessary information
     */
    static DeploymentPattern getDeploymentPatternBean(org.wso2.testgrid.common.DeploymentPattern deploymentPattern,
                                                      String status) {
        DeploymentPattern deploymentPatternBean = new DeploymentPattern();
        if (deploymentPattern != null) {
            deploymentPatternBean.setId(deploymentPattern.getId());
            deploymentPatternBean.setName(deploymentPattern.getName());
            deploymentPatternBean.setProductId(deploymentPattern.getProduct().getId());
            deploymentPatternBean.setTestStatus(status);
        }
        return deploymentPatternBean;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.DeploymentPattern} instances to a list of
     * {@link DeploymentPattern} instances.
     *
     * @param deploymentPatterns list of {@link org.wso2.testgrid.common.DeploymentPattern} instances to be converted
     * @param stats              list of {@link DeploymentPatternTestFailureStat} instances including test-stats
     * @return List of {@link DeploymentPattern} instances with necessary information
     */
    static List<DeploymentPattern> getDeploymentPatternBeans(List<org.wso2.testgrid.common.DeploymentPattern>
                                                                     deploymentPatterns,
                                                             List<DeploymentPatternTestFailureStat> stats) {
        List<DeploymentPattern> deploymentPatternsBean = new ArrayList<>();
        //Convert List to a Map
        Map<String, Long> failureStats = new HashMap<>();
        for (DeploymentPatternTestFailureStat stat : stats) {
            failureStats.put(stat.getDeploymentPatternId(), stat.getFailureCount());
        }

        for (org.wso2.testgrid.common.DeploymentPattern deploymentPattern : deploymentPatterns) {

            if (failureStats.containsKey(deploymentPattern.getId())) {
                deploymentPatternsBean.add(getDeploymentPatternBean(deploymentPattern, FAILURE_STATUS));
            } else {
                deploymentPatternsBean.add(getDeploymentPatternBean(deploymentPattern, SUCCESS_STATUS));
            }
        }
        return deploymentPatternsBean;
    }

    /**
     * Util method to convert {@link org.wso2.testgrid.common.TestPlan} instance to a {@link TestPlan} instance.
     *
     * @param testPlan             {@link org.wso2.testgrid.common.TestPlan} instance to be converted
     * @param requireTestScenarios boolean flag to indicate whether to include test-scenario info
     * @return {@link TestPlan} instance  with necessary information
     */
    static TestPlan getTestPlanBean(Set<InfrastructureValueSet> infraValueSet,
                                    org.wso2.testgrid.common.TestPlan testPlan, boolean requireTestScenarios) {
        TestPlan testPlanBean = new TestPlan();
        if (testPlan != null) {
            testPlanBean.setId(testPlan.getId());
            testPlanBean.setDeploymentPattern(testPlan.getDeploymentPattern().getName());
            testPlanBean.setDeploymentPatternId(testPlan.getDeploymentPattern().getId());
            String infraParams = "{" + TestGridUtil.getInfraParamsOfTestPlan(infraValueSet, testPlan).stream()
                    .map(infra -> "\"" + infra.getType() + "\":\"" + infra.getName() + "\"")
                    .collect(Collectors.joining(",")) + "}";

            testPlanBean.setInfraParams(infraParams);
            testPlanBean.setLogUrl(testPlan.getLogUrl());
            testPlanBean.setStatus(testPlan.getStatus().toString());
            testPlanBean.setCreatedTimestamp(testPlan.getCreatedTimestamp());
            testPlanBean.setModifiedTimestamp(testPlan.getModifiedTimestamp());
            testPlanBean.setBuildURL(testPlan.getBuildURL());
            testPlanBean.setTestRunNumber(testPlan.getTestRunNumber());
            if (requireTestScenarios) {
                testPlanBean.setTestScenarios(getTestScenarioBeans(testPlan.getTestScenarios(), false));
            }
        }
        return testPlanBean;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.TestPlan} instances to a list of {@link TestPlan}
     * instances.
     *
     * @param testPlans            list of {@link org.wso2.testgrid.common.TestPlan} instances to be converted
     * @param requireTestScenarios boolean flag to indicate whether to include test-scenario info
     * @return List of {@link TestPlan} instances with necessary information
     */
    static List<TestPlan> getTestPlanBeans(Set<InfrastructureValueSet> infraValueSet,
                                           List<org.wso2.testgrid.common.TestPlan> testPlans,
                                           boolean requireTestScenarios) {
        List<TestPlan> plans = new ArrayList<>();

        for (org.wso2.testgrid.common.TestPlan testPlan : testPlans) {
            plans.add(getTestPlanBean(infraValueSet, testPlan, requireTestScenarios));
        }
        return plans;
    }

    /**
     * Util method to convert {@link org.wso2.testgrid.common.TestScenario} instance to a {@link TestScenario} instance.
     *
     * @param testScenario     {@link org.wso2.testgrid.common.TestScenario} instance to be converted
     * @param requireTestCases boolean flag to indicate whether to include test-case info
     * @return {@link TestScenario} instance with necessary information
     */
    static TestScenario getTestScenarioBean(org.wso2.testgrid.common.TestScenario testScenario,
                                            boolean requireTestCases) {
        TestScenario testScenarioBean = new TestScenario();
        if (testScenario != null) {
            testScenarioBean.setId(testScenario.getId());
            testScenarioBean.setName(testScenario.getName());
            testScenarioBean.setDescription(testScenario.getDescription());
            testScenarioBean.setStatus(testScenario.getStatus().toString());
            if (requireTestCases) {
                testScenarioBean.setTestCases(getTestCaseBeans(testScenario.getTestCases()));
            }
        }
        return testScenarioBean;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.TestScenario} instances to a list of
     * {@link Product} instances.
     *
     * @param testScenarios    list of {@link org.wso2.testgrid.common.TestScenario} instances to be converted
     * @param requireTestCases boolean flag to indicate whether to include test-case info
     * @return List of {@link TestScenario} instances with necessary information
     */
    static List<TestScenario> getTestScenarioBeans(List<org.wso2.testgrid.common.TestScenario> testScenarios,
                                                   boolean requireTestCases) {
        List<TestScenario> testScenariosBean = new ArrayList<>();

        for (org.wso2.testgrid.common.TestScenario testScenario : testScenarios) {
            testScenariosBean.add(getTestScenarioBean(testScenario, requireTestCases));
        }
        return testScenariosBean;
    }

    /**
     * Util method to convert {@link org.wso2.testgrid.common.TestCase} instance to a {@link TestCase} instance.
     *
     * @param testCase {@link org.wso2.testgrid.common.TestCase} instance to be converted
     * @return {@link TestCase} instance with necessary information
     */
    static TestCase getTestCaseBean(org.wso2.testgrid.common.TestCase testCase) {
        TestCase testCaseBean = new TestCase();
        if (testCase != null) {
            testCaseBean.setId(testCase.getId());
            testCaseBean.setName(testCase.getName());
            testCaseBean.setStatus(testCase.getStatus());
            testCaseBean.setModifiedTimestamp(testCase.getModifiedTimestamp());
            testCaseBean.setCreatedTimestamp(testCase.getCreatedTimestamp());
            testCaseBean.setErrorMsg(testCase.getFailureMessage());
        }
        return testCaseBean;
    }

    /**
     * Util method to convert list of {@link org.wso2.testgrid.common.TestCase} instances to a list of {@link TestCase}
     * instances.
     *
     * @param testCases list of {@link org.wso2.testgrid.common.TestCase} instances to be converted
     * @return List of {@link TestCase} instances with necessary information
     */
    static List<TestCase> getTestCaseBeans(List<org.wso2.testgrid.common.TestCase> testCases) {
        List<TestCase> testCasesBean = new ArrayList<>();

        for (org.wso2.testgrid.common.TestCase testCase : testCases) {
            testCasesBean.add(getTestCaseBean(testCase));
        }
        return testCasesBean;
    }
}
