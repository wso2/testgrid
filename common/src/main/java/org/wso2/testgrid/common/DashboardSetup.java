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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.tinkerer.AsyncCommandResponse;
import org.wso2.testgrid.common.util.tinkerer.TinkererUtil;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.ProcessingException;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * This class create a database in influxDB and a Data Source in Grafana according to the test plan
 */
public class DashboardSetup {

    private static final Logger logger = LoggerFactory.getLogger(DashboardSetup.class);
    private String restUrl =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_URL);
    private String username =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_USER);
    private String password =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_PASS);
    private String apikey =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.GRAFANA_APIKEY);
    private String testplanID;
    private String productName;
    private int grafanaDataSourceCount = 20;


    public DashboardSetup(String testplanID, String productName) {
        this.testplanID = testplanID;
        this.productName = productName;
    }

    /**
     * This method will create a DB in influxDB and add a new Data source to grafana
     */
    public void initDashboard() {
        // create influxDB database according to tp_id
        try {
            InfluxDB influxDB = InfluxDBFactory.connect(TestGridConstants.HTTP + restUrl, username, password);
            String dbName = testplanID;
            influxDB.createDatabase(dbName);
            influxDB.close();
            logger.info("database created for performance data");
        } catch (AssertionError e) {
            logger.error(StringUtil.concatStrings("Cannot create a new Database: \n", e));
        } catch (IllegalArgumentException e) {
            logger.error(StringUtil.concatStrings("INFLUXDB_USER and INFLUXDB_PASS cannot be empty: \n", e));
        }

        // add a new data source to grafana
        addGrafanaDataSource();
        setTelegrafHost();

    }

    private void setTelegrafHost() {
        try {
            TinkererUtil tinkererSDK = new TinkererUtil();
            List<Agent> agents = tinkererSDK.getAgentListByTestPlanId(this.testplanID);
            String command = "echo complete > /opt/testgrid/agent/yes.log";
            for (Agent agent : agents) {
                AsyncCommandResponse response = tinkererSDK.executeCommandAsync(agent.getAgentId(), command);
            }
        } catch (ProcessingException e) {
            logger.info("Error while configuring telegraf host" + e);
        }



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
        user.put("url", TestGridConstants.HTTP + ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.INFLUXDB_URL));
        user.put("access", "proxy");
        user.put("basicAuth", false);
        user.put("password", ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.INFLUXDB_PASS));
        user.put("user", ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_USER));
        user.put("database", name);

        return user.toString();

    }

    /**
     * This method will create a grafana datasource according to TestPlanID
     */
    public void addGrafanaDataSource() {

        List<String> infraCombinations = new ArrayList<String>();
        List<String> toDelete = new ArrayList<String>();
        List<String> datasource;

        // Install the all-trusting host verifier
        HostnameVerifier allHostsValid = new HostValidator();
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        datasource = getDataSources();

        Connection dbcon = null;
        PreparedStatement stmt = null;
        String testplan;
        ResultSet resultSet = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbcon = DriverManager.getConnection(ConfigurationContext.getProperty(ConfigurationContext.
                    ConfigurationProperties.DB_URL), ConfigurationContext.getProperty(ConfigurationContext.
                    ConfigurationProperties.DB_USER), ConfigurationContext.getProperty(ConfigurationContext.
                    ConfigurationProperties.DB_USER_PASS));


            stmt = dbcon.prepareStatement("select distinct infra_parameters from test_plan");
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                infraCombinations.add(resultSet.getString(1));
            }

            stmt = dbcon.prepareStatement("drop table if exists temp ;");
            Boolean complete = stmt.execute();
            stmt = dbcon.prepareStatement("create table temp (id VARCHAR(36), modified_timestamp TIMESTAMP, " +
                    "name VARCHAR(50), infra_parameters  VARCHAR(255)); ");
            complete = stmt.execute();
            stmt = dbcon.prepareStatement("INSERT INTO  temp (id,name,modified_timestamp,infra_parameters ) " +
                    "select test_plan.id, product.name, test_plan.modified_timestamp, test_plan.infra_parameters " +
                    "from test_plan,product,deployment_pattern " +
                    "where test_plan.DEPLOYMENTPATTERN_id = deployment_pattern.id and " +
                    "deployment_pattern.PRODUCT_id = product.id;");
            complete = stmt.execute();

            for (String infra : infraCombinations) {
                logger.info(infra);
                stmt = dbcon.prepareStatement("select id from temp as tp left join " +
                        "(select id from temp where name = ? and  " +
                        "infra_parameters = ? order by (modified_timestamp) DESC " +
                        "limit ?)p2 USING(id) WHERE p2.id IS NULL and infra_parameters = ?  and name = ?" +
                        "order by (modified_timestamp) DESC");

                stmt.setString(1, productName);
                stmt.setString(2, infra);
                stmt.setInt(3, grafanaDataSourceCount);
                stmt.setString(4, infra);
                stmt.setString(5, productName);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    testplan = resultSet.getString(1);
                    if (datasource.contains(testplan)) {
                        toDelete.add(testplan);
                        logger.info(testplan + " added to delete");
                    }
                }
            }
            stmt = dbcon.prepareStatement("drop table if exists temp ;");
            complete = stmt.execute();

        } catch (SQLException e) {
            logger.error("error while trying to retreive the datasources that needs to be deleted" + e);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found" + e);
        } finally {
            if (dbcon != null) {
                DbUtils.closeQuietly(dbcon);
            }
            if (stmt != null) {
                DbUtils.closeQuietly(stmt);
            }

        }

        if (!toDelete.isEmpty()) {
            for (String dataSource : toDelete) {
                logger.info("deleting data source: " + dataSource);
                this.clearDataSources(dataSource);
            }
        }

        DataOutputStream dataOutputStream = null;

        try {
            String url = "https://" + ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties
                    .GRAFANA_DATASOURCE) + ":3000/api/datasources/";

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Accept", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Authorization", apikey);
            SSLSocketFactory sslSocketFactory = createSslSocketFactory();
            con.setSSLSocketFactory(sslSocketFactory);
            String urlParameters = getDataSource(testplanID);
            con.setDoOutput(true);
            dataOutputStream = new DataOutputStream(con.getOutputStream());
            dataOutputStream.writeBytes(urlParameters);
            dataOutputStream.flush();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("grafana Data Source created for testplan " + testplanID);
            } else {
                logger.error(StringUtil.concatStrings("failed to create grafana Data testplan ", testplanID,
                        " Response Code ", responseCode));
            }
        } catch (Exception e) {
            logger.error("Error while creating Grafana data source" + e);
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (Exception e) {
                    logger.error("Couldn't close the output stream");
                }
            }
        }
    }

    private void clearDataSources(String datasource) {
        try {
            String url = "https://" + ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties
                    .GRAFANA_DATASOURCE) + ":3000/api/datasources/name/" + datasource;

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            //add request header
            con.setRequestMethod("DELETE");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Accept", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Authorization", apikey);
            SSLSocketFactory sslSocketFactory = createSslSocketFactory();
            con.setSSLSocketFactory(sslSocketFactory);


            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("grafana Data Source deleted for testplan " + datasource);
            } else {
                logger.error(StringUtil.concatStrings("failed to delete grafana Data source ",
                        " Response Code ", responseCode));
            }
        } catch (Exception e) {
            logger.error("Error while deleting Grafana data source" + e);
        }

    }

    private  List<String> getDataSources() {
        try {
            String url = "https://" + ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties
                    .GRAFANA_DATASOURCE) + ":3000/api/datasources/";

            List<String> datasouce = new ArrayList<String>();

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            //add request header
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Accept", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Authorization", apikey);

            SSLSocketFactory sslSocketFactory = createSslSocketFactory();
            con.setSSLSocketFactory(sslSocketFactory);

            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(in, encoding);
            JSONArray dataSources = new JSONArray(body);
            String name;

            for (Object data:dataSources) {
                JSONObject first = (JSONObject) data;
                name = first.getString("name");
                datasouce.add(name);
            }
            return datasouce;
        } catch (Exception e) {
            logger.info("Error while getting the data sources in grafana: " + e);
            return null;
        }
    }

    /**
     * This method is to bypass SSL verification for Grafana dashboard URL
     * @return SSL socket factory that by will bypass SSL verification
     * @throws Exception java.security exception is thrown in an issue with SSLContext
     */
    private static SSLSocketFactory createSslSocketFactory() throws Exception {
        TrustManager[] byPassTrustManagers = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        } };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, byPassTrustManagers, new SecureRandom());
        return sslContext.getSocketFactory();
    }
}
