package org.wso2.carbon.testgrid.automation.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.TestManagerException;
import org.wso2.carbon.testgrid.automation.TestReaderException;

import org.wso2.carbon.testgrid.automation.beans.Test;

import org.wso2.carbon.testgrid.automation.executors.TestExecutorFactory;
import org.wso2.carbon.testgrid.automation.executors.common.TestExecutor;
import org.wso2.carbon.testgrid.automation.file.common.TestReader;
import org.wso2.carbon.testgrid.automation.file.common.TestReaderFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;

import java.io.File;
import java.util.List;

public class TestManager {

    private static final Log log = LogFactory.getLog(TestManager.class);

    private List<Test> tests;
    private Deployment deployment;
    private TestExecutor testExecutor;

    /**
     *
     * @param testConfig Test Configuration data model
     * @param deployment Deployment details of the current execution
     * @throws TestManagerException Initialization failed
     */
    public void init(TestConfiguration testConfig, Deployment deployment) throws TestManagerException{

        this.deployment = deployment;
        //get the test file structure
        TestReader testReader = TestReaderFactory.getTestReader(testConfig);
        this.testExecutor = TestExecutorFactory.getTestExecutor(testConfig);

        try {
            String test_grid_tests_location;
            String test_grid_tests_home = System.getenv("TEST_GRID_TESTS_HOME");
            if(test_grid_tests_home !=null){
                 test_grid_tests_location= test_grid_tests_home + File.separator + getTestfolderName(testConfig.getTestGitRepo());
            }else{
                throw new TestManagerException();
            }

            this.tests = testReader.getTests(test_grid_tests_location, testConfig.getTestGitRepo());

        } catch (TestReaderException e) {
           log.error("Error while reading tests",e);
           throw new TestManagerException();
        }


    }

    public void executeTests(){
        
        if(this.tests!=null){
            for (Test test:this.tests){
                log.info("Executing "+test.getTestName()+" Test");
                for (String testUrl:test.getJmterScripts()){
                    System.out.println("executing "+testUrl);
                    this.testExecutor.execute(testUrl,this.deployment);
                }
                log.info("---------------------------------------");
            }
        }

    }

    private String getTestfolderName(String gitLink){
        String[] split = gitLink.split("/");
        return split[split.length-1];
    }
}
