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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.tinkerer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.tinkerer.exception.TinkererOperationException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is the {@link TinkererClient} implementation for agents that are running in
 * UNIX operating systems.
 *
 * @since 1.0.0
 */
public class UnixClient extends TinkererClient {

    private static final Logger logger = LoggerFactory.getLogger(UnixClient.class);
    private static final String SCENARIO_LOG_LOCATION = "/repository/logs/";
    private static final String INTEGRATION_LOG_LOCATION = "/logs/";
    private static final String WORKSPACE_DIR_POSIX = "REMOTE_WORKSPACE_DIR_UNIX";

    @Override
    public void downloadLogs(DeploymentCreationResult deploymentCreationResult, TestPlan testPlan)
            throws TinkererOperationException {

        String testType = testPlan.getScenarioConfig().getTestType();
        logger.info("TestType is : " + testType);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        for (Agent agent : deploymentCreationResult.getAgents()) {
            logger.info("Initiating LOG file download for Agent " + agent.getInstanceName()
                    + "\n agent instance ID " + agent.getInstanceId()
                    + "\n test plan " + testPlan.getId());

            String productBasePath = "";
            String logLocation = "";
            String operationPath = this.getTinkererBase() + "test-plan/" + agent.getTestPlanId()
                    + "/agent/" + agent.getInstanceName() + "/operation";
            if (TestGridConstants.TEST_TYPE_FUNCTIONAL.equals(testType)) {
                //get the carbon.home from the instance where the product is running
                String content = "";
                try {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("code", "SHELL");
                    payload.put("request", "ps -ef | grep 'carbon.home'");
                    Response execute = Request.Post(operationPath).setHeader
                            (HttpHeaders.CONTENT_TYPE, "application/json")
                            .setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder()
                                    .encodeToString(StringUtil.concatStrings(this.getTinkererUserName(),
                                            ":", this.getTinkererPassword()).getBytes(Charset.defaultCharset())))
                            .bodyString(gson.toJson(payload), ContentType.APPLICATION_JSON)
                            .execute();

                    HttpResponse httpResponse = execute.returnResponse();
                    if (httpResponse.getCode() != HttpStatus.SC_OK) {
                        throw new TinkererOperationException("Error occurred while performing tinkerer " +
                                "REST api call for retrieving carbon.home. \nError message :" +
                                EntityUtils.toString(((CloseableHttpResponse) httpResponse).getEntity()));
                    }

                    content = EntityUtils.toString(((CloseableHttpResponse) httpResponse).getEntity());
                } catch (IOException e) {
                    throw new TinkererOperationException("Error occurred while retrieving the carbon.home value" +
                            "from agent" + agent.getAgentId()
                            + "\nRunning on instance : " + agent.getInstanceName()
                            + "\nfor test plan " + testPlan.getId(), e);
                } catch (ParseException e) {
                    throw new TinkererOperationException("Error occurred while parsing the response " +
                            "wile retrieving carbon.home from agent" + agent.getAgentId()
                            + "\nRunning on instance : " + agent.getInstanceName()
                            + "\nfor test plan " + testPlan.getId(), e);
                }
                if (content.contains("carbon.home")) {
                    String patternString = "carbon\\.home=[a-z\\/0-9-.]*";
                    Pattern pattern = Pattern.compile(patternString);
                    Matcher matcher = pattern.matcher(content);

                    if (matcher.find()) {
                        productBasePath = matcher.group().split("=")[1];
                    }
                }
                logLocation = productBasePath + SCENARIO_LOG_LOCATION;
            } else if (TestGridConstants.TEST_TYPE_INTEGRATION.equals(testType)) {
                if (testPlan.getJobProperties().containsKey(WORKSPACE_DIR_POSIX)) {
                    productBasePath = testPlan.getJobProperties().getProperty(WORKSPACE_DIR_POSIX);
                } else {
                    throw new TinkererOperationException("Product workspace path is not present, Please check if" +
                            "entry is present in job-config.yml file : "
                            + "\nfor test plan :" + testPlan.getId());
                }
                /*If the path contains the ~ character we need to replace it with ~{OS-USER} because,
                agent executes as the root user, and it will infer ~ as root.
                ex: ~ --replace into--> ~centos*/
                productBasePath = productBasePath.replace("~", "~" + agent.getInstanceUser());
                logLocation = productBasePath + INTEGRATION_LOG_LOCATION;
            }
            logger.info("Product base path found " + productBasePath);
            Map<String, String> resultMap;
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("code", "SHELL");
                payload.put("request", "ls " + logLocation);
                Response logFiles = Request.Post(operationPath).setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .setHeader(HttpHeaders.AUTHORIZATION,
                                "Basic " + Base64.getEncoder().encodeToString(
                                        StringUtil.concatStrings(this.getTinkererUserName(),
                                                ":", this.getTinkererPassword()).getBytes(Charset.defaultCharset())))
                        .bodyString(gson.toJson(payload), ContentType.APPLICATION_JSON).execute();
                //check if REST api call was successful
                HttpResponse response = logFiles.returnResponse();
                if (response.getCode() != HttpStatus.SC_OK) {
                    throw new TinkererOperationException("Error occurred while performing tinkerer " +
                            "REST api call for getting list of log files. \nError message :"
                            + EntityUtils.toString(((CloseableHttpResponse) response).getEntity()));
                }
                //create a map of json response
                resultMap = new Gson().fromJson(EntityUtils.toString(((CloseableHttpResponse) response).getEntity()),
                        new PropertyType().getType());
            } catch (IOException e) {
                throw new TinkererOperationException("Error occurred while retrieving the list of log files from the" +
                        "log location : " + logLocation
                        + "\nfrom agent :" + agent.getAgentId()
                        + "\nRunning on instance : " + agent.getInstanceName()
                        + "\nfor test plan :" + testPlan.getId(), e);
            } catch (ParseException e) {
                throw new TinkererOperationException("Error occurred while parsing the list of log files from the" +
                        "log location : " + logLocation
                        + "\nfrom agent :" + agent.getAgentId()
                        + "\nRunning on instance : " + agent.getInstanceName()
                        + "\nfor test plan :" + testPlan.getId(), e);
            }
            if (resultMap != null && resultMap.containsKey("response")) {
                String response = resultMap.get("response");
                List<String> logFileNames = Arrays.asList(response.split("\n"));


                for (String logFileName : logFileNames) {
                    try {
                        logger.info("Downloading log file : " + logFileName);
                        String key = Base64.getEncoder()
                                .encodeToString(FileUtils.readFileToByteArray(new File(testPlan.getKeyFileLocation())));
                        String source = logLocation + logFileName;
                        String destination = Paths.get(TestGridUtil.deriveLogDownloadLocation(testPlan), logFileName)
                                .toString();
                        String bastianIP = deploymentCreationResult.getBastianIP();
                        String downloadPath = this.getTinkererBase() + "test-plan/" + agent.getTestPlanId() +
                                "/agent/" + agent.getInstanceName() + "/stream-file";
                        Map<String, Object> payload = new HashMap<>();
                        payload.put("code", "STREAM_FILE");
                        Map<String, Object> subPayload = new HashMap<>();
                        subPayload.put("key", key);
                        subPayload.put("source", source);
                        subPayload.put("destination", destination);
                        if (bastianIP != null) {
                            subPayload.put("bastian-ip", bastianIP);
                        }
                        payload.put("data", subPayload);
                        Response execute = Request.Post(downloadPath)
                                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .setHeader(HttpHeaders.AUTHORIZATION,
                                        "Basic " + Base64.getEncoder().encodeToString(
                                                StringUtil.concatStrings(this.getTinkererUserName(),
                                                        ":", this.getTinkererPassword())
                                                        .getBytes(Charset.defaultCharset())))
                                .bodyString(gson.toJson(payload), ContentType.APPLICATION_JSON)
                                .execute();
                        //check if REST api call was successful
                        HttpResponse httpResponse = execute.returnResponse();
                        if (httpResponse.getCode() != HttpStatus.SC_OK) {
                            throw new TinkererOperationException("Error occurred while" +
                                    " performing tinkerer REST api call for log download." +
                                    "\nError message :"
                                    + EntityUtils.toString(((CloseableHttpResponse) httpResponse).getEntity()));
                        }
                        //verify the downloaded files are present in the location
                        if (!verifyFileDownload(destination)) {
                            throw new TinkererOperationException("Failed to download the file :"
                                    + destination);
                        }
                        logger.debug("Download Location :" + destination);
                    } catch (IOException e) {
                        throw new TinkererOperationException("Error occurred while downloading " +
                                "the log file location : " + logFileName
                                + "\nfrom agent :" + agent.getAgentId()
                                + "\nRunning on instance : " + agent.getInstanceName()
                                + "\nfor test plan :" + testPlan.getId(), e);
                    } catch (ParseException e) {
                        throw new TinkererOperationException("Error occurred while parsing the " +
                                "response from download " +
                                "logfile operation : " + logFileName
                                + "\nfrom agent :" + agent.getAgentId()
                                + "\nRunning on instance : " + agent.getInstanceName()
                                + "\nfor test plan :" + testPlan.getId(), e);
                    } catch (TestGridException e) {
                        throw new TinkererOperationException("Error occurred deriving the destination path " +
                                  "for logfile : " + logFileName
                                + "\nfrom agent :" + agent.getAgentId()
                                + "\nRunning on instance : " + agent.getInstanceName()
                                + "\nfor test plan :" + testPlan.getId(), e);
                    }
                }
                logger.info("Successfully downloaded all log files ");
            }
        }
        if (deploymentCreationResult.getAgents().size() == 0) {
            logger.warn("No registered agents found!");
        }
    }

    /**
     * The static inner class used as a type reference for parsing the json response
     * to a map of strings.
     */
    private static class PropertyType extends TypeToken<HashMap<String, String>> {
    }

    /**
     * This method verifies if the specified file is present in the file system
     *
     * @param fileLocation path of the file
     * @return true if exists, otherwise false
     */
    private boolean verifyFileDownload(String fileLocation) {
        Path path = Paths.get(fileLocation);
        return Files.exists(path) && Files.isRegularFile(path);
    }
}

