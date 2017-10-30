package org.wso2.carbon.testgrid.automation.executors;


import org.wso2.carbon.testgrid.automation.executors.common.TestExecutor;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;

public class TestExecutorFactory {

    public static TestExecutor getTestExecutor(TestConfiguration testConfig){
        switch (testConfig.getTestType()){
            case "JMETER": return new JmeterExecutor();
            case "TESTNG":return new TestNgExecutor();
            default:return null;

        }
    }
}
