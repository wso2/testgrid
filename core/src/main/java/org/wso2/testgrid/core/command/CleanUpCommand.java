package org.wso2.testgrid.core.command;


import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.HostValidator;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * this class is for data purging
 */

public class CleanUpCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CleanUpCommand.class);

    private String apikey =
            ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.GRAFANA_APIKEY);

    @Option(name = "--product",
            usage = "Product Name",
            aliases = { "-p" },
            required = true)
    private String productName = "";
    private int grafanaDataSourceCount = 10;
    private TestPlanUOW testPlanUOW;

    @Override
    public void execute() throws CommandExecutionException {

        List<String> toDelete = new ArrayList<String>();
        List<String> datasource;

        // Install the all-trusting host verifier
        HostnameVerifier allHostsValid = new HostValidator();
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        datasource = getDataSources();
        testPlanUOW = new TestPlanUOW();

        try {
            List<String> allTestPlans = testPlanUOW.getDeletingDataSources(productName, grafanaDataSourceCount);
            logger.info("number of items to delete: " + allTestPlans.size());

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
            } else {
                logger.info("NOTHING TO DELETE");
            }

        } catch (Exception e) {
            throw new CommandExecutionException("error while retrieving the data that needs to be deleted", e);
        }
    }

    /**
     * This method will delete a given data source from grafana
     * @param datasource name of the datasource that need to be deleted
     */
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

    /**
     * this method is to get the list of data sources
     * @return list of data sources in grafana
     */
    private List<String> getDataSources() {
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
