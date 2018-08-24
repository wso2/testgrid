package org.wso2.testgrid.common.util;

import com.google.gson.Gson;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.config.ConfigurationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This class is to handle tinkerer commands
 */
public class TinkererCommands {

    private static final Logger logger = LoggerFactory.getLogger(TinkererCommands.class);

    static String tinkererHost = ConfigurationContext.getProperty(
            ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH);
    // Get authentication detail from config.properties and encode with BASE64
    static String authenticationString = ConfigurationContext.getProperty(
            ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_USERNAME) + ":" +
            ConfigurationContext.getProperty(
                    ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_PASSWORD);
    static String authenticationToken = "Basic " + Base64.getEncoder().encodeToString(
            authenticationString.getBytes(StandardCharsets.UTF_8));

    public static Agent[] getAgents(String testPlanid) {

        String agentLink = tinkererHost + "test-plan/" + testPlanid + "/agents";

        try {
            // Get list of agent for given test plan id
            Content agentResponse = Request.Get(agentLink)
                    .addHeader(HttpHeaders.AUTHORIZATION, authenticationToken).
                            execute().returnContent();

            Agent[] agents = new Gson().fromJson(agentResponse.asString(), Agent[].class);

            // Execute command on found agents
            return agents;
        } catch (IOException e) {
            logger.error("Error in API call request to get Agent list ".concat(agentLink), e);
            return new Agent[0];
        } catch (Exception e) {
            logger.error("Error in API call request to execute script ".concat(tinkererHost), e);
            return new Agent[0];
        }

    }

    /**
     * Send a shell command to the agent and execute it
     *
     * @param agent                 Agent details
     * @param script                Script that need to be executed
     * @return                      Shell execution output
     * @throws Exception          Request post exception
     */
    public static Content sendShellCommand(Agent agent, String script)
            throws Exception {
        Content result;

        String requestLink = tinkererHost + "test-plan/" + agent.getTestPlanId() + "/agent/" + agent.getInstanceName()
                + "/operation";
        logger.info(requestLink);
        try {
            result = Request.Post(requestLink).addHeader(HttpHeaders.AUTHORIZATION, authenticationToken)
                    .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyString("{\"request\":\"" + script + "\",\"code\":\"SHELL\"}", ContentType.APPLICATION_JSON).
                            execute().returnContent();
        } catch (IOException e) {
            throw new Exception(StringUtil.concatStrings(
                    "Send api request to the agent error occur for the ", requestLink, agent.getAgentId()), e);
        }
        return result;
    }
}
