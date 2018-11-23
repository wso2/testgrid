package org.wso2.testgrid.phase1;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.wso2.testgrid.TestProperties;
import org.wso2.testgrid.common.EmailUtils;
import org.wso2.testgrid.common.HostValidator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TestPhase1 {

    private static final Logger logger = LoggerFactory.getLogger(TestPhase1.class);
    private TestProperties testProperties;

    @BeforeTest
    public void init() {

        testProperties = new TestProperties();
        HostnameVerifier allHostsValid = new HostValidator();
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    @Test(dataProvider = "jobs")
    public void buildTriggerTest(String jobName) {

        JenkinsJob currentBuild = null;
        HttpsURLConnection connection = null;
        logger.info("Running tests for the job : " + jobName);
        try {
            String jenkinsToken = testProperties.jenkinsToken;
            URL buildTriggerUrl = new URL(TestProperties.jenkinsUrl + "/job/" + jobName + "/build?token=" +
                                          jenkinsToken);
            URL buildStatusUrl =
                    new URL(TestProperties.jenkinsUrl + "/job/" + jobName + "/lastBuild/api/json");

            JenkinsJob jenkinsJob = getLastJob(buildStatusUrl);
            logger.info("Job status for job ID : " + jenkinsJob.id + " : " + jenkinsJob.status);

            connection = (HttpsURLConnection) buildTriggerUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            SSLSocketFactory sslSocketFactory = createSslSocketFactory();
            connection.setSSLSocketFactory(sslSocketFactory);

            int response = connection.getResponseCode();
            if (response == 201) {
                logger.info("build Triggered");
            } else {
                Assert.fail("Phase 1 build couldn't be triggered. Response code : " + response);
            }

            jenkinsJob = getLastJob(buildStatusUrl);

            while (jenkinsJob.building) {
                jenkinsJob = getLastJob(buildStatusUrl);
                logger.info(jobName + " #(" + jenkinsJob.id + ") building ");
                TimeUnit.SECONDS.sleep(2);
            }

            currentBuild = getLastJob(buildStatusUrl);

            Assert.assertEquals(currentBuild.status, "SUCCESS");

            EmailUtils emailUtils = connectToEmail();
            testTextContained(emailUtils, jenkinsJob.id);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        // Validating the logs
        logTest(jobName, currentBuild.id);
        summaryTest(jobName, currentBuild.id);
    }

    //@Test(dependsOnMethods = {"buildTriggerTest"})
    public void logTest(String jobName, String buildID) {

        try {
            validateLog(getTestPlanID(jobName, buildID));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }

    public void summaryTest(String jobName, String buildID) {

        try {
            testSummaryValidate(getTestPlanID(jobName, buildID));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public void validateLog(String testPlan) throws Exception {

        String webPage = "https://testgrid-live-dev.private.wso2.com/api/test-plans/log/" + testPlan;

        URL url = new URL(webPage);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "test");
        SSLSocketFactory sslSocketFactory = createSslSocketFactory();
        connection.setSSLSocketFactory(sslSocketFactory);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            if (response.toString().contains(testPlan)) {
                logger.info("Correct log is found");
            } else {
                Assert.fail("Correct log is not found");
            }
        }
    }

    public String getTestPlanID(String jobName, String buildNo) throws Exception {

        URL url = new URL(testProperties.jenkinsUrl + "/job/" + jobName + "/" + buildNo +
                          "/consoleText");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        SSLSocketFactory sslSocketFactory = createSslSocketFactory();
        connection.setSSLSocketFactory(sslSocketFactory);
        int responseCode = connection.getResponseCode();
        String testplan;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuffer response = new StringBuffer();

            String patternString = ".*Preparing workspace for testplan.*";

            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher;
            String rowTestPlanID = "";
            while ((inputLine = in.readLine()) != null) {
                matcher = pattern.matcher(inputLine);
                if (matcher.find()) {
                    rowTestPlanID = inputLine;
                    break;
                }
            }
            testplan = rowTestPlanID.split(":")[5].replaceAll("\\s", "");
        }
        connection.disconnect();
        return testplan;
    }

    private void testTextContained(EmailUtils emailUtils, String buildNo) {

        try {
            for (int i = 0; i < 30; i++) {
                Message[] emails = emailUtils.getMessagesBySubject("'Phase-1' Test Results! #(" + buildNo + ")",
                        false, 100);
                if (emails.length != 0) {
                    logger.info("EMAIL Found");
                    break;
                }
                TimeUnit.SECONDS.sleep(1);
                logger.info("Waiting for email");
            }
            Message email = emailUtils.getMessagesBySubject("'Phase-1' Test Results! #(" + buildNo + ")",
                    false, 100)[0];
            Assert.assertTrue(emailUtils.isTextInMessage(email, "Phase-1 integration test Results!"),
                    "Phase-1 integration test Results!");
            logger.info("Email received on " + email.getReceivedDate());
        } catch (ArrayIndexOutOfBoundsException e) {
            Assert.fail("Email not recieved for the build");
        } catch (Exception e) {
            // Log Error
        }
    }

    public static EmailUtils connectToEmail() {

        try {
            //gmail need to alow less secure apps
            EmailUtils emailUtils = new EmailUtils(TestProperties.email, TestProperties.emailPassword,
                    "smtp.gmail.com", EmailUtils.EmailFolder.INBOX);
            return emailUtils;
        } catch (MessagingException e) {
            Assert.fail(e.getMessage());
            return null;
        }
    }

    private void testSummaryValidate(String testplan) throws Exception {

        URL url = new URL("https://testgrid-live-dev.private.wso2.com/api/test-plans/test-summary/" + testplan);

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "test");
        SSLSocketFactory sslSocketFactory = createSslSocketFactory();
        con.setSSLSocketFactory(sslSocketFactory);
        StringBuffer response;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        JSONObject myresponse = new JSONObject(response.toString());
        myresponse = myresponse.getJSONArray("scenarioSummaries").getJSONObject(0);

        Assert.assertEquals(myresponse.getInt("totalSuccess"), 541);
        Assert.assertEquals(myresponse.getInt("totalFail"), 0);
        Assert.assertEquals(myresponse.getString("scenarioDescription"), "Test-Phase-1");
        con.disconnect();
    }

    private JenkinsJob getLastJob(URL url) throws Exception {

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setDoOutput(true);
        SSLSocketFactory sslSocketFactory = createSslSocketFactory();
        con.setSSLSocketFactory(sslSocketFactory);

        StringBuffer response;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        JSONObject responseObj = new JSONObject(response.toString());
        JenkinsJob jenkinsJob;
        if (responseObj.getBoolean("building")) {
            jenkinsJob = new JenkinsJob(responseObj.getString("id"), responseObj.getBoolean("building"));
        } else {
            jenkinsJob = new JenkinsJob(responseObj.getString("result"), responseObj.getString("id"),
                    responseObj.getBoolean("building"));
        }
        con.disconnect();
        return jenkinsJob;
    }

    /**
     * Bean to capture Jenkins Job information.
     */
    public static class JenkinsJob {

        public String status;
        public String id;
        public boolean building;

        public JenkinsJob(String status, String id, Boolean building) {

            this.status = status;
            this.id = id;
            this.building = building;
        }

        public JenkinsJob(String id, Boolean building) {

            this(null, id, building);
        }
    }

    /**
     * This method is to bypass SSL verification
     *
     * @return SSL socket factory that by will bypass SSL verification
     * @throws Exception java.security exception is thrown in an issue with SSLContext
     */
    private static SSLSocketFactory createSslSocketFactory() throws Exception {

        TrustManager[] byPassTrustManagers = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {

                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }
        }};
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, byPassTrustManagers, new SecureRandom());
        return sslContext.getSocketFactory();
    }

    @DataProvider(name = "jobs")
    public static Object[][] jobs() {

        return new Object[][]{{"Phase-1"}, {"Phase-2"}};
    }
}
