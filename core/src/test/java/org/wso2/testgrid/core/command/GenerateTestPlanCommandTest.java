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
 *
 */

package org.wso2.testgrid.core.command;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.infrastructure.DefaultInfrastructureTypes;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.infrastructure.InfrastructureCombinationsProvider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(StringUtil.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class GenerateTestPlanCommandTest extends PowerMockTestCase {

    private static final Logger logger = LoggerFactory.getLogger(GenerateTestPlanCommandTest.class);
    private static final String EXPECTED_TEST_PLAN_PATH = Paths.get("src", "test", "resources", "test-plan-01.yaml")
            .toString();
    private static final String TESTGRID_HOME = Paths.get("target", "testgrid-home").toString();
    private String actualTestPlanFileLocation;
    @Mock
    private InfrastructureCombinationsProvider infrastructureCombinationsProvider;
    @Mock
    private ProductUOW productUOW;
    @Mock
    private DeploymentPatternUOW deploymentPatternUOW;
    @Mock
    private TestPlanUOW testPlanUOW;

    private Product product;
    private TestPlan testPlan;
    private DeploymentPattern deploymentPattern;
    
    @BeforeMethod
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        System.setProperty(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY, TESTGRID_HOME);

        this.product = new Product();
        int random = new Random().nextInt(100);
        product.setName("wso2-" + random);

        this.testPlan = new TestPlan();
        testPlan.setId("abc");
        testPlan.setTestRunNumber(1);

        this.deploymentPattern = new DeploymentPattern();
        deploymentPattern.setName("default-" + random);
        deploymentPattern.setProduct(product);

        actualTestPlanFileLocation = Paths.get("target", "testgrid-home", TestGridConstants.TESTGRID_JOB_DIR,
                product.getName(), TestGridConstants.PRODUCT_TEST_PLANS_DIR, "test-plan-01.yaml").toString();
    }

    @Test(dataProvider = "getJobConfigData")
    public void testExecute(String jobConfigFile, String workingDir) throws Exception {
        infrastructureCombinationsProvider = mock(InfrastructureCombinationsProvider.class);
        productUOW = mock(ProductUOW.class);
        deploymentPatternUOW = mock(DeploymentPatternUOW.class);
        testPlanUOW = mock(TestPlanUOW.class);

        PowerMockito.spy(StringUtil.class);
        when(StringUtil.generateRandomString(anyInt())).thenReturn("");

        InfrastructureParameter param = new InfrastructureParameter("ubuntu_16.04", DefaultInfrastructureTypes
                .OPERATING_SYSTEM, "{}", true);
        InfrastructureCombination comb1 = new InfrastructureCombination(Collections.singleton(param));
        when(infrastructureCombinationsProvider.getCombinations()).thenReturn(Collections.singleton(comb1));

        logger.info("Product : " + product.getName());
        when(productUOW.persistProduct(anyString())).thenReturn(product);
        when(testPlanUOW.persistTestPlan(any(TestPlan.class))).thenReturn(testPlan);
        when(deploymentPatternUOW.getDeploymentPattern(any(Product.class), anyString())).
                thenReturn(Optional.of(deploymentPattern));

        GenerateTestPlanCommand generateTestPlanCommand = new GenerateTestPlanCommand(product.getName(),
                jobConfigFile, workingDir, infrastructureCombinationsProvider, productUOW, deploymentPatternUOW,
                testPlanUOW);
        generateTestPlanCommand.execute();

        Path actualTestPlanPath = Paths.get(actualTestPlanFileLocation);
        Path expectedTestPlanPath = Paths.get(EXPECTED_TEST_PLAN_PATH);
        Assert.assertTrue(Files.exists(actualTestPlanPath));

        String generatedTestPlanContent = new String(Files.readAllBytes(actualTestPlanPath), StandardCharsets.UTF_8);
        String expectedTestPlanContent = new String(Files.readAllBytes(expectedTestPlanPath), StandardCharsets.UTF_8);

        String repoPath = "./src/test/resources/workspace";
        expectedTestPlanContent = expectedTestPlanContent
                .replaceAll(repoPath, Paths.get(repoPath).toAbsolutePath().normalize().toString());

        Assert.assertEquals(generatedTestPlanContent, expectedTestPlanContent,
                String.format("Generated test-plan is not correct: \n%s", generatedTestPlanContent));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Path testPlanPath = Paths.get(TESTGRID_HOME, TestGridConstants.TESTGRID_JOB_DIR, product.getName(),
                TestGridConstants.PRODUCT_TEST_PLANS_DIR, "test-plan-01.yaml");
        if (Files.exists(testPlanPath)) {
            FileUtils.forceDelete(testPlanPath.toFile());
        } else {
            Assert.fail("Failed to delete test-plan. Test plan does not exist: " + testPlanPath.toString());
        }
    }

    @DataProvider
    public Object[][] getJobConfigData() {
        return new Object[][] {
                { "src/test/resources/job-config.yaml", "." },
                { "src/test/resources/job-config2.yaml", "" }
        };
    }

}
