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
package org.wso2.carbon.testgrid.reporting.beans;

/**
 * Sample result model to capture jmeter test result.
 *
 * @since 1.0.0
 */
public class Result {

    private String timeStamp;
    private String elapsed;
    private String label;
    private String responseCode;
    private String responseMessage;
    private String threadName;
    private String dataType;
    private String success;
    private String failureMessage;
    private String bytes;
    private String sentBytes;
    private String grpThreads;
    private String allThreads;
    private String latency;
    private String idleTime;
    private String connect;

    /**
     * Returns the time stamp of the result.
     *
     * @return time stamp of the result
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the time stamp of the result.
     *
     * @param timeStamp time stamp of the result
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Returns the elapsed time of the result.
     *
     * @return elapsed time of the result
     */
    public String getElapsed() {
        return elapsed;
    }

    /**
     * Sets the elapsed time of the result.
     *
     * @param elapsed elapsed time of the result
     */
    public void setElapsed(String elapsed) {
        this.elapsed = elapsed;
    }

    /**
     * Returns the label of the result.
     *
     * @return label of the result
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of the result.
     *
     * @param label label of the result
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the response code of the result.
     *
     * @return response code of the result
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * Sets the response code of the result.
     *
     * @param responseCode response code of the result
     */
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Returns the response message of the result.
     *
     * @return response message of the result
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Sets the response message of the result.
     *
     * @param responseMessage response message of the result
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Returns the thread name of the result.
     *
     * @return thread name of the result
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Sets the thread name of the result.
     *
     * @param threadName thread name of the result
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * Returns the data type of the result.
     *
     * @return data type of the result
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type of the result.
     *
     * @param dataType data type of the result
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Returns the success of the result.
     *
     * @return success of the result
     */
    public String getSuccess() {
        return success;
    }

    /**
     * Sets the success of the result.
     *
     * @param success success of the result
     */
    public void setSuccess(String success) {
        this.success = success;
    }

    /**
     * Returns the failure message of the result.
     *
     * @return failure message of the result
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Sets the failure message of the result.
     *
     * @param failureMessage failure message of the result
     */
    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    /**
     * Returns the bytes of the result.
     *
     * @return bytes of the result
     */
    public String getBytes() {
        return bytes;
    }

    /**
     * Sets the bytes of the result.
     *
     * @param bytes bytes of the result
     */
    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    /**
     * Returns the sent bytes of the result.
     *
     * @return sent bytes of the result
     */
    public String getSentBytes() {
        return sentBytes;
    }

    /**
     * Sets the sent bytes of the result.
     *
     * @param sentBytes sent bytes of the result
     */
    public void setSentBytes(String sentBytes) {
        this.sentBytes = sentBytes;
    }

    /**
     * Returns the grp threads of the result.
     *
     * @return grp threads of the result
     */
    public String getGrpThreads() {
        return grpThreads;
    }

    /**
     * Sets the grp threads of the result.
     *
     * @param grpThreads grp threads of the result
     */
    public void setGrpThreads(String grpThreads) {
        this.grpThreads = grpThreads;
    }

    /**
     * Returns the threads of the result.
     *
     * @return threads of the result
     */
    public String getAllThreads() {
        return allThreads;
    }

    /**
     * Sets the threads of the result.
     *
     * @param allThreads threads of the result
     */
    public void setAllThreads(String allThreads) {
        this.allThreads = allThreads;
    }

    /**
     * Returns the latency of the result.
     *
     * @return latency of the result
     */
    public String getLatency() {
        return latency;
    }

    /**
     * Sets the latency of the result.
     *
     * @param latency latency of the result
     */
    public void setLatency(String latency) {
        this.latency = latency;
    }

    /**
     * Returns the idle time of the result.
     *
     * @return idle time of the result
     */
    public String getIdleTime() {
        return idleTime;
    }

    /**
     * Sets the idle time of the result.
     *
     * @param idleTime idle time of the result
     */
    public void setIdleTime(String idleTime) {
        this.idleTime = idleTime;
    }

    /**
     * Returns the connect of the result.
     *
     * @return connect of the result
     */
    public String getConnect() {
        return connect;
    }

    /**
     * Sets the connect of the result.
     *
     * @param connect connect of the result
     */
    public void setConnect(String connect) {
        this.connect = connect;
    }
}
