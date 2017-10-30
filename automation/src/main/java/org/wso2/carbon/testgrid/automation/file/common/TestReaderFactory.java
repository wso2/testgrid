package org.wso2.carbon.testgrid.automation.file.common;


import org.wso2.carbon.testgrid.automation.file.JmeterTestReader;
import org.wso2.carbon.testgrid.automation.file.TestNGTestReader;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;

public class TestReaderFactory {

    public static TestReader getTestReader(TestConfiguration config){

        switch (config.getTestType()){
            case "JMETER":return new JmeterTestReader();

            case "TESTNG":return new TestNGTestReader();

            default:return null;
        }
    }
}
