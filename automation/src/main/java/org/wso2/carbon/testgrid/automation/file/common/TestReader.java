package org.wso2.carbon.testgrid.automation.file.common;

import org.wso2.carbon.testgrid.automation.TestReaderException;
import org.wso2.carbon.testgrid.automation.beans.Test;

import java.util.List;

/**
 * Created by sameera on 31/10/17.
 */
public interface TestReader {


    List<Test> readTests(String testLocation) throws TestReaderException;
}
