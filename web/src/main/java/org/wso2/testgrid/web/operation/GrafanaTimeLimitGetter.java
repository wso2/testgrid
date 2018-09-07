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

package org.wso2.testgrid.web.operation;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.config.ConfigurationContext;

import java.util.List;

/**
 * This class is to get the start time and end time of the test run. These values are used to set the time period of
 * Grafana dashboard
 */
public class GrafanaTimeLimitGetter {

    private static final Logger logger = LoggerFactory.getLogger(GrafanaTimeLimitGetter.class);
    private String testplanID;
    private String startTime;
    private String endTime;
    private String influxUrl = TestGridConstants.HTTP + ConfigurationContext.getProperty
            (ConfigurationContext.ConfigurationProperties.INFLUXDB_URL);
    private String username =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_USER);
    private String password =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_PASS);

    public GrafanaTimeLimitGetter(String testplanID) {
        this.testplanID = testplanID;
        setTimePeriod();
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    /**
     * this method wil get the time stamp of the first data point and the last data point with respect to testplan ID
     */
    private void setTimePeriod() {
        try {
            InfluxDB influxDB = InfluxDBFactory.connect(influxUrl, username, password);
            String dbName = testplanID;
            Query query = new Query("select first(used), time from mem", dbName);
            QueryResult queryResult = influxDB.query(query);

            InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
            List<TimeLimits> cpuList = resultMapper.toPOJO(queryResult, TimeLimits.class);
            TimeLimits timeLimits = cpuList.get(cpuList.size() - 1);
            startTime = String.valueOf(timeLimits.getTime().getEpochSecond() * 1000);
            query = new Query("select last(used), time from mem", dbName);
            queryResult = influxDB.query(query);
            cpuList = resultMapper.toPOJO(queryResult, TimeLimits.class);
            timeLimits = cpuList.get(cpuList.size() - 1);
            endTime = String.valueOf(timeLimits.getTime().getEpochSecond() * 1000);

        } catch (Throwable e) {
            logger.error("Error while getting start time and end time of test run" + e);
        }
    }
}
