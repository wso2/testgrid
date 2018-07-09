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

package org.wso2.testgrid.core.configchangeset;

import com.google.gson.Gson;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.ConfigChangeSet;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.core.TestPlanExecutor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * This class execute commands for applying config change set on UNIX machines
 *
 */
public class ConfigChangeSetExecutorUnix extends ConfigChangeSetExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestPlanExecutor.class);
    private static final String PRODUCT_HOME_ENV = "PRODUCT_HOME";

    /**
     * Apply config change set script before and after run test scenarios
     *
     * @param testPlan the test plan
     * @param configChangeSet   config change set
     * @param isInit run apply config-script if true. else, run revert-config script
     * @return execution passed or failed
     */
    @Override
    public boolean applyConfigChangeSet(TestPlan testPlan, ConfigChangeSet configChangeSet,
                                        DeploymentCreationResult deploymentCreationResult, boolean isInit) {
        try {
            URL configChangeSetRepoPath = new URL(testPlan.getConfigChangeSetRepository());
            String filePath = configChangeSetRepoPath.getPath();
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            String configChangeSetLocation = "./repos/" + fileName + "-master/config-sets/" + configChangeSet.getName();
            List<String> applyShellCommand = new ArrayList<String>();
            // Generate array of commands that need to be applied
            if (isInit) {
                String productHomePath = System.getenv(PRODUCT_HOME_ENV);
                if (productHomePath == null) {
                    logger.warn(PRODUCT_HOME_ENV +
                            "  environment variable is not set. JMeter test executions may fail.");
                } else {
                    configChangeSetLocation = "export productHome=" +
                            productHomePath + " && " + configChangeSetLocation;
                }
                for (Host host : deploymentCreationResult.getHosts()) {
                    configChangeSetLocation = "export " + host.getLabel() + "=" + host.getIp() + " && " +
                            configChangeSetLocation;
                }
                // run if config-init.sh exist
                applyShellCommand.add(configChangeSetLocation.concat("/config-init.sh &>/dev/null"));
                applyShellCommand.add(configChangeSetLocation.concat("/apply-config.sh &>/dev/null"));
            } else {
                applyShellCommand.add(configChangeSetLocation.concat("/revert-config.sh &>/dev/null"));
            }

            return applyShellCommandOnAgent(testPlan, applyShellCommand);
        } catch (MalformedURLException e) {
            logger.warn("Error parsing scenario repository path for ".
                    concat(testPlan.getConfigChangeSetRepository()), e);
            return false;
        }
    }

    /**
     * Initialize agent before running config change set
     *
     * @param testPlan  The test plan
     * @return          True if execution success. Else, false
     */
    @Override
    public boolean initConfigChangeSet(TestPlan testPlan) {
        try {
            URL configChangeSetRepoPath = new URL(testPlan.getConfigChangeSetRepository());
            String filePath = configChangeSetRepoPath.getPath();
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            List<String> initShellCommand = new ArrayList<>();
            initShellCommand.add("mkdir repos");
            initShellCommand.add("cd repos && curl -LJO " +
                    testPlan.getConfigChangeSetRepository() + "/archive/master.tar.gz   &>/dev/null");
            initShellCommand.add("cd repos && tar xvzf " + fileName + "-master.tar.gz  &>/dev/null");
            initShellCommand.add("chmod -R 755 repos/" + fileName + "-master/config-sets/ &>/dev/null");
            return applyShellCommandOnAgent(testPlan, initShellCommand);
        } catch (MalformedURLException e) {
            logger.warn("Error parsing scenario repository path for ".
                    concat(testPlan.getConfigChangeSetRepository()), e);
            return false;
        }
    }

    /**
     * Revert back changes did in initConfigChangeSet
     *
     * @param testPlan  The test plan
     * @return          True if execution success. Else, false
     */
    @Override
    public boolean deInitConfigChangeSet(TestPlan testPlan) {
        List<String> deInitShellCommand = new ArrayList<>();
        deInitShellCommand.add("rm -rf repos");
        return applyShellCommandOnAgent(testPlan, deInitShellCommand);
    }


    /**
     * Send an array of shell command to an agent
     *
     * @param testPlan          The test plan
     * @param initShellCommand  List of shell command
     * @return  True if execution success. Else, false
     */
    private boolean applyShellCommandOnAgent(TestPlan testPlan, List<String> initShellCommand) {
        String tinkererHost = ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH);
        // Get authentication detail from config.properties and encode with BASE64
        String authenticationString = ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_USERNAME) + ":" +
                ConfigurationContext.getProperty(
                        ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_PASSWORD);
        String authenticationToken = "Basic " + Base64.getEncoder().encodeToString(
                authenticationString.getBytes(StandardCharsets.UTF_8));
        try {
            // Get list of agent for given test plan id
            Content agentResponse = Request.Get(tinkererHost + "test-plan/" + testPlan.getId() + "/agents")
                    .addHeader(HttpHeaders.AUTHORIZATION, authenticationToken).
                            execute().returnContent();

            Agent[] agents = new Gson().fromJson(agentResponse.asString(), Agent[].class);
            // Execute command on found agents
            for (Agent agent : Arrays.asList(agents)) {
                logger.info("Start sending commands to agent " + agent.getAgentId());
                for (String shellCommand : initShellCommand) {
                    logger.info("Execute: " + shellCommand);
                    Content shellCommandResponse = sendShellCommand(tinkererHost, authenticationToken
                            , agent, shellCommand);
                    logger.info("Agent Response: " + shellCommandResponse.asString());
                }
            }
            return true;
        } catch (IOException e) {
            logger.warn("Error in API call request ".concat(tinkererHost), e);
            return false;
        }
    }

    /**
     * Send a shell command to the agent and execute it
     *
     * @param tinkererHost          Tinkerer host address
     * @param authenticationKey     Tinkerer authentication key as BASE64 encoded string
     * @param agent                 Agent details
     * @param script                Script that need to be executed
     * @return                      Shell execution output
     * @throws IOException          Request post exception
     */
    private Content sendShellCommand(String tinkererHost, String authenticationKey, Agent agent, String script)
            throws IOException {
        return Request.Post(
                tinkererHost + "test-plan/" + agent.getTestPlanId() +
                        "/agent/" + agent.getInstanceName() + "/operation"
        ).addHeader(HttpHeaders.AUTHORIZATION, authenticationKey)
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyString("{\"request\":\"" + script + "\",\"code\":\"SHELL\"}", ContentType.APPLICATION_JSON).
                        execute().returnContent();
    }

}
