/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.testgrid.reporting;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.TestCaseUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Properties;

public class BaseClass {

    static final String TESTGRID_HOME = Paths.get("target", "testgrid-home").toString();

    @Mock
    TestScenarioUOW testScenarioUOW;
    @Mock
    TestPlanUOW testPlanUOW;
    @Mock
    TestCaseUOW testCaseUOW;

    Product product;
    TestPlan testPlan;
    Path productDir;
    String testPlanId = "abc";

    @BeforeMethod
    public void init() throws Exception {
        System.setProperty(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY, TESTGRID_HOME);
        Files.createDirectories(Paths.get(TESTGRID_HOME));

        final String randomStr = StringUtil.generateRandomString(5);
        String productName = "wso2-" + randomStr;
        this.product = new Product();
        product.setName(productName);
        productDir = Paths.get(TESTGRID_HOME).resolve("jobs").resolve(productName);
        Files.createDirectories(productDir);

        MockitoAnnotations.initMocks(this);

        testPlan = new TestPlan();
        testPlan.setInfraParameters("{\"OSVersion\":\"2016\",\"JDK\":\"ORACLE_JDK8\",\"OS\":\"Windows\","
                + "\"DBEngineVersion\":\"5.7\",\"DBEngine\":\"mysql\"}");
        testPlan.setStatus(Status.FAIL);
        testPlan.setId(testPlanId);

        InfrastructureConfig infraConfig = new InfrastructureConfig();
        Properties p = new Properties();
        p.setProperty("DBEngine", "mysql");
        p.setProperty("DBEngineVersion", "5.7");
        p.setProperty("OS", "CentOS");
        p.setProperty("JDK", "ORACLE_JDK8");
        infraConfig.setParameters(p);
        testPlan.setInfrastructureConfig(infraConfig);

        TestScenario s = new TestScenario();
        s.setName("Sample scenario 01");
        s.setStatus(Status.SUCCESS);
        TestScenario s2 = new TestScenario();
        s2.setName("Sample scenario 02");
        s2.setStatus(Status.FAIL);

        TestCase tc = new TestCase();
        tc.setSuccess(true);
        tc.setFailureMessage("success");
        tc.setName("Sample Testcase 01");
        tc.setTestScenario(s);
        s.addTestCase(tc);

        tc = new TestCase();
        tc.setSuccess(false);
        tc.setFailureMessage("fail");
        tc.setName("Sample Testcase 02");
        tc.setTestScenario(s2);
        s2.addTestCase(tc);

        tc = new TestCase();
        tc.setSuccess(false);
        tc.setFailureMessage("fail");
        tc.setName("Sample Testcase 03");
        tc.setTestScenario(s2);
        s2.addTestCase(tc);
        testPlan.setTestScenarios(Arrays.asList(s, s2));

        DeploymentPattern dp = new DeploymentPattern();
        dp.setName("dp");
        dp.setProduct(product);
        testPlan.setDeploymentPattern(dp);

        final Path testPlanPath = Paths.get("src", "test", "resources", "test-plan-01.yaml");
        final Path testPlanFinalPath = Paths.get("target", "testgrid-home", TestGridConstants.TESTGRID_JOB_DIR,
                product.getName(), TestGridConstants.PRODUCT_TEST_PLANS_DIR, "test-plan-01.yaml");
        Files.createDirectories(testPlanFinalPath.getParent());
        Files.copy(testPlanPath, testPlanFinalPath, StandardCopyOption.REPLACE_EXISTING);

        Path testSuiteTxtPath = Paths.get("src", "test", "resources", "surefire-reports");
        Path testSuiteTxtFinalPath = TestGridUtil.getSurefireReportsDir(testPlan);
        FileUtils.copyDirectory(testSuiteTxtPath.toFile(), testSuiteTxtFinalPath.toFile());
    }

}
