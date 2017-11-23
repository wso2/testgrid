//package org.wso2.carbon.testgrid.automation.beans;
//
//import org.mockito.Mockito;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//import org.wso2.carbon.testgrid.automation.TestAutomationException;
//import org.wso2.carbon.testgrid.common.Deployment;
//import org.wso2.testgrid.automation.beans.TestNGTest;
//
//import java.io.File;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.testng.Assert.assertTrue;
//
//public class TestNGTestTest {
//
//    private TestNGTest testNGTest;
//    private List<String> jarNames;
//    private File directory;
//
//    /**
//     * Creates an instance of TestNGTest and sets TestNG test jars
//     * for test execution
//     */
//    @BeforeMethod
//    public void setUp() throws TestAutomationException {
//        testNGTest = new TestNGTest();
//        directory = new File("src/test/resources");
//        File[] jarList = directory.listFiles((dir, filename) -> filename.endsWith(".jar"));
//        jarNames = new ArrayList<String>();
//        for (File file : jarList) {
//            jarNames.add(file.getAbsolutePath());
//        }
//
//        testNGTest.setTestNGJars(jarNames);
//    }
//
//    /**
//     * Test if the execute() method executes tests of a provided
//     * set of test jars and produces results for each test jar.
//     * @throws Exception when execution fails
//     */
//    @Test
//    public void testExecute() throws TestAutomationException {
//        Deployment deployment = Mockito.mock(Deployment.class);
//        testNGTest.execute(Paths.get(directory.getAbsolutePath(), "Results").toString(), deployment);
//        for (String jar : jarNames){
//            assertTrue(Paths.get(jar).toFile().exists());
//        }
//    }
//
//}