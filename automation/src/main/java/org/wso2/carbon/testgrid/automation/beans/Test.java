package org.wso2.carbon.testgrid.automation.beans;

import java.util.List;

public class Test {

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
}
