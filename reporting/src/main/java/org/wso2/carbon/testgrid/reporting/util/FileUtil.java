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
package org.wso2.carbon.testgrid.reporting.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.reporting.ReportingException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * This class is responsible for handling file operations.
 *
 * @since 1.0.0
 */
public class FileUtil {

    private static Log logger = LogFactory.getLog(FileUtil.class);

    /**
     * Writes a given string to a given file to persistent media.
     *
     * @param filePath absolute path of the file to be written
     * @param string   string to be written
     * @throws ReportingException thrown on {@link FileNotFoundException} or {@link UnsupportedEncodingException}
     */
    public static void writeToFile(String filePath, String string) throws ReportingException {
        createFileIfNotExists(filePath); // Create file if not exists
        try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8.name())) {
            writer.write(string);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new ReportingException(String.format(Locale.ENGLISH, "File %s not found", filePath), e);
        } catch (UnsupportedEncodingException e) {
            throw new ReportingException(
                    String.format(Locale.ENGLISH, "Unsupported encoding %s", StandardCharsets.UTF_8.name()), e);
        }
    }

    /**
     * Returns the file list of a given path.
     * <p>
     * If the given path denotes a directory, then the file list in the directory is returned, else the file it self
     * as an array will ne returned
     *
     * @param path path to obtain the file list
     * @return list of files or the file of the given path
     */
    public static File[] getFileList(Path path) {
        File file = new File(path.toAbsolutePath().toString());
        if (!file.isDirectory()) {
            return new File[]{file};
        }
        return file.listFiles();
    }

    /**
     * Creates a file with the given name.
     *
     * @param filePath absolute path of the file
     * @throws ReportingException thrown when IO exception on creating a file
     */
    private static void createFileIfNotExists(String filePath) throws ReportingException {
        File file = new File(filePath);
        if (!file.exists()) {
            // Create directories if not exists
            boolean isDirCreated = new File(Paths.get(filePath).getParent().toAbsolutePath().toString()).mkdirs();
            if (isDirCreated) {
                logger.info("Directory structure created.");
            }

            // Touch file
            try {
                new FileOutputStream(file).close();
            } catch (IOException e) {
                throw new ReportingException(String
                        .format(Locale.ENGLISH, "IO Exception occurred when creating file %s", file), e);
            }
        }
    }
}
