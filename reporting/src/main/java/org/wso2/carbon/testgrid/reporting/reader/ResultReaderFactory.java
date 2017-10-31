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

import java.nio.file.Path;

/**
 * Factory class to return an instance of a result reader based on the file extension.
 *
 * @since 1.0.0
 */
public class ResultReaderFactory {

    private static final String CSV_EXTENSION = ".csv";

    public static ResultReader getResultReader(Path path) throws ReportingException {
        // Check whether configuration filepath is null. proceed if not null.
        if (path == null || !path.toFile().exists()) {
            throw new ReportingException("No result file path is provided");
        }

        // Initialize result reader based on the file's extension.
        if (path.toString().endsWith(CSV_EXTENSION)) {
            return new CSVResultReader();
        } else {
            throw new ReportingException("Error while initializing result reader, file extension is not supported");
        }
    }
}
