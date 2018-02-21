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
package org.wso2.testgrid.infrastructure.providers;

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
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestPlan;
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
import java.util.Properties;

/**
 * This class provides the infrastructure from amazon web services (AWS).
 *
 * @since 1.0.0
 */
public class AWSProvider implements InfrastructureProvider {

    public static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    public static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
    private static final String AWS_PROVIDER = "AWS";
    private static final Logger logger = LoggerFactory.getLogger(AWSProvider.class);
    private static final String AWS_REGION_PARAMETER = "region";
    private CloudFormationScriptPreprocessor cfScriptPreprocessor;

    @Override
    public String getProviderName() {
        return AWS_PROVIDER;
    }

    @Override
    public boolean canHandle(InfrastructureConfig infrastructureConfig) {
        //Check if scripts has a cloud formation script.
        boolean isAWS =
                infrastructureConfig.getInfrastructureProvider() == InfrastructureConfig.InfrastructureProvider.AWS;
        boolean isCFN = infrastructureConfig.getIacProvider() == InfrastructureConfig.IACProvider.CLOUDFORMATION;
        return isAWS && isCFN;

    }

    @Override
    public void init() throws TestGridInfrastructureException {
        String awsIdentity = System.getenv(AWS_ACCESS_KEY_ID);
        String awsSecret = System.getenv(AWS_SECRET_ACCESS_KEY);
        if (StringUtil.isStringNullOrEmpty(awsIdentity) || StringUtil.isStringNullOrEmpty(awsSecret)) {
            throw new TestGridInfrastructureException(StringUtil
                    .concatStrings("AWS Credentials must be set as environment variables: ", AWS_ACCESS_KEY_ID, ", ",
                            AWS_SECRET_ACCESS_KEY));
        }
        cfScriptPreprocessor = new CloudFormationScriptPreprocessor();
    }

    /**
     * This method initiates creating infrastructure through CLoudFormation.
     *
     * @param testPlan {@link TestPlan} with current test run specifications
     * @return Returns the InfrastructureProvisionResult.
     * @throws TestGridInfrastructureException When there is an error with CloudFormation script.
     */
    @Override
    public InfrastructureProvisionResult provision(TestPlan testPlan)
            throws TestGridInfrastructureException {
        InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
        for (Script script : infrastructureConfig.getProvisioners().get(0).getScripts()) {
            if (script.getType().equals(Script.ScriptType.CLOUDFORMATION)) {
                infrastructureConfig.getParameters().forEach((key, value) ->
                        script.getInputParameters().setProperty((String) key, (String) value));
                return doProvision(infrastructureConfig, script.getName(), testPlan.getInfraRepoDir());
            }
        }
        throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
    }

    @Override
    public boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir)
            throws TestGridInfrastructureException {
        try {
            for (Script script : infrastructureConfig.getProvisioners().get(0).getScripts()) {
                if (script.getType().equals(Script.ScriptType.CLOUDFORMATION)) {
                    return doRelease(infrastructureConfig, script.getName());
                }
            }
            throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
        } catch (InterruptedException e) {
            throw new TestGridInfrastructureException("Error while waiting for CloudFormation stack to destroy", e);
        }
    }

    private InfrastructureProvisionResult doProvision(InfrastructureConfig infrastructureConfig,
            String stackName, String infraRepoDir) throws TestGridInfrastructureException {
        String region = infrastructureConfig.getParameters().getProperty(AWS_REGION_PARAMETER);
        AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(region)
                .build();

        CreateStackRequest stackRequest = new CreateStackRequest();
        stackRequest.setStackName(stackName);
        try {
            Script script = infrastructureConfig.getProvisioners().get(0).getScripts().stream()
                    .filter(s -> s.getName().equals(stackName))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "The script with name '" + stackName + "' does not exist."));
            String file = new String(Files.readAllBytes(Paths.get(infraRepoDir, script.getFile())),
                    StandardCharsets.UTF_8);
            file = cfScriptPreprocessor.process(file);
            ValidateTemplateRequest validateTemplateRequest = new ValidateTemplateRequest();
            validateTemplateRequest.withTemplateBody(file);
            ValidateTemplateResult validationResult = cloudFormation.validateTemplate(validateTemplateRequest);
            List<TemplateParameter> expectedParameters = validationResult.getParameters();

            stackRequest.setTemplateBody(file);
            stackRequest.setParameters(getParameters(script, expectedParameters));

            logger.info(StringUtil.concatStrings("Creating CloudFormation Stack '", stackName,
                    "' in region '", region, "'. Script : ", script.getFile()));
            CreateStackResult stack = cloudFormation.createStack(stackRequest);
            if (logger.isDebugEnabled()) {
                logger.info(StringUtil.concatStrings("Stack configuration created for name ", stackName));
            }
            logger.info(StringUtil.concatStrings("Waiting for stack : ", stackName));
            Waiter<DescribeStacksRequest> describeStacksRequestWaiter = new AmazonCloudFormationWaiters(cloudFormation)
                    .stackCreateComplete();
            describeStacksRequestWaiter.run(new WaiterParameters<>(new DescribeStacksRequest()));
            logger.info(StringUtil.concatStrings("Stack : ", stackName, ", with ID : ", stack.getStackId(),
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
            InfrastructureProvisionResult result = new InfrastructureProvisionResult();
            //added for backward compatibility. todo remove.
            result.setHosts(hosts);
            Properties props = new Properties();
            props.setProperty("HOSTS", hosts.toString());
            result.setProperties(props);

            return result;
        } catch (WaiterUnrecoverableException e) {
            throw new TestGridInfrastructureException(StringUtil.concatStrings("Error while waiting for stack : "
                    , stackName, " to complete"), e);
        } catch (IOException e) {
            throw new TestGridInfrastructureException("Error occurred while Reading CloudFormation script", e);
        }
    }

    /**
     * This method releases the provisioned AWS infrastructure.
     *
     * @param infrastructureConfig The infrastructure configuration.
     * @param stackName            the cloudformation script name
     * @return true or false to indicate the result of destroy operation.
     * @throws TestGridInfrastructureException when AWS error occurs in deletion process.
     * @throws InterruptedException            when there is an interruption while waiting for the result.
     */
    private boolean doRelease(InfrastructureConfig infrastructureConfig, String stackName) throws
            TestGridInfrastructureException, InterruptedException {
        AmazonCloudFormation stackdestroy = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(infrastructureConfig.getParameters().getProperty(AWS_REGION_PARAMETER))
                .build();
        DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
        deleteStackRequest.setStackName(stackName);
        stackdestroy.deleteStack(deleteStackRequest);
        logger.info(StringUtil.concatStrings("Waiting for stack : ", stackName, " to delete.."));
        Waiter<DescribeStacksRequest> describeStacksRequestWaiter = new
                AmazonCloudFormationWaiters(stackdestroy).stackDeleteComplete();
        try {
            describeStacksRequestWaiter.run(new WaiterParameters<>(new DescribeStacksRequest()));
        } catch (WaiterUnrecoverableException e) {
            throw new TestGridInfrastructureException("Error occured while waiting for Stack :"
                    + stackName + " deletion !");
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

}
