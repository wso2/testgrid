package org.wso2.carbon.testgrid.automation.file.common;

import org.wso2.carbon.testgrid.automation.file.JmeterTestReader;
import org.wso2.carbon.testgrid.automation.file.TestNGTestReader;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;

public class TestReaderFactory {


    public static TestReader getTestReader(String testType) {
        switch (testType) {
            case TestGridConstants.TEST_TYPE_JMETER:
                return new JmeterTestReader();

            case TestGridConstants.TEST_TYPE_TESTNG:
                return new TestNGTestReader();

            default:
                return null;
        }
    }
}
