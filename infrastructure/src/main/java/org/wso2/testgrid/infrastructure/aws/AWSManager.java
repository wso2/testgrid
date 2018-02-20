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
package org.wso2.testgrid.infrastructure.aws;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.TemplateParameter;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateResult;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import com.amazonaws.waiters.WaiterUnrecoverableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.infrastructure.CloudFormationScriptPreprocessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is responsible for creating the AWS infrastructure.
 *
 * @since 1.0.0
 */
public class AWSManager {

    private static final Logger logger = LoggerFactory.getLogger(AWSManager.class);
    private static final String AWS_REGION_PARAMETER = "region";
    private static final String WUM_USERNAME = "WUMUsername";
    private static final String WUM_PASSWORD = "WUMPassword";
    private static final String DB_ENGINE = "DBEngine";
    private static final String DB_ENGINE_VERSION = "DBEngineVersion";
    private static final String JDK = "JDK";
    private static final String IMAGE = "Image";
    private InfrastructureConfig infrastructureConfig;

    /**
     * This constructor creates AWS deployer object and validate AWS related environment variables are present.
     *
     * @param awsKeyVariableName    Environment variable name for AWS ACCESS KEY.
     * @param awsSecretVariableName Environment variable name for AWS SECRET KEY.
     * @throws TestGridInfrastructureException Throws exception when environment variables are not set.
     */
    public AWSManager(String awsKeyVariableName, String awsSecretVariableName) throws TestGridInfrastructureException {
        String awsIdentity = System.getenv(awsKeyVariableName);
        String awsSecret = System.getenv(awsSecretVariableName);
        if (StringUtil.isStringNullOrEmpty(awsIdentity) || StringUtil.isStringNullOrEmpty(awsSecret)) {
            throw new TestGridInfrastructureException(StringUtil
                    .concatStrings("AWS Credentials must be set as environment variables: ", awsIdentity, ", ",
                            awsSecretVariableName));
        }
    }

    /**
     * This method initiates creating infrastructure through CLoudFormation.
     *
     * @param script       Script object containing the CF details.
     * @param infraRepoDir Path of TestGrid home location in file system as a String.
     * @return Returns a  Deployment object with created infrastructure details.
     * @throws TestGridInfrastructureException When there is an error with CloudFormation script.
     */
    public Deployment createInfrastructure(Script script, String infraRepoDir) throws TestGridInfrastructureException {
        String cloudFormationName = script.getName();
        String region = this.infrastructureConfig.getParameters().getProperty(AWS_REGION_PARAMETER);
        AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(region)
                .build();

        CreateStackRequest stackRequest = new CreateStackRequest();
        stackRequest.setStackName(cloudFormationName);
        CloudFormationScriptPreprocessor cfScriptPreprocessor = new CloudFormationScriptPreprocessor();
        try {
            String file = new String(Files.readAllBytes(Paths.get(infraRepoDir,
                    script.getFile())), StandardCharsets.UTF_8);
            file = cfScriptPreprocessor.process(file);
            ValidateTemplateRequest validateTemplateRequest = new ValidateTemplateRequest();
            validateTemplateRequest.withTemplateBody(file);
            ValidateTemplateResult validationResult = cloudFormation.validateTemplate(validateTemplateRequest);
            List<TemplateParameter> expectedParameters = validationResult.getParameters();

            stackRequest.setTemplateBody(file);
            stackRequest.setParameters(getParameters(script, expectedParameters));

            logger.info(StringUtil.concatStrings("Creating CloudFormation Stack '", cloudFormationName,
                    "' in region '", region, "'. Script : ", script.getFile()));
            CreateStackResult stack = cloudFormation.createStack(stackRequest);
            if (logger.isDebugEnabled()) {
                logger.info(StringUtil.concatStrings("Stack configuration created for name ", cloudFormationName));
            }
            logger.info(StringUtil.concatStrings("Waiting for stack : ", cloudFormationName));
            Waiter<DescribeStacksRequest> describeStacksRequestWaiter = new AmazonCloudFormationWaiters(cloudFormation)
                    .stackCreateComplete();
            describeStacksRequestWaiter.run(new WaiterParameters<>(new DescribeStacksRequest()));
            logger.info(StringUtil.concatStrings("Stack : ", cloudFormationName, ", with ID : ", stack.getStackId(),
                    " creation completed !"));

            DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
            describeStacksRequest.setStackName(stack.getStackId());
            DescribeStacksResult describeStacksResult = cloudFormation
                    .describeStacks(describeStacksRequest);
            List<Host> hosts = new ArrayList<>();
            Host tomcatHost = new Host();
            tomcatHost.setLabel("tomcatHost");
            tomcatHost.setIp("ec2-52-54-230-106.compute-1.amazonaws.com");
            Host tomcatPort = new Host();
            tomcatPort.setLabel("tomcatPort");
            tomcatPort.setIp("8080");
            hosts.add(tomcatHost);
            hosts.add(tomcatPort);
            for (Stack st : describeStacksResult.getStacks()) {
                for (Output output : st.getOutputs()) {
                    Host host = new Host();
                    host.setIp(output.getOutputValue());
                    host.setLabel(output.getOutputKey());
                    hosts.add(host);
                }
            }
            logger.info("Created a CloudFormation Stack with the name :" + stackRequest.getStackName());
            Deployment deployment = new Deployment();
            deployment.setHosts(hosts);
            return deployment;
        } catch (WaiterUnrecoverableException e) {
            throw new TestGridInfrastructureException(StringUtil.concatStrings("Error while waiting for stack : "
                    , cloudFormationName, " to complete"), e);
        } catch (IOException e) {
            throw new TestGridInfrastructureException("Error occurred while Reading CloudFormation script", e);
        }
    }

    /**
     * Initialize the manager with an infrastructure object.
     *
     * @param infrastructureConfig infrastructure details.
     */
    public void init(InfrastructureConfig infrastructureConfig) {
        this.infrastructureConfig = infrastructureConfig;
    }

    /**
     * This method destroys the CF infrastructure given the stack name.
     *
     * @param script Script object with the CloudFormation details.
     * @return true or false to indicate the result of destroy operation.
     * @throws TestGridInfrastructureException when AWS error occurs in deletion process.
     * @throws InterruptedException            when there is an interruption while waiting for the result.
     */
    public boolean destroyInfrastructure(Script script) throws TestGridInfrastructureException, InterruptedException {
        String cloudFormationName = script.getName();
        AmazonCloudFormation stackdestroy = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(infrastructureConfig.getParameters().getProperty(AWS_REGION_PARAMETER))
                .build();
        DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
        deleteStackRequest.setStackName(cloudFormationName);
        stackdestroy.deleteStack(deleteStackRequest);
        logger.info(StringUtil.concatStrings("Waiting for stack : ", cloudFormationName, " to delete.."));
        Waiter<DescribeStacksRequest> describeStacksRequestWaiter = new
                AmazonCloudFormationWaiters(stackdestroy).stackDeleteComplete();
        try {
            describeStacksRequestWaiter.run(new WaiterParameters<>(new DescribeStacksRequest()));
        } catch (WaiterUnrecoverableException e) {
            throw new TestGridInfrastructureException("Error occured while waiting for Stack :"
                    + cloudFormationName + " deletion !");
        }
        return true;
    }

    /**
     * Reads the parameters for the stack from file.
     *
     * @param script             Script object with script details.
     * @param expectedParameters
     * @return a List of {@link Parameter} objects
     * @throws IOException When there is an error reading the parameters file.
     */
    private List<Parameter> getParameters(Script script, List<TemplateParameter> expectedParameters)
            throws IOException, TestGridInfrastructureException {

        List<Parameter> cfCompatibleParameters = new ArrayList<>();

        expectedParameters.forEach(expected -> {
            Optional<Map.Entry<Object, Object>> scriptParameter = script.getInputParameters().entrySet().stream()
                    .filter(input -> input.getKey().equals(expected
                            .getParameterKey())).findAny();
            scriptParameter.ifPresent(theScriptParameter -> {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue((String) theScriptParameter.getValue());
                cfCompatibleParameters.add(awsParameter);
            });

        });

        return cfCompatibleParameters;
    }

    private enum AWSRDSEngine {
        MYSQL("mysql"), ORACLE("oracle-se"), SQL_SERVER("sqlserver-ex"), POSTGRESQL("postgres"),
        MariaDB("mariadb");

        private final String name;

        AWSRDSEngine(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }
}
