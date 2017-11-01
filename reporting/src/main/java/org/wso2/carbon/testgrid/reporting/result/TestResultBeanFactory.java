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
package org.wso2.carbon.testgrid.reporting.result;

import org.wso2.carbon.testgrid.reporting.ReportingException;

import java.nio.file.Path;

/**
 * Factory class for returning the result type based on the test type.
 *
 * @since 1.0.0
 */
public class TestResultBeanFactory {

    private static final String JMETER_TEST_DIR = "Jmeter";

    /**
     * Returns the result type based on the test type.
     *
     * @param directoryPath directory of the test outputs
     * @param <T>           result type
     * @return type of the test result
     * @throws ReportingException thrown when result type cannot be identified
     */
    @SuppressWarnings("unchecked")
    public static <T extends TestResultable> Class<T> getResultType(Path directoryPath)
            throws ReportingException {

        // Check whether configuration directoryPath is null. proceed if not null.
        if (directoryPath == null || !directoryPath.toFile().exists()) {
            throw new ReportingException("Directory cannot be located");
        }

        if (directoryPath.toString().endsWith(JMETER_TEST_DIR)) {
            return (Class<T>) JmeterTestResult.class;
        } else {
            throw new ReportingException("Cannot identify test type based on the directory name.");
        }
    }
}
