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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.TestCaseUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Base test class for reporting module.
 *
 */
public class BaseClass {

    private static final String TESTGRID_HOME = Paths.get("target", "testgrid-home").toString();
    private static final Logger logger = LoggerFactory.getLogger(BaseClass.class);

    @Mock
    protected TestScenarioUOW testScenarioUOW;
    @Mock
    protected TestPlanUOW testPlanUOW;
    @Mock
    protected TestCaseUOW testCaseUOW;

    protected Product product;
    //    protected List<TestPlan> testPlans;
    protected Path productDir;
    protected String testPlanId = "abc";

    @DataProvider
    public static Object[][] testPlanInputsNum() {
        return new Object[][] {
                new Object[] { "01" },
                new Object[] { "02" }
        };
    }

    @BeforeMethod
    public void init() throws Exception {
        System.setProperty(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY, TESTGRID_HOME);
        Files.createDirectories(Paths.get(TESTGRID_HOME));

        MockitoAnnotations.initMocks(this);

        final String randomStr = StringUtil.generateRandomString(5);
        String productName = "wso2-" + randomStr;
        this.product = new Product();
        product.setName(productName);
        productDir = Paths.get(TESTGRID_HOME).resolve("jobs").resolve(productName);
        Files.createDirectories(productDir);

    }

    protected Path getTestPlansPath() {
        return Paths.get("target", "testgrid-home", TestGridConstants.TESTGRID_JOB_DIR,
                product.getName(), TestGridConstants.PRODUCT_TEST_PLANS_DIR);
    }

    /**
     * get the test-plans for the current testng data-provider input.
     *
     * @param testNum the data provider input
     * @return list of test plans
     * @throws Exception in case of failure
     */
    protected List<TestPlan> getTestPlansFor(String testNum) throws Exception {
        List<TestPlan> testPlans;
        switch (testNum) {
        case "01":
            testPlans = prepareTestNum01();
            break;
        case "02":
            testPlans = prepareTestNum02();
            break;
        default:
            throw new Exception("Invalid data provider input: " + testNum);
        }
        return testPlans;
    }

    protected List<TestPlan> prepareTestNum01() throws IOException {
        logger.info("--- Preparing data provider 01 ---");
        TestPlan testPlan = new TestPlan();
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
        tc.setSuccess(Status.SUCCESS);
        tc.setFailureMessage("success");
        tc.setName("Sample Testcase 01");
        tc.setTestScenario(s);
        s.addTestCase(tc);

        tc = new TestCase();
        tc.setSuccess(Status.FAIL);
        tc.setFailureMessage("fail");
        tc.setName("Sample Testcase 02");
        tc.setTestScenario(s2);
        s2.addTestCase(tc);

        tc = new TestCase();
        tc.setSuccess(Status.FAIL);
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
        final Path testPlanFinalPath = getTestPlansPath().resolve("test-plan-01.yaml");
        Files.createDirectories(testPlanFinalPath.getParent());
        Files.copy(testPlanPath, testPlanFinalPath, StandardCopyOption.REPLACE_EXISTING);

        Path testSuiteTxtPath = Paths.get("src", "test", "resources", "surefire-reports");
        Path testSuiteTxtFinalPath = TestGridUtil.getSurefireReportsDir(testPlan);
        FileUtils.copyDirectory(testSuiteTxtPath.toFile(), testSuiteTxtFinalPath.toFile());

        return Collections.singletonList(testPlan);
    }

    public List<TestPlan> prepareTestNum02() throws Exception {
        logger.info("--- Preparing data provider 02 ---");
        List<InfrastructureParameter> oses = new ArrayList<>();
        oses.add(getInfrastructureParameterFor("OS", "CentOS"));
        oses.add(getInfrastructureParameterFor("OS", "Windows"));

        List<InfrastructureParameter> dbs = new ArrayList<>();
        dbs.add(getInfrastructureParameterFor("DBEngine", "Oracle"));
        dbs.add(getInfrastructureParameterFor("DBEngine", "MySQL"));
        dbs.add(getInfrastructureParameterFor("DBEngine", "SQLServer"));

        List<InfrastructureParameter> jdks = new ArrayList<>();
        jdks.add(getInfrastructureParameterFor("JDK", "ORACLE_JDK8"));
        jdks.add(getInfrastructureParameterFor("JDK", "OPEN_JDK8"));

        final List<InfrastructureCombination> infraCombinations = getInfraCombinationsFor(oses, dbs, jdks);
        Assert.assertEquals(infraCombinations.size(), 12, "Infra comb generation error.");
        final Map<InfrastructureCombination, TestScenario> testCases = getTestCasesFor(infraCombinations);
        List<TestPlan> testPlans = getTestPlansFor(testCases);

        Files.createDirectories(getTestPlansPath());
        Yaml yaml = new Yaml(new NullRepresenter());
        for (int i = 0; i < testPlans.size(); i++) {
            final Path testPlansPath = getTestPlansPath().resolve(i + ".yaml");
            Writer writer = Files.newBufferedWriter(testPlansPath);
            yaml.dump(testPlans.get(i), writer);
        }

        return testPlans;
    }

    /**
     * Generate the test-plans for the given infrastructure combination list.
     *
     */
    private Map<InfrastructureCombination, TestScenario> getTestCasesFor(
            List<InfrastructureCombination> infraCombinations) {
        Map<InfrastructureCombination, TestScenario> testCases = new HashMap<>();

        //test case T1 fails only on CentOS.
        for (InfrastructureCombination infraCombination : infraCombinations) {
            TestCase t1 = new TestCase();
            t1.setName("T1");
            if (infraCombination.getParameters().stream().anyMatch(p -> p.getName().equals("CentOS"))) {
                t1.setSuccess(Status.FAIL);
                t1.setFailureMessage("I fail on CentOS");
            } else {
                t1.setSuccess(Status.SUCCESS);
                t1.setFailureMessage("Success. I only fail on CentOS.");
            }

            TestScenario s = new TestScenario();
            s.setName("Integration tests");
            s.setStatus(t1.getStatus());
            s.addTestCase(t1);
            t1.setTestScenario(s);
            testCases.put(infraCombination, s);
        }

        return testCases;
    }

    /**
     * Get list of infra combinations for the OS, DB, and JDK input parameters.
     *
     */
    private List<InfrastructureCombination> getInfraCombinationsFor(List<InfrastructureParameter> oses,
            List<InfrastructureParameter> dbs,
            List<InfrastructureParameter> jdks) {
        List<InfrastructureCombination> infrastructureCombinations = new ArrayList<>();
        for (InfrastructureParameter os : oses) {
            for (InfrastructureParameter db : dbs) {
                for (InfrastructureParameter jdk : jdks) {
                    final HashSet<InfrastructureParameter> params = new HashSet<>();
                    params.add(os);
                    params.add(db);
                    params.add(jdk);
                    infrastructureCombinations.add(new InfrastructureCombination(params));
                }
            }
        }
        return infrastructureCombinations;
    }

    private InfrastructureParameter getInfrastructureParameterFor(String type, String name) {
        return new InfrastructureParameter(name, type, "", true);
    }

    /**
     * Get the list of test plans for the infra combinations that are populated with @{@link TestScenario}s
     *
     * @param testScenarios list of test scenarios.
     * @return the test plans
     */
    private List<TestPlan> getTestPlansFor(Map<InfrastructureCombination, TestScenario> testScenarios)
            throws JsonProcessingException {
        List<TestPlan> testPlans = new ArrayList<>();
        int i = 0;
        for (InfrastructureCombination infrastructureCombination : testScenarios.keySet()) {

            TestPlan testPlan = new TestPlan();
            testPlan.setStatus(testScenarios.get(infrastructureCombination).getStatus());
            testPlan.setId(i + "");

            InfrastructureConfig infraConfig = new InfrastructureConfig();
            Properties props = new Properties();
            for (InfrastructureParameter infrastructureParameter : infrastructureCombination.getParameters()) {
                props.setProperty(infrastructureParameter.getType(), infrastructureParameter.getName());
            }
            infraConfig.setParameters(props);
            testPlan.setInfrastructureConfig(infraConfig);

            String jsonInfraParams = new ObjectMapper()
                    .writeValueAsString(props);
            testPlan.setInfraParameters(jsonInfraParams);

            testPlan.setTestScenarios(Collections.singletonList(testScenarios.get(infrastructureCombination)));
            testScenarios.get(infrastructureCombination).setTestPlan(testPlan);

            DeploymentPattern dp = new DeploymentPattern();
            dp.setName("dp");
            dp.setProduct(product);
            testPlan.setDeploymentPattern(dp);
            testPlans.add(testPlan);
            i++;
        }

        return testPlans;
    }

    /**
     * When persisting yaml test plans, skip any field that is null.
     *
     */
    private static class NullRepresenter extends Representer {

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
                Object propertyValue, Tag customTag) {
            if (propertyValue == null) {
                return null;
            } else {
                return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            }
        }
    }

}
