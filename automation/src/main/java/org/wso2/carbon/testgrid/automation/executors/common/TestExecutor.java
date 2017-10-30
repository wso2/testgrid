package org.wso2.carbon.testgrid.automation.executors.common;

import org.wso2.carbon.testgrid.common.Deployment;

public interface TestExecutor {
    void execute(String script, Deployment deployment);
}
