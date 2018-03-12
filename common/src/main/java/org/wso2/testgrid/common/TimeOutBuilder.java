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

package org.wso2.testgrid.common;

import java.util.concurrent.TimeUnit;

/**
 * A TimeOut Builder for awaitality usages.
 */
public class TimeOutBuilder {

    private int timeOut;
    private TimeUnit timeOutUnit;
    private int pollInterval;
    private TimeUnit pollUnit;

    /**
     * Constructs a timeout object with defined timeouts and time units.
     *
     * @param timeout timeout for waiting
     * @param timeOutUnit time unit of timeout
     * @param pollInterval polling interval
     * @param pollUnit time unit of polling interval
     */
    public TimeOutBuilder(int timeout, TimeUnit timeOutUnit, int pollInterval, TimeUnit pollUnit) {

        this.timeOut = timeout;
        this.timeOutUnit = timeOutUnit;
        this.pollInterval = pollInterval;
        this.pollUnit = pollUnit;
    }

    public int getTimeOut() {

        return timeOut;
    }

    public TimeUnit getTimeOutUnit() {

        return timeOutUnit;
    }

    public int getPollInterval() {

        return pollInterval;
    }

    public TimeUnit getPollUnit() {

        return pollUnit;
    }
}
