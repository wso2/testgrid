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


import org.apache.http.entity.ContentType;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBIOException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * This class create a database in influxDB and a Data Source in Grafana according to the test plan
 */
public class GrafanaDashboardHandler {

    private static final Logger logger = LoggerFactory.getLogger(GrafanaDashboardHandler.class);
    private String restUrl =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_URL);
    private String username =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_USER);
    private String password =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_PASS);
    private String apikey =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.GRAFANA_APIKEY);
    private String testplanID;


    public GrafanaDashboardHandler(String testplanID) {
        this.testplanID = testplanID;
    }

    /**
     * This method will create a DB in influxDB and add a new Data source to grafana
     */
    public void initDashboard() {
        if (!ConfigurationContext.isGrafanaDashboardEnabled()) {
            logger.info("Grafana dashboard is not configured for this testgrid deployment");
            return;
        }
        // create influxDB database according to tp_id
        InfluxDB influxDB = null;
        try {
            influxDB = InfluxDBFactory.connect(TestGridConstants.HTTP + restUrl, username, password);
            String dbName = testplanID;
            influxDB.createDatabase(dbName);

            logger.info(StringUtil.concatStrings("database created for testplan: ", testplanID,
                    "and DB name: ", dbName));

            // add a new data source to grafana
            addGrafanaDataSource();
            //TODO: Fixing immediate build hanging issue.
            //configureTelegrafHost();
        } catch (AssertionError e) {
            logger.error("Cannot create a new Database. Test plan ID: " + testplanID, e);
        } catch (IllegalArgumentException e) {
            logger.error("INFLUXDB_USER and INFLUXDB_PASS cannot be empty. Test plan ID: " + testplanID, e);
        } catch (InfluxDBIOException e) {
            logger.warn("InfluxDB is temporarily disabled. Hence, not creating deployment monitor database (InfluxDB). "
                    + "Test plan ID: " + testplanID + ". Root cause: " + e.getMessage());
        } finally {
            if (influxDB != null) {
                try {
                    influxDB.close();
                } catch (Exception e) {
                    logger.error("Couldn't close the influxDB connection");
                }
            }
        }
    }

    /**
     * This method will set telegraf host name and start telegraf using tinkerer
     */
//    private void configureTelegrafHost() {
//        try {
//            String shellCommand;
//            TinkererSDK tinkererSDK = new TinkererSDK();
//            List<Agent> agents = tinkererSDK.getAgentListByTestPlanId(this.testplanID);
//
//            for (Agent vm : agents) {
//                shellCommand = "sudo sed -i 's/wso2_sever/" + vm.getInstanceName() + "-"
//                        + vm.getInstanceId() + "/g' /etc/telegraf/telegraf.conf";
//                logger.info(StringUtil.concatStrings("agent: ", vm.getInstanceName(), "Shell Command: ",
//                        shellCommand));
//                tinkererSDK.executeCommandAsync(vm.getAgentId(), shellCommand);
//
//                shellCommand = "sudo systemctl start telegraf";
//                tinkererSDK.executeCommandAsync(vm.getAgentId(), shellCommand);
//            }
//        } catch (ProcessingException | IllegalArgumentException e) {
//            logger.error("Error while configuring telegraf host for testplan " + testplanID, e);
//        }
//    }

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

        // Install the all-trusting host verifier
        HostnameVerifier allHostsValid = new HostValidator();
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        DataOutputStream dataOutputStream = null;

        try {
            String url = "https://" + ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties
                    .GRAFANA_DATASOURCE) + "/api/datasources/";

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
