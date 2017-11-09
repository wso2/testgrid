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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.wso2.carbon.testgrid.reporting.ReportingException;
import org.wso2.carbon.testgrid.reporting.result.TestResultable;
import org.wso2.carbon.testgrid.reporting.util.ReflectionUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class is responsible for reading CSV files and producing an output based on a given model.
 *
 * @since 1.0.0
 */
public class CSVResultReader implements ResultReadable {

    private static final String SEPARATOR = ",";

    @Override
    public <T extends TestResultable> List<T> readFile(Path path, Class<T> type) throws ReportingException {

        if (path == null) {
            throw new ReportingException("File path is null.");
        }

        if (type == null) {
            throw new ReportingException("Type cannot be null.");
        }

        String filePath = path.toString();
        List<T> results = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String titles[] = bufferedReader.readLine().split(SEPARATOR);

            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(titles);
            CSVParser csvFileParser = new CSVParser(bufferedReader, csvFileFormat);

            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            // Read the CSV file records starting from the second record to skip the header
            for (CSVRecord record : csvRecords) {
                T result = ReflectionUtil.createInstanceFromClass(type);
                // Set field values
                for (String title : titles) {
                    ReflectionUtil.setFieldValue(result, title, record.get(title));
                }
                results.add(result);
            }
        } catch (FileNotFoundException e) {
            throw new ReportingException(String.format(Locale.ENGLISH, "File %s cannot be found", filePath), e);
        } catch (IOException e) {
            throw new ReportingException(String
                    .format(Locale.ENGLISH, "IO error occurred when reading file %s", filePath), e);
        }
        return results;
    }
}
