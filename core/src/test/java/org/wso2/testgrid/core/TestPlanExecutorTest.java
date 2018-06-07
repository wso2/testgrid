package org.wso2.testgrid.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the summary log that gets printed at the end.
 *
 */
public class TestPlanExecutorTest {
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

        final Path testgridLogPath = Paths.get("target", "testgrid.log");
        Files.deleteIfExists(testgridLogPath);
        final TestPlanExecutor testPlanExecutor = new TestPlanExecutor();
        testPlanExecutor.printSummary(testPlan, System.currentTimeMillis() - 1525414900000L);

        final List<String> testgridLog = Files.readAllLines(testgridLogPath);
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

        Files.deleteIfExists(testgridLogPath);
    }

}
