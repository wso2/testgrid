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
package org.wso2.testgrid.web.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * This class is responsible for watching changes of a given file.
 *
 * @since 1.0.0
 */
public abstract class FileWatcher implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);
    private final Path folderPath;
    private final String watchFile;

    /**
     * Creates an instance of {@link FileWatcher} to watch the given file.
     *
     * @param watchFile file to be watched
     * @throws FileWatcherException thrown when error on creating an instance of {@link FileWatcher}
     */
    public FileWatcher(Path watchFile) throws FileWatcherException {
        // Do not allow this to be a folder since we want to watch files
        if (!Files.isRegularFile(watchFile)) {
            throw new FileWatcherException(StringUtil.concatStrings(watchFile, " is not a regular file."));
        }

        // This is always a folder
        this.folderPath = watchFile.getParent();
        if (this.folderPath == null) {
            throw new FileWatcherException("The path provided do not have a parent. Please provide to complete " +
                                           "path to the file.");
        }

        // Keep this relative to the watched folder
        Path watchFileName = watchFile.getFileName();
        if (watchFileName == null) {
            throw new FileWatcherException("The path has 0 (zero) elements. Please provide a valid file path.");
        }
        this.watchFile = watchFileName.toString();
    }

    /**
     * This method will execute before watching the file for changes. The contents of the file will be passed to the
     * method.
     *
     * @param fileContents contents of the file to be watched
     * @throws FileWatcherException thrown when error on executing before file watch method
     */
    public abstract void beforeFileWatch(String fileContents) throws FileWatcherException;

    /**
     * This method will be called when the file creation is detected. Implement this method to execute the
     * necessary logic when the file is created. The contents of the file created is passed to the method.
     *
     * @param fileContents contents of the file created
     * @throws FileWatcherException thrown when error on executing creation method
     */
    public abstract void onCreate(String fileContents) throws FileWatcherException;

    /**
     * This method will be called when the file modification is detected. Implement this method to execute the
     * necessary logic when the file is modified. The contents of the file created is passed to the method.
     *
     * @param fileContents contents of the file modified
     * @throws FileWatcherException thrown when error on executing modification method
     */
    public abstract void onModified(String fileContents) throws FileWatcherException;

    /**
     * This method will be called when the file deletion is detected. Implement this method to execute the
     * necessary logic when the file is deleted.
     */
    public abstract void onDelete();

    @Override
    public void run() {
        try (WatchService service = folderPath.getFileSystem().newWatchService()) {
            Path watchFilePath = folderPath.resolve(watchFile);

            // Execute before file watch
            beforeFileWatch(readFile(watchFilePath));

            // Watch for modification events
            folderPath.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            // Start the infinite polling loop
            while (true) {
                // Wait for the next event
                WatchKey watchKey = service.take();

                for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = watchEvent.kind();

                    @SuppressWarnings("unchecked")
                    Path watchEventPath = ((WatchEvent<Path>) watchEvent).context();
                    // Call this if the right file is involved
                    if (watchEventPath.toString().equals(watchFile)) {
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            onCreate(readFile(watchFilePath));
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            onModified(readFile(watchFilePath));
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            onDelete();
                        }
                    }
                }

                // Exit if no longer valid
                if (!watchKey.reset()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            logger.error(StringUtil
                    .concatStrings("Error on waiting for changes in file ", watchFile), e);
        } catch (IOException e) {
            logger.error(StringUtil
                    .concatStrings("Error on registering file watch service for file ", watchFile), e);
        } catch (FileWatcherException e) {
            logger.error(StringUtil.concatStrings("Error on reading the file contents of file ", watchFile), e);
        }
    }

    /**
     * Returns the contents of the given file.
     *
     * @param watchFilePath watch file path
     * @return content of the watch file
     * @throws FileWatcherException thrown when error on reading file content
     */
    private String readFile(Path watchFilePath) throws FileWatcherException {
        try {
            byte[] encoded = Files.readAllBytes(watchFilePath);
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileWatcherException(StringUtil
                    .concatStrings("Error on reading file content of file ", watchFilePath), e);
        }
    }
}
