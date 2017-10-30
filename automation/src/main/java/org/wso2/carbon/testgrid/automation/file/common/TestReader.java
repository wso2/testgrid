package org.wso2.carbon.testgrid.automation.file.common;

import org.wso2.carbon.testgrid.automation.TestReaderException;
import org.wso2.carbon.testgrid.automation.beans.Test;

import java.util.List;

public interface TestReader {

     List<Test> getTests(String testLocation,String gitLocation) throws TestReaderException;

}
