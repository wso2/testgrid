package org.wso2.carbon.testgrid.automation.executers.common;


import org.wso2.carbon.testgrid.automation.executers.JmeterExecuter;
import org.wso2.carbon.testgrid.automation.executers.TestNgExecuter;
import org.wso2.carbon.testgrid.automation.executers.common.TestExecuter;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;

public class TestExecuterFactory {

    public static TestExecuter getTestExecutor(String testTypeJmeter) {
        switch (testTypeJmeter) {
            case TestGridConstants.TEST_TYPE_JMETER:
                return new JmeterExecuter();
            case TestGridConstants.TEST_TYPE_TESTNG:
                return new TestNgExecuter();
            default:
                return null;

        }
    }
}
