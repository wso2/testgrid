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
package org.wso2.carbon.testgrid.reporting.reader;

import org.wso2.carbon.testgrid.reporting.ReportingException;
import org.wso2.carbon.testgrid.reporting.result.TestResultable;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for reading a generated result set from a test suite.
 *
 * @since 1.0.0
 */
public interface ResultReadable {

    /**
     * Reads the given result file and produces a result set based on the given type.
     *
     * @param path path of the result file
     * @param type type of the result
     * @param <T>  type of the result
     * @return returns
     * @throws ReportingException thrown when an error occurs in reading the results.Results.Tests.results file
     */
    <T extends TestResultable> List<T> readFile(Path path, Class<T> type) throws ReportingException;
}
