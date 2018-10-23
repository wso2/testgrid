package org.wso2.testgrid.core.command;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(StringUtil.class)
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*", "javax.net.ssl.*" })
public class CleanUpCommandTest extends PowerMockTestCase {

    private static final Logger logger = LoggerFactory.getLogger(CleanUpCommandTest.class);
    private static final String infraParamsString = "{\"operating_system\":\"ubuntu_16.04\"}";

    @Mock
    private TestPlanUOW testPlanUOW;

    private Product product;
    private DeploymentPattern deploymentPattern;

    private String actualTestPlanFileLocation;
    private String workspaceDir;

    @Test
    public void testCleanup() throws Exception {
        final String randomStr = StringUtil.generateRandomString(5);
        testPlanUOW = mock(TestPlanUOW.class);
        List<String> dataToDelete = new ArrayList<String>();
        dataToDelete.add("TP1");
        String grafanaUrl = "ec2-34-232-211-33.compute-1.amazonaws.com:3000";

        when(testPlanUOW.getTestPlansToCleanup(10)).thenReturn(dataToDelete);
        TestPlan testPlan = new TestPlan();
        String productName = "wso2-" + randomStr;
        actualTestPlanFileLocation = Paths.get("target", "testgrid-home", TestGridConstants.TESTGRID_JOB_DIR,
                productName, TestGridConstants.PRODUCT_TEST_PLANS_DIR, "test-plan-01.yaml").toString();
        workspaceDir = Paths.get(TestGridUtil.getTestGridHomePath(), TestGridConstants.TESTGRID_JOB_DIR,
                productName).toString();

        this.product = new Product();
        product.setName(productName);

        this.deploymentPattern = new DeploymentPattern();
        deploymentPattern.setName("default-" + randomStr);
        deploymentPattern.setProduct(product);

        testPlan.setId("TP1");
        testPlan.setTestRunNumber(1);
        testPlan.setDeploymentPattern(deploymentPattern);
        testPlan.setInfraParameters(infraParamsString);
        testPlan.setDeployerType(TestPlan.DeployerType.SHELL);
        testPlan.setScenarioTestsRepository(Paths.get(workspaceDir, "workspace", "scenarioTests").toString());
        testPlan.setInfrastructureRepository(Paths.get(workspaceDir, "workspace", "scenarioTests").toString());
        testPlan.setDeploymentRepository(Paths.get(workspaceDir, "workspace", "deployment").toString());
        testPlan.setKeyFileLocation(Paths.get(workspaceDir, "workspace", "testkey.pem").toString());
        testPlan.setWorkspace(workspaceDir);

        when(testPlanUOW.getTestPlanById("TP1")).thenReturn(Optional.of(testPlan));
        CleanUpCommand cleanUpCommand = new CleanUpCommand(0, 10, testPlanUOW, dataToDelete,
                grafanaUrl);

        logger.info("Status of the cleanup : " + cleanUpCommand.getStatus());
        cleanUpCommand.execute();
        Assert.assertEquals(cleanUpCommand.getToDelete().size(), dataToDelete.size());
        Assert.assertEquals(cleanUpCommand.getToDelete().get(0), dataToDelete.get(0));
    }


}
