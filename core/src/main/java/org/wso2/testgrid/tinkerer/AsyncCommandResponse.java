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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.tinkerer.exception.TinkererOperationException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Maintain details of the execution result of async command request.
 */
public class AsyncCommandResponse extends CommandResponse {

    private static final Logger logger = LoggerFactory.getLogger(AsyncCommandResponse.class);

    private String filePath;
    private ScriptExecutorThread scriptExecutorThread;
    private String operationId;
    private int contentCount = 0;

    /**
     * Constructor to handle async output from Tinkerer
     *
     * @param operationId   The operation id
     * @param filePath          The file path to persist data
     * @param scriptExecutorThread      Streaming thread pointer
     */
    AsyncCommandResponse(String operationId, Path filePath, ScriptExecutorThread scriptExecutorThread) {
        this.operationId = operationId;
        this.filePath = filePath.toString();
        this.scriptExecutorThread = scriptExecutorThread;
    }

    /**
     * Check if more content available to read.
     *
     * @return          has more content
     */
    public boolean hasMoreContent() {
        if (isCompleted() && (this.contentCount == this.scriptExecutorThread.getSegmentCount())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Read next section of result log from the file
     *
     * @return      Result segment
     */
    public String readLines() {
        String fileName = StringUtil.concatStrings(this.operationId, "_",
                Integer.toString(this.contentCount + 1), ".txt");
        String fileToRead = Paths.get(this.filePath, fileName).toString();
        String result = "";
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("Error while waiting to read logs", e);
            }
            if (FileUtil.isFileExist(fileToRead)) {
                break;
            }
            if (!hasMoreContent()) {
                return "";
            }
        }
        try {
            result = FileUtil.readFile(filePath, fileName);
        } catch (TestGridException e) {
            logger.info("Error while reading file", e);
        }
        this.contentCount++;
        return result;
    }


    /**
     * Check if execution completed
     *
     * @return  is operation running completed
     */
    public boolean isCompleted() {
        return scriptExecutorThread.isCompleted();
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
     * Remove persisted stream result from file.
     */
    public void endReadStream() throws TinkererOperationException {
        for (int fileCount = 1; fileCount <= this.contentCount; fileCount++) {
            String fileName = StringUtil.concatStrings(this.operationId, "_",
                    Integer.toString(fileCount), ".txt");
            String fileToDelete = Paths.get(this.filePath, fileName).toString();
            try {
                FileUtil.removeFile(fileToDelete);
            } catch (TestGridException e) {
                logger.error("Error while deleting file ", e);
            }
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
