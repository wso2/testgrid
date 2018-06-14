/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.testgrid.automation.parser;

import org.wso2.testgrid.automation.exception.JTLResultParserException;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;

/**
 * This class holds the util methods required for JMeter result parsers.
 *
 * @since 1.0.0
 */
public class ResultParserUtil {

    static final String JTL_EXTENSION = ".jtl";

    /**
     * This method will return the location of the JMeter scenario results file.
     *
     * @param testLocation - Location of the scenario test
     * @return a String {@link String} JTL file location
     * @throws JTLResultParserException {@link JTLResultParserException} if the JTL file is not found in the
     *                                  location
     */
    public static String getJTLFile(String testLocation) throws JTLResultParserException {
        File dir = new File(testLocation);
        FilenameFilter filter = (dir1, name) -> name.endsWith(JTL_EXTENSION);
        String[] files = dir.list(filter);
        if (files == null || files.length == 0) {
            throw new JTLResultParserException("Unable to locate the results jtl file in the directory : '" +
                    testLocation + "'");
        } else if (files.length > 0) {
            return Paths.get(testLocation, files[0]).toString();
        }
        return null;
    }
}

