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

import org.wso2.testgrid.common.exception.TestGridException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

/**
 * Utility class for handling file operations.
 *
 * @since 1.0.0
 */
public class FileUtil {

    public static final String YAML_EXTENSION = ".yaml";

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
     * @param directory the directory in which the files should be look-up
     * @param glob the glob pattern to be filtered
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
     * @param files list of files to be archive
     * @param destination path of the archive file
     */
    public static void compressFiles(List<String> files, String destination) throws TestGridException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        URI uri = URI.create("jar:file:" + destination);

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(uri, env)) {
            for (String filePath: files) {
                File file = new File(filePath);
                Path destinationPath =  zipFileSystem.getPath(file.getName());
                Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new TestGridException("Error occurred while making the archive: " + destination , e);
        }
    }

}
