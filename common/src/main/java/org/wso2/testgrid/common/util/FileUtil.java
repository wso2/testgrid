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
import org.wso2.testgrid.common.exception.TestGridException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
     * @param append   true if append, false if overwrite
     * @throws TestGridException thrown when error on persisting file
     */
    public static void saveFile(String content, String filePath, String fileName, boolean append)
            throws TestGridException {
        String fileAbsolutePath = Paths.get(filePath, fileName).toAbsolutePath().toString();
        saveFile(content, fileAbsolutePath, append);
    }

    /**
     * Saves the content to a file with the given file path.
     *
     * @param content       The content to save into file
     * @param filePath      Whole file path to write
     * @param append        true if append, false if overwrite
     * @throws TestGridException    thrown when error on persisting file
     */
    public static void saveFile(String content, String filePath, boolean append) throws TestGridException {
        try (OutputStream outputStream = new FileOutputStream(filePath, append);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            outputStreamWriter.write(content);
        } catch (IOException e) {
            throw new TestGridException(StringUtil.concatStrings("Error while writing data to a file ",
                    filePath), e);
        }
    }

    /**
     * Read content from a given file
     *
     * @param filePath          The file path to write
     * @param fileName          name of the file
     * @return                  File content
     * @throws TestGridException    Throw when file reading error
     */
    public static String readFile(String filePath, String fileName) throws TestGridException {
        StringBuilder out = new StringBuilder();
        try {
            String fileAbsolutePath = Paths.get(filePath, fileName).toAbsolutePath().toString();
            try (InputStream fis = new FileInputStream(new File(fileAbsolutePath));
                InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                int data = reader.read();
                while (data != -1) {
                    out.append((char) data);
                    data = reader.read();
                }
            }
        } catch (IOException e) {
            throw new TestGridException(StringUtil.concatStrings("Error in writing file ", fileName), e);
        }
        return out.toString();
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

}
