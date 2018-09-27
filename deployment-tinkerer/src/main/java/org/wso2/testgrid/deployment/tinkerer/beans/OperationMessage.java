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

package org.wso2.testgrid.deployment.tinkerer.beans;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.agentoperation.OperationRequest;
import org.wso2.testgrid.common.agentoperation.OperationSegment;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.deployment.tinkerer.utils.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Hold list of operation result which were executed on agent.
 */
public class OperationMessage {

    private static final Logger logger = LoggerFactory.getLogger(OperationMessage.class);
    public static final String PERSISTED_FILE_PATH = System.getProperty("java.io.tmpdir");

    private String operationId;
    private String agentId;
    private OperationRequest.OperationCode code;
    private volatile Queue<String> messageQueue;
    private volatile boolean completed;
    private volatile int exitValue;
    private volatile double contentLength;
    private volatile long createdTime;
    private volatile long lastUpdatedTime;
    private volatile long lastConsumedTime;
    private volatile boolean persisted;

    /**
     * Create new operation queue and initialize with operation id and code.
     *
     * @param operationId   The operation id
     * @param code          Type of the operation
     * @param agentId       The id of the agent operation executing on
     */
    public OperationMessage(String operationId, OperationRequest.OperationCode code, String agentId) {
        this.operationId = operationId;
        this.agentId = agentId;
        this.messageQueue = new LinkedList<>();
        this.code = code;
        this.createdTime = Calendar.getInstance().getTimeInMillis();
        this.lastUpdatedTime = Calendar.getInstance().getTimeInMillis();
        this.lastConsumedTime = Calendar.getInstance().getTimeInMillis();
        this.contentLength = 0;
        this.persisted = false;
        this.completed = false;
        this.exitValue = 0;
    }

    /**
     * Add new message to the message queue.
     *
     * @param operationSegment  operation segment to add
     */
    public synchronized void addMessage(OperationSegment operationSegment) {
        if (operationSegment.getCompleted()) {
            setOperationAsCompleted(operationSegment.getExitValue());
        }
        if (this.persisted) {
            persistMessage(operationSegment.getResponse());
        } else {
            this.messageQueue.add(operationSegment.getResponse());
            this.contentLength += operationSegment.getResponse().length();
        }
        // Persist message queue into a file if it overflow
        if (this.contentLength > Constants.MAX_QUEUE_CONTENT_LENGTH) {
            logger.info("Message overflow for operation " + this.operationId +
                    " Start persist message queue into a file");
            persistOperationQueue();
        }
        this.lastUpdatedTime = Calendar.getInstance().getTimeInMillis();;
    }

    /**
     * Get operation id of the current message queue
     *
     * @return      The operation id
     */
    public synchronized String getOperationId() {
        return operationId;
    }

    /**
     * Set operation id for message queue
     *
     * @param operationId   The operation queue
     */
    public synchronized void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get message queue as queue
     *
     * @return  The message queue
     */
    public synchronized Queue<String> getMessageQueue() {
        if (this.persisted) {
            recoverOperationQueue();
        }
        return messageQueue;
    }

    /**
     * Set message queue
     *
     * @param messageQueue  The message queue
     */
    public synchronized void setMessageQueue(Queue<String> messageQueue) {
        if (this.persisted) {
            removePersistedFile();
        }
        this.messageQueue = messageQueue;
        this.contentLength = calculateContentLength();
        this.lastUpdatedTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Reset message queue with empty message queue
     *
     */
    public synchronized void resetMessageQueue() {
        if (this.persisted) {
            removePersistedFile();
        }
        this.messageQueue = new LinkedList<>();
        this.contentLength = 0;
        this.lastConsumedTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Get created time of the message queue
     *
     * @return  Created time in milliseconds
     */
    public long getCreatedTime() {
        return this.createdTime;
    }

    /**
     * Get last updated time of the message queue
     *
     * @return  Last updated time in milliseconds
     */
    public synchronized long getLastUpdatedTime() {
        return this.lastUpdatedTime;
    }

    /**
     * Get when last message was dequeue
     * @return      Last consumed time in millisecond
     */
    public synchronized long getLastConsumedTime() {
        return lastConsumedTime;
    }

    /**
     * Update the last consume time to current time
     */
    public synchronized void updateLastConsumedTime() {
        this.lastConsumedTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Set the type of the operation
     *
     * @param code      Operation type
     */
    public void setCode(OperationRequest.OperationCode code) {
        this.code = code;
    }

    /**
     * Get the type of the operation
     *
     * @return  Type of the operation
     */
    public OperationRequest.OperationCode getCode() {
        return code;
    }

    /**
     * Get the length of the all messages in message queue
     *
     * @return      Length of the message queue
     */
    public synchronized double getContentLength() {
        return contentLength;
    }

    /**
     * Get the length of the shell execution result string
     *
     * @return      Content length
     */
    private synchronized double calculateContentLength() {
        double sizeOfContent = 0;
        for (String operationSegment : this.messageQueue) {
            sizeOfContent += operationSegment.length();
        }

        return sizeOfContent;
    }

    /**
     * Persist message queue into a file
     *
     * @return      true if success, else false
     */
    public synchronized boolean persistOperationQueue() {
        String dataToWrite = "";
        for (String dataSegment : messageQueue) {
            dataToWrite = dataToWrite.concat(dataSegment);
        }
        return persistMessage(dataToWrite);
    }

    /**
     * Print string into a file
     *
     * @param message       The message to write into a file
     * @return              true if success, else false
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private synchronized boolean persistMessage(String message) {
        try {
            FileUtil.saveFile(message, PERSISTED_FILE_PATH, this.operationId.concat(".txt"), true);
            this.messageQueue = new LinkedList<>();
            this.lastUpdatedTime = Calendar.getInstance().getTimeInMillis();;
            this.contentLength = 0;
            this.persisted = true;
        } catch (TestGridException e) {
            logger.error("Unable persist data into a file for operation id " + this.operationId, e);
            return false;
        }
        return true;
    }

    /**
     * Read back persisted data from a file into the message queue
     *
     * @return      true if success, else false
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private synchronized boolean recoverOperationQueue() {
        try {
            String persistedResult = FileUtil.readFile(PERSISTED_FILE_PATH, this.operationId.concat(".txt"));
            double contentLength = persistedResult.length();
            this.messageQueue.add(persistedResult);
            this.persisted = false;
            this.contentLength = contentLength;
        } catch (TestGridException e) {
            logger.error("Unable read data from a file for operation id " + this.operationId, e);
            return false;
        }
        return removePersistedFile();
    }

    /**
     * Remove persisted in file.
     *
     * @return      true if success, else false
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    public synchronized boolean removePersistedFile() {
        Path filePath = Paths.get(PERSISTED_FILE_PATH, this.operationId.concat(".txt"));
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            logger.warn("Error while removing file " + filePath.toString());
            return false;
        }
        return true;
    }

    /**
     * Check if operation execution completed
     *
     * @return      Completed state
     */
    public synchronized boolean isCompleted() {
        return completed;
    }

    /**
     * Get exit value of the operation execution
     *
     * @return      The exit value
     */
    public synchronized int getExitValue() {
        return exitValue;
    }

    /**
     * Get agent id where operation is executing
     *
     * @return      The agent id
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Set agent id where operation is executing
     *
     * @param agentId       The agent id
     */
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * Set current operation as completed.
     *
     * @param exitValue     The exit value
     */
    public synchronized void setOperationAsCompleted(int exitValue) {
        this.completed = true;
        this.exitValue = exitValue;
    }
}
