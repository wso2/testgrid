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
import com.sun.javafx.fxml.PropertyNotFoundException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.*;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.deployment.DeploymentUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * This class performs Shell related deployment tasks.
 *
 * @since 1.0.0
 */
public class KubernetesDeployer implements Deployer {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesDeployer.class);
    private static final String DEPLOYER_NAME = "KUBERNETES";

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    @Override
    public DeploymentCreationResult deploy(TestPlan testPlan,
                                           InfrastructureProvisionResult infrastructureProvisionResult)
            throws TestGridDeployerException {
        setProperties(testPlan);
        DeploymentConfig.DeploymentPattern deploymentPatternConfig = testPlan.getDeploymentConfig()
                .getDeploymentPatterns().get(0);
        logger.info("Performing the Deployment " + deploymentPatternConfig.getName());
        String deplInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                .toAbsolutePath().toString();
        String deplOutputsLoc = DataBucketsHelper.getOutputLocation(testPlan).toString();

        try {
            Script deployment = getScriptToExecute(testPlan.getDeploymentConfig(), Script.Phase.CREATE);
            String deployScriptLocation = Paths.get(testPlan.getDeploymentRepository()).toString();
            logger.info("Performing the Deployment " + deployment.getName());

            ShellExecutor executor = new ShellExecutor(Paths.get(deployScriptLocation));
            final String command = "bash " + Paths.get(deployScriptLocation, TestGridConstants.DEPLOY_SCRIPT)
                    + " --input-dir " + deplInputsLoc + " --output-dir " + deplOutputsLoc;
            int exitCode = executor.executeCommand(command);
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
        DeploymentCreationResult result = DeploymentUtil.getDeploymentCreationResult(deplInputsLoc);
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
    private void setProperties(TestPlan testplan) throws TestGridDeployerException{

        String WUM_USERNAME = null;
        String WUM_PASSWORD = null;
        Script deployment = getScriptToExecute(testplan.getDeploymentConfig(), Script.Phase.CREATE);
        String deployScriptLocation = Paths.get(testplan.getDeploymentRepository()).toString();

        String scriptPath=Paths.get(deployScriptLocation, deployment.getFile()).toString();

        final Path location = DataBucketsHelper.getInputLocation(testplan)
                .resolve(DataBucketsHelper.INFRA_OUT_FILE);
        logger.info(location.toString());
        logger.info(location.toString());
        try{
            WUM_USERNAME=ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.WUM_USERNAME);
            WUM_PASSWORD=ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.WUM_PASSWORD);
        }catch(PropertyNotFoundException e){
            logger.error("properties are not found"); }

        DeploymentConfig.DeploymentPattern deploymentPatternConfig = testplan.getDeploymentConfig()
                .getDeploymentPatterns().get(0);


        try (OutputStream os = Files.newOutputStream(location, CREATE, APPEND)) {
            os.write(("\nscript="+scriptPath).getBytes(StandardCharsets.UTF_8));
            os.write(("\nname="+deploymentPatternConfig.getName()).getBytes(StandardCharsets.UTF_8));
            os.write(("\n" +TestGridConstants.WUM_USERNAME_PROPERTY + "=" + WUM_USERNAME).getBytes(StandardCharsets.UTF_8));
            os.write(("\n" +TestGridConstants.WUM_PASSWORD_PROPERTY + "=" + WUM_PASSWORD+"\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + location, e);
        }

    }

}