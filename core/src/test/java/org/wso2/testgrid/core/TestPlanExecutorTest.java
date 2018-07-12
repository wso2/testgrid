package org.wso2.testgrid.core;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.core.command.RunTestPlanCommandTest;
import org.wso2.testgrid.dao.uow.TestCaseUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;
import org.wso2.testgrid.logging.plugins.LogFilePathLookup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.spy;

/**
 * Tests the summary log that gets printed at the end.
 */
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*" })
public class TestPlanExecutorTest {

    private static final Logger logger = LoggerFactory.getLogger(RunTestPlanCommandTest.class);

    @Mock
    private TestScenarioUOW testScenarioUOW;
    @Mock
    private TestPlanUOW testPlanUOW;
    @Mock
    private TestCaseUOW testCaseUOW;

    private Product product;
    private TestPlanExecutor testPlanExecutor;
    private ScenarioExecutor scenarioExecutor;

    @BeforeMethod
    public void init() throws Exception {
        final String randomStr = StringUtil.generateRandomString(5);
        String productName = "wso2-" + randomStr;
        this.product = new Product();
        product.setName(productName);

        scenarioExecutor = new ScenarioExecutor(testScenarioUOW, testCaseUOW);
        testPlanExecutor = new TestPlanExecutor(scenarioExecutor, testPlanUOW, testScenarioUOW);
        testPlanExecutor = spy(testPlanExecutor);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPrintSummary() throws Exception {
        TestPlan testPlan = new TestPlan();
        testPlan.setInfraParameters("{DBEngine: \"mysql\"}");
        testPlan.setStatus(Status.FAIL);

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

        LogFilePathLookup.setLogFilePath("TestReportEngineTest.log");
        final Path testgridLogPath = Paths.get("target", "TestReportEngineTest.log").toAbsolutePath();
        final TestPlanExecutor testPlanExecutor = new TestPlanExecutor(scenarioExecutor, testPlanUOW, testScenarioUOW);
        testPlanExecutor.printSummary(testPlan, System.currentTimeMillis() - 1525414900000L);

        logger.info("Reading TestReportEngineTest.log at " + testgridLogPath.toAbsolutePath());
        waitForLog(testgridLogPath.toAbsolutePath());

        final List<String> testgridLog = Files.readAllLines(testgridLogPath.toAbsolutePath());
        logger.info("TestReportEngineTest.log content: " + testgridLog);
        boolean failedTestsTextFound = false;
        boolean testPlanSummaryTextFound = false;
        boolean testRunFailTextFound = false;
        for (int i = 0; i < testgridLog.size(); i++) {
            if (testgridLog.get(i).contains("Failed tests:")) {
                Assert.assertTrue(testgridLog.get(i + 1).endsWith("Sample scenario 02::Sample Testcase 02: fail"));
                Assert.assertTrue(testgridLog.get(i + 2).endsWith("Sample scenario 02::Sample Testcase 03: fail"));
                Assert.assertTrue(testgridLog.get(i + 3).endsWith(" - "));
                Assert.assertTrue(testgridLog.get(i + 4).endsWith("Tests run: 3, Failures/Errors: 2"));
                failedTestsTextFound = true;
            } else if (testgridLog.get(i).contains("Test Plan Summary for ")) {
                Assert.assertTrue(testgridLog.get(i + 1).endsWith("Sample scenario 01 "
                        + "................................. SUCCESS"));
                Assert.assertTrue(testgridLog.get(i + 2).endsWith("Sample scenario 02 "
                        + "................................. FAIL"));
                testPlanSummaryTextFound = true;
            } else if (testgridLog.get(i).contains("TEST RUN FAIL")) {
                testRunFailTextFound = true;
            }
        }

        Assert.assertTrue(failedTestsTextFound, "'Failed tests:' text was not found.");
        Assert.assertTrue(testPlanSummaryTextFound, "'Test Plan Summary for:' text was not found.");
        Assert.assertTrue(testRunFailTextFound, "'TEST RUN FAIL:' text was not found.");

        LogFilePathLookup.setLogFilePath("testgrid.log");
    }

    private void waitForLog(Path path) {
        int retryCount = 100;

        while (retryCount-- > 0) {
            try {
                if (Files.exists(path)) {
                    logger.info("Log file found at " + path + " in " + (101 - retryCount) + " tries.");
                    return;
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
