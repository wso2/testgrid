/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.testgrid.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.TestGridException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.wso2.testgrid.common.TestGridConstants.PRODUCT_TEST_PLANS_DIR;

/**
 * Utility class for handling file operations.
 *
 * @since 1.0.0
 */
public class FileUtil {

    public static final String YAML_EXTENSION = ".yaml";
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * Returns an instance of the specified type from the given configuration YAML.
     *
     * @param location location of the configuration YAML file
     * @return instance of the specified type from the given configuration YAML
     * @throws IOException thrown when no file is found in the given location or when error on closing file input stream
     */
    public static <T> T readYamlFile(String location, Class<T> type) throws IOException {
        if (StringUtil.isStringNullOrEmpty(location) || !location.endsWith(YAML_EXTENSION)) {
            throw new IllegalArgumentException(
                    StringUtil.concatStrings("Invalid configuration file: ", location));
        }

        try (FileInputStream fileInputStream = new FileInputStream(new File(location))) {
            return new Yaml().loadAs(fileInputStream, type);
        }
    }

    /**
     * This method returns the list of files in a given directory after filtering a given glob pattern.
     *
     * @param directory the directory in which the files should be look-up
     * @param glob      the glob pattern to be filtered
     * @return list of files
     * @throws TestGridException thrown when an error occurred while accessing files on directory.
     */
    public static List<String> getFilesOnDirectory(String directory, String glob) throws TestGridException {
        List<String> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory), glob)) {
            for (Path entry : stream) {
                files.add(entry.toString());
            }
        } catch (IOException e) {
            throw new TestGridException("Error occurred while getting files on directory + " + directory, e);
        }
        return files;
    }

    /**
     * Saves the content to a file with the given name in the given file path.
     *
     * @param content  content to write in the file
     * @param filePath location to save the file
     * @param fileName name of the file
     * @throws TestGridException thrown when error on persisting file
     */
    public static void saveFile(String content, String filePath, String fileName) throws TestGridException {
        String fileAbsolutePath = Paths.get(filePath, fileName).toAbsolutePath().toString();
        try (OutputStream outputStream = new FileOutputStream(fileAbsolutePath);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            outputStreamWriter.write(content);
        } catch (IOException e) {
            throw new TestGridException(StringUtil.concatStrings("Error in writing file ", fileName), e);
        }
    }

    /**
     * Creates a archive file of the list of files passing in the given destination.
     *
     * @param files       list of files to be archive
     * @param destination path of the archive file
     */
    public static void compressFiles(List<String> files, String destination) throws TestGridException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        URI uri = URI.create("jar:file:" + destination);

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(uri, env)) {
            for (String filePath : files) {
                File file = new File(filePath);
                Path destinationPath = zipFileSystem.getPath(file.getName());
                Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new TestGridException("Error occurred while making the archive: " + destination, e);
        }
    }

    /**
     * Compress the files at sourceDir into the destination zip file.
     *
     * @param sourceDir   the source dir that has contents to archive
     * @param destination the zip file location
     */
    public static void compress(String sourceDir, String destination) throws IOException {
        Path destPath = Files.createFile(Paths.get(destination));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(destPath))) {
            Path sourcePath = Paths.get(sourceDir);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            if (Files.isSameFile(path, destPath)) {
                                return;
                            }
                            ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            logger.debug(e.getMessage(), e);
                            //ignore the exception and continue
                        }
                    });
        }
    }

    /**
     * Writes a given string to a given file to persistent media.
     *
     * @param filePath absolute path of the file to be written
     * @param string   string to be written
     * @throws TestGridException thrown on {@link FileNotFoundException} or {@link UnsupportedEncodingException}
     */
    public static void writeToFile(String filePath, String string) throws TestGridException {
        createFileIfNotExists(filePath); // Create file if not exists
        try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8.name())) {
            writer.write(string);
        } catch (FileNotFoundException e) {
            throw new TestGridException(String.format(Locale.ENGLISH, "File %s not found", filePath), e);
        } catch (UnsupportedEncodingException e) {
            throw new TestGridException(
                    String.format(Locale.ENGLISH, "Unsupported encoding %s", StandardCharsets.UTF_8.name()), e);
        }
    }

    /**
     * Creates a file with the given name.
     *
     * @param filePath absolute path of the file
     * @throws TestGridException thrown when IO exception on creating a file
     */
    private static void createFileIfNotExists(String filePath) throws TestGridException {
        File file = new File(filePath);
        if (!file.exists()) {
            // Create directories if not exists
            Path parent = Paths.get(filePath).getParent();

            if (parent != null) {
                boolean status = new File(parent.toAbsolutePath().toString()).mkdirs();

                if (status) {
                    // Touch file
                    try {
                        new FileOutputStream(file).close();
                    } catch (IOException e) {
                        throw new TestGridException(String.format(Locale.ENGLISH,
                                "IO Exception occurred when creating file %s", file), e);
                    }
                }
            }
        }
    }

    /**
     * Get test plan ids by reading testgrid yaml files contains in the testgrid home.
     *
     * @param workspace path of the current workspace
     * @throws TestGridException thrown when IO exception on reading testgrid yaml files.
     */
    public static List<String> getTestPlanIdByReadingTGYaml(String workspace) throws TestGridException {
        List<String> testPlanIds = new ArrayList<>();
        Path source = Paths.get(workspace, PRODUCT_TEST_PLANS_DIR);
        if (!Files.exists(source)) {
            logger.error("Test-plans dir does not exist: " + source);
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(source).filter(Files::isRegularFile)) {
            List<Path> paths = stream.sorted().collect(Collectors.toList());
            if (paths.isEmpty()) {
                logger.warn("No test plans were found at " + source);
            } else {
                logger.info("Test plans found at " + paths);
            }
            for (Path path : paths) {
                if (!path.toFile().exists()) {
                    throw new IOException(
                            "Test Plan File doesn't exist. File path is " + path.toAbsolutePath().toString());
                }
                TestPlan testPlanYaml = org.wso2.testgrid.common.util.FileUtil
                        .readYamlFile(path.toAbsolutePath().toString(), TestPlan.class);
                testPlanIds.add(testPlanYaml.getId());
            }
            return testPlanIds;
        } catch (IOException e) {
            throw new TestGridException("Error occurred while reading the test-plan yamls in workspace " + workspace,
                    e);
        }

    }
}
