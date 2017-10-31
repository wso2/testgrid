package org.wso2.carbon.testgrid.automation.file;

import org.wso2.carbon.testgrid.automation.TestReaderException;
import org.wso2.carbon.testgrid.automation.beans.Test;
import org.wso2.carbon.testgrid.automation.file.common.TestGridTestReader;
import org.wso2.carbon.testgrid.automation.file.common.TestReader;

import java.util.List;

public class TestNGTestReader implements TestReader {
    @Override
    public List<Test> readTests(String testLocation) throws TestReaderException {
        return null;
    }
}
