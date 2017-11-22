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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.testgrid.deployment;

import org.awaitility.Awaitility;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class performs tasks related to validating if a deployment is successful.
 *
 * @since 1.0.0
 */
public class DeploymentValidator {

    /**
     * Wait on the Url response untile the defined timeout.
     *
     * @param url             The URL endpoint of deployment.
     * @param timeout         Time to wait upon the desired response.
     * @param timeoutTimeUnit TimeUnit of timeout value.
     * @param pollInterval    Time interval to perform polling.
     * @param pollTimeUnit    TimeUnit of pollInterval.
     */
    public void waitForDeployment(String url, int timeout, TimeUnit timeoutTimeUnit, int pollInterval,
                                  TimeUnit pollTimeUnit) {
        Awaitility.with().pollInterval(pollInterval, pollTimeUnit).await().
                atMost(timeout, timeoutTimeUnit).until(isDeploymentSuccessful(url));
    }

    /**
     * This method checks if the given URL provides the HTTP_OK (200) response.
     *
     * @param urlString The URL to perform the check
     * @return Returns true if expected response received.
     */
    private Callable<Boolean> isDeploymentSuccessful(String urlString) {
        return new AWSCallable(urlString);
    }

    /**
     * This class is the callable implementation used by awaitility to determine the
     * condition of server.
     */
    private static class AWSCallable implements Callable<Boolean> {

        private String urlString;

        /**
         * Constructs the AWSCallable object with Url of server.
         * @param urlString URL as a String.
         */
        AWSCallable(String urlString) {
            this.urlString = urlString;
        }

        @Override
        public Boolean call() throws TestGridDeployerException {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new AWSTrustManager()
            };
            boolean response;
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                URL url = new URL(urlString);
                HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
                urlConn.setHostnameVerifier(new AWSHostNameVerifier());
                urlConn.connect();
                response = (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK);

            } catch (MalformedURLException e) {
                throw new TestGridDeployerException("Deployment URL " + urlString + " is malformed", e);
            } catch (NoSuchAlgorithmException e) {
                throw new TestGridDeployerException("Error configuring the SSL client", e);
            } catch (IOException e) {
                throw new TestGridDeployerException("Error when connecting with URL :" + urlString, e);
            } catch (KeyManagementException e) {
                throw new TestGridDeployerException("Error occurred while configuring key manager", e);
            }
            return response;
        }
    }

    /**
     * Implementation of the TrustManager interface to suit the needs of Awaitility function.
     */
    private static class AWSTrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(
                X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(
                X509Certificate[] certs, String authType) {
        }
    }

    /**
     * HostNameVerifier that accepts all host names.
     */
    private static class AWSHostNameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}
