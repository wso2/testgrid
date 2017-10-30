package org.wso2.carbon.testgrid.automation.file;

import org.wso2.carbon.testgrid.automation.TestReaderException;
import org.wso2.carbon.testgrid.automation.beans.Test;
import org.wso2.carbon.testgrid.automation.file.common.TestReader;

import java.util.List;

public class TestNGTestReader implements TestReader {
    @Override
    public List<Test> getTests(String testLocation, String gitLocation) throws TestReaderException {
        return null;
    }
}
