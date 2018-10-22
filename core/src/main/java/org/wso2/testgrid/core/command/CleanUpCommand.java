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

package org.wso2.testgrid.core.command;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.HostValidator;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.S3StorageUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.apache.http.protocol.HTTP.USER_AGENT;


/**
 * This class is for data purging. This class will be used to purge testplans, grafana data sources and keep only a
 * specified number of testplans and grafana data sources for each infra combination in each job
 * It will not delete builds that are tagged as "keep forever"
 */

public class CleanUpCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CleanUpCommand.class);
    private int status;
    private List<String> datasource;
    private String grafanaUrl = ConfigurationContext.getProperty
            (ConfigurationContext.ConfigurationProperties.GRAFANA_DATASOURCE);
    private String grafanaApikey =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.GRAFANA_APIKEY);
    private List<String> toDelete = new ArrayList<String>();

    @Option(name = "--count",
            usage = "Build Count",
            aliases = { "-c" },
            required = true)
    private int remainingBuildCount = 100;



    private TestPlanUOW testPlanUOW;

    public List<String> getToDelete() {

        return toDelete;
    }

    public CleanUpCommand() {

        HostnameVerifier allHostsValid = new HostValidator();
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        testPlanUOW = new TestPlanUOW();
        datasource = getDataSources();
    }

    public CleanUpCommand(int status, int remainingBuildCount, TestPlanUOW testPlanUOW, List<String> datasource,
                          String grafanaUrl) {
        HostnameVerifier allHostsValid = new HostValidator();
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        this.status = status;
        this.remainingBuildCount = remainingBuildCount;
        this.testPlanUOW = testPlanUOW;
        this.datasource = datasource;
        this.grafanaUrl = grafanaUrl;

    }

    public int getStatus() {

        return status;
    }

    public void setStatus(int status) {

        this.status = status;
    }

    @Override
    public void execute() throws CommandExecutionException {

        try {
            List<String> allTestPlans = testPlanUOW.deleteDatasourcesByAge(remainingBuildCount);
            logger.info("number of items to delete: " + allTestPlans.size());

            logger.info("Clearing S3 files");
            for (String deletingTestPlan : allTestPlans) {
                deleteS3(deletingTestPlan);
            }

            logger.info("Deleting test plan from DB");
            testPlanUOW.deleteTestPlans(allTestPlans);

            logger.info("Deleting Grafana Data Sources");
            for (String deletingTestPlan : allTestPlans) {
                if (datasource.contains(deletingTestPlan)) {
                    toDelete.add(deletingTestPlan);
                    logger.info(deletingTestPlan + " added to delete");
                }
            }
            if (!toDelete.isEmpty()) {
                for (String dataSource : toDelete) {
                    logger.info("deleting data source: " + dataSource);
                    this.clearDataSources(dataSource);
                }
            }

        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(e.getMessage(), e);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("error while retrieving the data that needs to be deleted", e);
        }
    }

    /**
     * This method will delete a given data source from grafana
     * @param datasource name of the datasource that need to be deleted
     */
    private void clearDataSources(String datasource) {
        try {
            String url = "https://" + grafanaUrl + "/api/datasources/name/" + datasource;

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            //add request header
            con.setRequestMethod("DELETE");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Accept", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Authorization", grafanaApikey);
            SSLSocketFactory sslSocketFactory = createSslSocketFactory();
            con.setSSLSocketFactory(sslSocketFactory);


            int responseCode = con.getResponseCode();
            status = responseCode;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("grafana Data Source deleted for testplan " + datasource);
            } else {
                logger.error(StringUtil.concatStrings("failed to delete grafana Data source ",
                        " Response Code ", responseCode));
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while connecting to grafana server  ", e);
        } catch (MalformedURLException e) {
            logger.error("Error with Grafana Data source URL ", e);
        } catch (IOException e) {
            logger.error("Error while deleting Grafana data source", e);
        } catch (KeyManagementException e) {
            logger.error("Error while connecting to grafana server  ", e);
        }

    }

    /**
     * This method is to get the list of data sources
     * @return list of data sources in grafana
     */
    private List<String> getDataSources() {

        InputStream in = null;
        try {
            String url = "https://" + grafanaUrl + "/api/datasources/";

            List<String> datasouce = new ArrayList<String>();

            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            //add request header
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Accept", ContentType.APPLICATION_JSON.toString());
            con.setRequestProperty("Authorization", grafanaApikey);

            SSLSocketFactory sslSocketFactory = createSslSocketFactory();
            con.setSSLSocketFactory(sslSocketFactory);

            in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(in, encoding);
            JSONArray dataSources = new JSONArray(body);
            String name;

            for (Object data : dataSources) {
                JSONObject first = (JSONObject) data;
                name = first.getString("name");
                datasouce.add(name);
            }
            return datasouce;
        } catch (IOException e) {
            logger.info("Error while getting the data sources in grafana: " + e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while connecting to grafana server  ", e);
            return null;
        } catch (KeyManagementException e) {
            logger.error("Error with SSL key management while connecting to grafana server  ", e);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.error("Couldn't close the input stream");
                }
            }
        }
    }

    /**
     * This method delete the files in s3 for a given test plan
     * @param testPlanId tes plan id of which the files need to be deleted
     * @throws TestGridDAOException
     */
    public void deleteS3(String testPlanId) throws TestGridDAOException {

        Optional<TestPlan> testPlanEntity = testPlanUOW.getTestPlanById(testPlanId);
        TestPlan testPlan = testPlanEntity.get();
        S3StorageUtil.deleteTestPlan(testPlan);

    }

    /**
     * This method is to bypass SSL verification for Grafana dashboard URL
     * @return SSL socket factory that by will bypass SSL verification
     * @throws Exception java.security exception is thrown in an issue with SSLContext
     */
    private static SSLSocketFactory createSslSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
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
