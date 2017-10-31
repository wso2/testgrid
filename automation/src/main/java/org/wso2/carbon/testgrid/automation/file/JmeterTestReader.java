/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.testgrid.automation.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.beans.JmeterTest;
import org.wso2.carbon.testgrid.automation.beans.Test;
import org.wso2.carbon.testgrid.automation.exceptions.TestReaderException;
import org.wso2.carbon.testgrid.automation.file.common.TestReader;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JmeterTestReader implements TestReader {

    private static final Log log = LogFactory.getLog(JmeterTestReader.class);
    private final String JMETER_TEST_PATH = "src" + File.separator + "test" + File.separator + "jmeter";


    private List<Test> processTestStructure(File file) {
        List<Test> testsList = new ArrayList<>();
        String[] list = file.list();
        for (String solution : Arrays.asList(list)) {
            File tests = new File(file.getAbsolutePath() + File.separator + solution + File.separator + JMETER_TEST_PATH);
            JmeterTest test = new JmeterTest();

            test.setTestName(solution);
            List<String> jmxList = new ArrayList<>();
            if (tests.exists()) {
                for (String jmx : Arrays.asList(tests.list())) {
                    if (jmx.endsWith(TestGridConstants.JMTER_SUFFIX)) {
                        jmxList.add(tests.getAbsolutePath() + File.separator + jmx);
                    }
                }
            }
            Collections.sort(jmxList);
            test.setJmterScripts(jmxList);
            testsList.add(test);

        }
        return testsList;
    }


    @Override
    public List<Test> readTests(String testLocation) throws TestReaderException {
        return processTestStructure(new File(testLocation));
    }
}
