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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This class create a database in influxDB and a Data Source in
 */
public class DashboardSetup {

    private static final Logger logger = LoggerFactory.getLogger(DashboardSetup.class);
    private String restUrl = "http://" +
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.PERFORMANCE_DASHBOARD_URL);
    private String username =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_USER);
    private String password =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_PASS);
    private String apikey =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.GRAFANA_APIKEY);
    private String testplanID;

    public DashboardSetup(String testplanID) {
        this.testplanID = testplanID;
    }

    /**
     * This method will create a DB in influxDB and add a new Data source to grafana
     */
    public void initDashboard() {
        // create influxDB database according to tp_id
        try {
            InfluxDB influxDB = InfluxDBFactory.connect(restUrl + ":8086", username, password);
            String dbName = testplanID;
            influxDB.createDatabase(dbName);
            influxDB.close();
        } catch (AssertionError e) {
            logger.error(StringUtil.concatStrings("Cannot create a new Database: \n", e));
        } catch (IllegalArgumentException e) {
            logger.error(StringUtil.concatStrings("INFLUXDB_USER and INFLUXDB_PASS cannot be empty: \n", e));
        }
        // add a new data source to grafana
        HttpPost httpPost = createConnectivity(restUrl, apikey);
        executeReq(getDataSource(testplanID), httpPost);

    }

    /**
     * This method creates a json string that describes the grafana datasource
     *
     * @param name name of the data source that need to be created which is test plan ID
     * @return string of jason string of the data source
     */
    private String getDataSource(String name) {

        JSONObject user = new JSONObject();
        user.put("name", name);
        user.put("type", "influxdb");
        user.put("url", "http://localhost:8086");
        user.put("access", "proxy");
        user.put("basicAuth", false);
        user.put("password", ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.INFLUXDB_PASS));
        user.put("user", ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_USER));
        user.put("database", name);

        return user.toString();

    }

    /**
     * this method will create the header of the POST request
     * @param restUrl url of the grafana server
     * @param apikey APIkey of the grafana server
     * @return return a http post request
     */
    private HttpPost createConnectivity(String restUrl, String apikey) {
        HttpPost post = new HttpPost(restUrl + ":3000/api/datasources/");
        post.setHeader("AUTHORIZATION", apikey);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");
        return post;
    }

    /**
     * This command will handle the posting http request to REST  api
     * @param jsonData json string of the data source
     * @param httpPost post request with authentication
     */
    private void executeReq(String jsonData, HttpPost httpPost) {
        try {
            executeHttpRequest(jsonData, httpPost);
        } catch (UnsupportedEncodingException e) {
            logger.error(StringUtil.concatStrings("error while encoding grafana http api url : ", e));
        } catch (IOException e) {
            logger.error(StringUtil.concatStrings("ioException occured while sending http request : ", e));
        } catch (Exception e) {
            logger.error(StringUtil.concatStrings("Exception occured while sending http request : ", e));
        } finally {
            httpPost.releaseConnection();
        }
    }

    /**
     * This method will execute the post request
     * @param jsonData json string of the data source
     * @param httpPost post request with authentication
     * @throws UnsupportedEncodingException thown when an error with api key
     * @throws IOException thrown when error in connection
     */
    private void executeHttpRequest(String jsonData,  HttpPost httpPost)  throws UnsupportedEncodingException,
            IOException {
        httpPost.setEntity(new StringEntity(jsonData));
        HttpClient client = HttpClientBuilder.create().build();
        client.execute(httpPost);
    }

}
