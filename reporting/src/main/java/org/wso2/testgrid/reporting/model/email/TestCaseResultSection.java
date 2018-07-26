package org.wso2.testgrid.reporting.model.email;

import org.wso2.testgrid.common.InfraCombination;

import java.util.List;

/**
 * Model class representing the test-plan result section in summarized email-report.
 */
public class TestCaseResultSection {

    private String testCaseName;
    private String testCaseDescription;
    private String testCaseExecutedOS;
    private String testCaseExecutedJDK;
    private String testCaseExecutedDB;
    int rowSpan = -1;

    List<InfraCombination> infraCombinations;

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getTestCaseDescription() {
        return testCaseDescription;
    }

    public void setTestCaseDescription(String testCaseDescription) {
        this.testCaseDescription = testCaseDescription;
    }

    public String getTestCaseExecutedOS() {
        return testCaseExecutedOS;
    }

    public void setTestCaseExecutedOS(String testCaseExecutedOS) {
        this.testCaseExecutedOS = testCaseExecutedOS;
    }

    public String getTestCaseExecutedJDK() {
        return testCaseExecutedJDK;
    }

    public void setTestCaseExecutedJDK(String testCaseExecutedJDK) {
        this.testCaseExecutedJDK = testCaseExecutedJDK;
    }

    public String getTestCaseExecutedDB() {
        return testCaseExecutedDB;
    }

    public void setTestCaseExecutedDB(String testCaseExecutedDB) {
        this.testCaseExecutedDB = testCaseExecutedDB;
    }

    public List<InfraCombination> getInfraCombinations() {
        return infraCombinations;
    }

    public void setInfraCombinations(List<InfraCombination> infraCombinations) {
        this.infraCombinations = infraCombinations;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }
}
