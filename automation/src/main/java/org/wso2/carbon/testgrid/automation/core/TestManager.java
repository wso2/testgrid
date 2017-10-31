package org.wso2.carbon.testgrid.automation.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.TestManagerException;
import org.wso2.carbon.testgrid.automation.TestReaderException;
import org.wso2.carbon.testgrid.automation.beans.Test;
import org.wso2.carbon.testgrid.automation.file.common.TestGridTestReader;
import org.wso2.carbon.testgrid.common.Deployment;
import java.io.File;
import java.util.List;

public class TestManager {

    private static final Log log = LogFactory.getLog(TestManager.class);
    private List<Test> tests;
    private Deployment deployment;
    private String testLocation;


    public void init(String testLocation, Deployment deployment) throws TestManagerException, TestGridExecuteException {

        this.deployment = deployment;
        TestGridTestReader testGridTestReader = new TestGridTestReader();

        try {

            this.testLocation= testLocation + File.separator + "Tests";
            this.tests = testGridTestReader.getTests(this.testLocation);

        } catch (TestReaderException e) {
           log.error("Error while reading tests",e);
           throw new TestManagerException();
        }


    }

    public void executeTests() throws TestGridExecuteException {
        
        if(this.tests!=null){
            for (Test test:this.tests){
                log.info("Executing "+test.getTestName()+" Test");
                test.execute(this.testLocation,this.deployment);
                log.info("---------------------------------------");
            }
        }

    }

    private String getTestfolderName(String gitLink){
        String[] split = gitLink.split("/");
        return split[split.length-1];
    }
}
