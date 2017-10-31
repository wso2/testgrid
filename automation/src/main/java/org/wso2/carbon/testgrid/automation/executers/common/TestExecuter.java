package org.wso2.carbon.testgrid.automation.executers.common;

import org.wso2.carbon.testgrid.automation.TestGridExecuteException;
import org.wso2.carbon.testgrid.common.Deployment;

public interface TestExecuter {
    void execute(String script, Deployment deployment) throws TestGridExecuteException;

    void init(String testGridFolder,String testName) throws TestGridExecuteException;
}
