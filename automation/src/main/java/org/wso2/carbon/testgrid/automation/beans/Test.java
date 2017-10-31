package org.wso2.carbon.testgrid.automation.beans;

import org.wso2.carbon.testgrid.automation.TestGridExecuteException;
import org.wso2.carbon.testgrid.common.Deployment;

import java.util.List;

public abstract class Test {

    private String testName;
    private List<String> jmterScripts;


    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public List<String> getJmterScripts() {
        return jmterScripts;
    }

    public void setJmterScripts(List<String> jmterScripts) {
        this.jmterScripts = jmterScripts;
    }

    public abstract void execute(String testLocation, Deployment deployment) throws TestGridExecuteException;
}
