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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.tinkerer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.tinkerer.exception.TinkererOperationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Maintain details of the execution result of async command request.
 */
public class AsyncCommandResponse extends CommandResponse {

    private static final Logger logger = LoggerFactory.getLogger(TinkererSDK.class);

    private String filePath;
    private ScriptExecutorThread scriptExecutorThread;
    private BufferedReader bufferedReader;

    /**
     * Constructor to handle async output from Tinkerer
     *
     * @param filePath
     * @param scriptExecutorThread
     */
    AsyncCommandResponse(Path filePath, ScriptExecutorThread scriptExecutorThread) {
        this.filePath = filePath.toString();
        this.scriptExecutorThread = scriptExecutorThread;
        this.bufferedReader = null;
    }

    /**
     *  Start read stream from file
     *
     * @return  BufferedReader output
     * @throws TinkererOperationException  File reading error exceptions
     */
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    public BufferedReader startReadStream() throws TinkererOperationException {
        try {
            InputStream inputStream = new FileInputStream(new File(this.filePath));
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

                public String readLine() throws IOException {
                    String result;
                    do {
                        result = super.readLine();
                    } while (result == null && hasMoreContent());
                    return result;
                }
            };
        } catch (FileNotFoundException e) {
            throw new TinkererOperationException("File did not found " + this.filePath, e);
        }
        return this.bufferedReader;
    }

    /**
     * Check if more content available to read.
     *
     * @return
     */
    public boolean hasMoreContent() {
        return !scriptExecutorThread.isCompleted();
    }

    /**
     * Get exit value of the operation
     *
     * @return      The exit value of the operation
     */
    @Override
    public int getExitValue() {
        super.setExitValue(scriptExecutorThread.getExitValue());
        return super.getExitValue();
    }

    /**
     * Close buffer and remove persisted stream result from file.
     */
    public void endReadStream() {
        try {
            this.bufferedReader.close();

        } catch (IOException e) {
            logger.error("Error while closing buffer for " + this.filePath);
        }
        try {
            Files.delete(Paths.get(this.filePath));
        } catch (IOException e) {
            logger.error("Error while removing file " + this.filePath);
        }

    }

    /**
     * Get file path of persisted logs
     *
     * @return  The file path
     */
    public String getFilePath() {
        return filePath;
    }
}
