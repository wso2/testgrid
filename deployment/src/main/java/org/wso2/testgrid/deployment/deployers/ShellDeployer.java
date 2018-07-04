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
package org.wso2.testgrid.deployment.deployers;

import com.google.gson.Gson;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.ShellExecutor;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.deployment.DeploymentUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.wso2.testgrid.common.TestGridConstants.WORKSPACE;

/**
 * This class performs Shell related deployment tasks.
 *
 * @since 1.0.0
 */
public class ShellDeployer implements Deployer {

    private static final Logger logger = LoggerFactory.getLogger(ShellDeployer.class);
    private static final String DEPLOYER_NAME = "SHELL";

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    @Override
    public DeploymentCreationResult deploy(TestPlan testPlan,
            InfrastructureProvisionResult infrastructureProvisionResult)
            throws TestGridDeployerException {

        DeploymentConfig.DeploymentPattern deploymentPatternConfig = testPlan.getDeploymentConfig()
                .getDeploymentPatterns().get(0);
        logger.info("Performing the Deployment " + deploymentPatternConfig.getName());
        final Properties inputParameters;
        try {
            Script deployment = getScriptToExecute(testPlan.getDeploymentConfig(), Script.Phase.CREATE);
            logger.info("Performing the Deployment " + deployment.getName());
            String infraArtifact = StringUtil
                    .concatStrings(infrastructureProvisionResult.getResultLocation(),
                            File.separator, "k8s.properties");
            inputParameters = getInputParameters(testPlan, deployment);
            String parameterString = TestGridUtil.getParameterString(infraArtifact, inputParameters);
            ShellExecutor shellExecutor = new ShellExecutor(Paths.get(TestGridUtil.getTestGridHomePath()));

            int exitCode = shellExecutor
                    //TODO: Remove the usages of InfrastructureProvisionResult#getDeploymentScriptsDir.
                    .executeCommand("bash " + Paths.get(infrastructureProvisionResult
                            .getDeploymentScriptsDir(), deployment.getFile()) + " " + parameterString);
            if (exitCode > 0) {
                logger.error(StringUtil.concatStrings("Error occurred while executing the deploy-provision script. ",
                        "Script exited with a status code of ", exitCode));
                DeploymentCreationResult result = new DeploymentCreationResult();
                result.setName(deploymentPatternConfig.getName());
                result.setSuccess(false);
                return result;
            }
        } catch (CommandExecutionException e) {
            throw new TestGridDeployerException(e);
        }
        DeploymentCreationResult result = DeploymentUtil
                .getDeploymentCreationResult(inputParameters.getProperty(WORKSPACE));
        result.setName(deploymentPatternConfig.getName());

        List<Host> hosts = new ArrayList<>();
        Host tomcatHost = new Host();
        tomcatHost.setLabel("tomcatHost");
        tomcatHost.setIp("ec2-34-204-80-18.compute-1.amazonaws.com");
        Host tomcatPort = new Host();
        tomcatPort.setLabel("tomcatPort");
        tomcatPort.setIp("8080");
        hosts.add(tomcatHost);
        hosts.add(tomcatPort);
        hosts.addAll(result.getHosts());

        DeploymentCreationResult deploymentCreationResult = new DeploymentCreationResult();
        deploymentCreationResult.setName(deploymentPatternConfig.getName());
        deploymentCreationResult.setHosts(hosts);
        //store bastian ip
        Optional<Host> bastionEIP = infrastructureProvisionResult.getHosts()
                .stream().filter(host -> host.getLabel().equals(TestGridConstants.OUTPUT_BASTIAN_IP)).findFirst();
        bastionEIP.ifPresent(host -> deploymentCreationResult.setBastianIP(host.getIp()));

        //Call the rest api of tinkerer and get the agents for current test plan
        try {
            String tinkererEndpoint = ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties
                    .DEPLOYMENT_TINKERER_REST_BASE_PATH);
            String username = ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties
                    .DEPLOYMENT_TINKERER_USERNAME);
            String password = ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties
                    .DEPLOYMENT_TINKERER_PASSWORD);
            String agentsPath = tinkererEndpoint + "test-plan/" + testPlan.getId() + "/agents";
            Response execute = Request.Get(agentsPath)
                    .setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(
                            StringUtil.concatStrings(username, ":", password).getBytes(
                                    Charset.defaultCharset())))
                    .execute();
            String agentContent = execute.returnContent().toString();
            logger.debug("AgentContent" + agentContent);
            Agent[] agents = new Gson().fromJson(agentContent, Agent[].class);
            for (Agent agent : Arrays.asList(agents)) {
                logger.info("Agent registered : " + agent.getAgentId());
            }
            if (agents.length == 0) {
                logger.warn(String.format("Unable retrieve agents for  test plan with id %s , %n "
                        , testPlan.getId()));
            }
            deploymentCreationResult.setAgents(Arrays.asList(agents));
        } catch (IOException e) {
            logger.warn(String.format("Unable retrieve agents for  test plan with id %s , %n "
                    , testPlan.getId()));
        }
        return deploymentCreationResult;
    }

    private Properties getInputParameters(TestPlan testPlan, Script deployment) {
        try {
            Path workspace = TestGridUtil.getTestRunWorkspace(testPlan, false);
            final Properties inputParameters = deployment.getInputParameters();
            inputParameters.setProperty(WORKSPACE, workspace.toString());
            return inputParameters;
        } catch (TestGridException e) {
            logger.info("Error while reading input parameters for deployment. Using empty properties. Error: " +
                    e.getMessage(), e);
            return new Properties();
        }
    }

    /**
     * This method returns the script matching the correct script phase.
     *
     * @param deploymentConfig {@link DeploymentConfig} object with current deployment configurations
     * @param scriptPhase      {@link Script.Phase} enum value for required script
     * @return the matching script from deployment configuration
     * @throws TestGridDeployerException if there is no matching script for phase defined
     */
    private Script getScriptToExecute(DeploymentConfig deploymentConfig, Script.Phase scriptPhase)
            throws TestGridDeployerException {

        for (Script script : deploymentConfig.getDeploymentPatterns().get(0).getScripts()) {
            if (scriptPhase.equals(script.getPhase())) {
                return script;
            }
        }
        if (Script.Phase.CREATE.equals(scriptPhase)) {
            for (Script script : deploymentConfig.getDeploymentPatterns().get(0).getScripts()) {
                if (script.getPhase() == null) {
                    return script;
                }
            }
        }
        throw new TestGridDeployerException("The Script list Provided doesn't containt a " + scriptPhase.toString() +
                "Type script to succesfully complete the execution!");
    }
}
