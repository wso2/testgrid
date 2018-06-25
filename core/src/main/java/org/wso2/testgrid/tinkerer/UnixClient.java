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
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.core5.http.ContentType;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.tinkerer.exception.TinkererOperationException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
 *
 */
public class UnixClient extends TinkererClient {

    private static final Logger logger = LoggerFactory.getLogger(UnixClient.class);
    private static final String SCENARIO_LOG_LOCATION = "/repository/logs/";
    private static final String INTEGRATION_LOG_LOCATION = "/logs";

    @Override
    public void downloadLogs(DeploymentCreationResult deploymentCreationResult, TestPlan testPlan)
            throws TinkererOperationException {

        String testType = testPlan.getScenarioConfig().getTestType();
        logger.info("TestType is : " + testType);

        try {
            for (Agent agent : deploymentCreationResult.getAgents()) {
                String productBasePath = "";
                String logLocation = "";
                String operationPath = this.getTinkererBase() + "test-plan/" + agent.getTestPlanId()
                        + "/agent/" + agent.getInstanceName() + "/operation";
                if (TestGridConstants.TEST_TYPE_FUNCTIONAL.equals(testType)) {
                    //get the carbon.home from the instance where the product is running
                    Response execute = Request.Post(operationPath).setHeader
                            (HttpHeaders.CONTENT_TYPE, "application/json")
                            .setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder()
                                    .encodeToString(StringUtil.concatStrings(this.getTinkererUserName(),
                                            ":", this.getTinkererPassword()).getBytes(Charset.defaultCharset())))
                            .bodyString("{\"code\":\"SHELL\",\"request\":\"ps -ef |grep 'carbon.home'\"}",
                                    ContentType.APPLICATION_JSON).execute();

                    String content = execute.returnContent().toString();
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
                    for (Host host : deploymentCreationResult.getHosts()) {
                        if (host.getLabel().equals("workspace")) {
                            productBasePath = host.getIp();
                            break;
                        }
                    }
                    logLocation = productBasePath + INTEGRATION_LOG_LOCATION;
                }
                logger.info("Product base path found " + productBasePath);
                Response logFiles = Request.Post(operationPath).setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .setHeader(HttpHeaders.AUTHORIZATION,
                                "Basic " + Base64.getEncoder().encodeToString(
                                        StringUtil.concatStrings(this.getTinkererUserName(),
                                                ":", this.getTinkererPassword()).getBytes(Charset.defaultCharset())))
                        .bodyString("{\"code\":\"SHELL\",\"request\":\"ls " + logLocation + "\"}",
                                ContentType.APPLICATION_JSON).execute();
                Map<String, String> resultMap = new Gson().fromJson(logFiles.returnContent().asString(),
                        new PropertyType().getType());
                if (resultMap.containsKey("response")) {
                    String response = resultMap.get("response");
                    List<String> logFileNames = Arrays.asList(response.split("\n"));

                    for (String logFileName : logFileNames) {
                        logger.info("Downloading log file : " + logFileName);
                        String key = Base64.getEncoder()
                                .encodeToString(FileUtils.readFileToByteArray(new File(testPlan.getKeyFileLocation())));
                        String source = logLocation + logFileName;
                        String destination = TestGridUtil.getTestGridHomePath() + File.separator +
                                testPlan.getDeploymentPattern().getProduct().getName() + logFileName;
                        String bastianIP = deploymentCreationResult.getBastianIP();
                        //TODO set correct download path so the log files are in the correct place in the folder hierarchy
                        String downloadPath = this.getTinkererBase() + "test-plan/" + agent.getTestPlanId() +
                                "/agent/" + agent.getInstanceName() + "/stream-file";
                        Request.Post(downloadPath)
                                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .bodyString("{" +
                                                "\"code\":\"STREAM_FILE\"," +
                                                "\"data\":{\"key\":\"" + key + "\"," +
                                                "\"source\":\"" + source + "\"," +
                                                "\"destination\":\"" + destination + "\"" +
                                                (bastianIP != null ? ",\"bastian-ip\":\"" +
                                                        bastianIP + "\"" : "") + "}}"
                                        , ContentType.APPLICATION_JSON)
                                .execute();
                        logger.debug("Download Location :" + destination);
                    }
                    logger.info("Successfully downloaded all log files ");
                }
            }
        } catch (IOException e) {
            throw new TinkererOperationException("Error occurred while executing the Tinkerer operation ", e);
        }
    }

    /**
     * The static inner class used as a type reference for parsing the json response
     * to a map of strings
     */
    private static class PropertyType extends TypeToken<HashMap<String, String>> {}
}

