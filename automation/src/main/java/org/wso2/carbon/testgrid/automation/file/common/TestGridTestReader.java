package org.wso2.carbon.testgrid.automation.file.common;

import org.wso2.carbon.testgrid.automation.TestReaderException;
import org.wso2.carbon.testgrid.automation.beans.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestGridTestReader {

     public List<Test> getTests(String testLocation) throws TestReaderException{
         File tests = new File(testLocation);
         List<Test> testList = new ArrayList<>();
         for(String testType: Arrays.asList(tests.list())){
             TestReader testReader = TestReaderFactory.getTestReader(testType.toUpperCase());
             List<Test> tests1;
             if (testReader != null) {
                 tests1 = testReader.readTests(testLocation + File.separator + testType);
                 testList.addAll(tests1);
             }

         }

         return testList;
     }

}
