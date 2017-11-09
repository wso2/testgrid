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
package org.wso2.carbon.testgrid.reporting.result;

import org.wso2.carbon.testgrid.reporting.ReportingException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Result model to capture jmeter test result.
 *
 * @since 1.0.0
 */
public class JmeterTestResult implements TestResultable {

    private static final String DATE_FORMAT = "dd-MM-yyyy hh:mm:ss";

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

    @Override
    public boolean isTestSuccess() {
        return Boolean.valueOf(success);
    }

    @Override
    public String getTimestamp() {
        return timeStamp;
    }

    @Override
    public String getFormattedTimestamp() throws ReportingException {
        Long timestamp = Long.parseLong(timeStamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date(timestamp);
        return simpleDateFormat.format(date);
    }

    @Override
    public String getTestCase() {
        return label;
    }

    @Override
    public String getFailureMessage() {
        return failureMessage;
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
     * Returns the response code of the result.
     *
     * @return response code of the result
     */
    public String getResponseCode() {
        return responseCode;
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
     * Returns the thread name of the result.
     *
     * @return thread name of the result
     */
    public String getThreadName() {
        return threadName;
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
     * Returns the bytes of the result.
     *
     * @return bytes of the result
     */
    public String getBytes() {
        return bytes;
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
     * Returns the grp threads of the result.
     *
     * @return grp threads of the result
     */
    public String getGrpThreads() {
        return grpThreads;
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
     * Returns the latency of the result.
     *
     * @return latency of the result
     */
    public String getLatency() {
        return latency;
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
     * Returns the connect of the result.
     *
     * @return connect of the result
     */
    public String getConnect() {
        return connect;
    }
}
