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

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
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

import org.awaitility.core.ConditionTimeoutException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TimeOutBuilder;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.infrastructure.DeploymentPatternResourceUsage;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.LambdaExceptionUtils;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.infrastructure.CloudFormationScriptPreprocessor;
import org.wso2.testgrid.infrastructure.providers.aws.AMIMapper;
import org.wso2.testgrid.infrastructure.providers.aws.AWSResourceManager;
import org.wso2.testgrid.infrastructure.providers.aws.StackCreationWaiter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * This class provides the infrastructure from amazon web services (AWS).
 *
 * @since 1.0.0
 */
public class AWSProvider implements InfrastructureProvider {

    private static final String AWS_PROVIDER = "AWS";
    private static final Logger logger = LoggerFactory.getLogger(AWSProvider.class);
    private static final String AWS_REGION_PARAMETER = "region";
    private static final String CUSTOM_USER_DATA = "CustomUserData";
    private CloudFormationScriptPreprocessor cfScriptPreprocessor;
    private static final int TIMEOUT = 30;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;
    private static final int POLL_INTERVAL = 1;
    private static final TimeUnit POLL_UNIT = TimeUnit.MINUTES;
    private static final int REGION_WAIT_TIMEOUT = 12;
    private static final TimeUnit REGION_WAIT_TIMEOUT_UNIT = TimeUnit.HOURS;
    private static final int REGION_WAIT_POLL_INTERVAL = 1;
    private static final TimeUnit REGION_WAIT_POLL_UNIT = TimeUnit.MINUTES;

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
    public void init(TestPlan testPlan) throws TestGridInfrastructureException {
        cfScriptPreprocessor = new CloudFormationScriptPreprocessor();

        //Set default region specified in config.properties to test plan
        testPlan.getInfrastructureConfig().getProvisioners().get(0).getScripts().get(0)
                .getInputParameters().setProperty(AWS_REGION_PARAMETER,
                ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME));
    }

    @Override
    public void cleanup(TestPlan testPlan) throws TestGridInfrastructureException {
        //Do nothing
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
                return doProvision(infrastructureConfig, script, testPlan);
            }
        }
        throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
    }

    @Override
    public boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir,
                           DeploymentPattern deploymentPattern) throws TestGridInfrastructureException {
        try {
            for (Script script : infrastructureConfig.getProvisioners().get(0).getScripts()) {
                if (script.getType().equals(Script.ScriptType.CLOUDFORMATION)) {
                    return doRelease(infrastructureConfig, script.getName(), deploymentPattern);
                }
            }
            throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
        } catch (InterruptedException e) {
            throw new TestGridInfrastructureException("Error while waiting for CloudFormation stack to destroy", e);
        } catch (TestGridDAOException e) {
            throw new TestGridInfrastructureException("Error while updating released resources in the database.", e);
        }
    }

    private InfrastructureProvisionResult doProvision(InfrastructureConfig infrastructureConfig,
        Script script, TestPlan testPlan) throws TestGridInfrastructureException {
        Path configFilePath = TestGridUtil.getConfigFilePath();
        String region = infrastructureConfig.getProvisioners().get(0).getScripts().get(0)
                .getInputParameters().getProperty(AWS_REGION_PARAMETER);
        AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                .withRegion(region)
                .build();

        String stackName = getValidatedStackName(script.getName());
        CreateStackRequest stackRequest = new CreateStackRequest();
        stackRequest.setStackName(stackName);
        StackCreationWaiter stackCreationWaiter = new StackCreationWaiter();
        try {
            Path cfnFilePath = Paths.get(testPlan.getInfrastructureRepository(),
                    script.getFile());
            String file = new String(Files.readAllBytes(cfnFilePath), StandardCharsets.UTF_8);
            file = cfScriptPreprocessor.process(file);
            ValidateTemplateRequest validateTemplateRequest = new ValidateTemplateRequest();
            validateTemplateRequest.withTemplateBody(file);
            ValidateTemplateResult validationResult = cloudFormation.validateTemplate(validateTemplateRequest);
            List<TemplateParameter> expectedParameters = validationResult.getParameters();

            stackRequest.setTemplateBody(file);

            //Get resource requirements for test plan
            AWSResourceManager awsResourceManager = new AWSResourceManager();
            Optional<List<DeploymentPatternResourceUsage>> resourceRequirementsOptional =
                    awsResourceManager.getResourceRequirements(testPlan.getDeploymentPattern());

            boolean isFirstRunOrHashChanged = false;
            if (resourceRequirementsOptional.isPresent() && !resourceRequirementsOptional.get().isEmpty()) {

                //Parse resource properties to get MD5 hash
                String deploymentProps = resourceRequirementsOptional.get().get(0)
                        .getDeploymentPattern().getProperties();
                final JSONObject jsonObject = new JSONObject(deploymentProps);
                String cfnHash = jsonObject.getString(DeploymentPattern.MD5_HASH_PROPERTY);

                // Check if the file has changed using the hash values
                if (cfnHash.equals(TestGridUtil.getHashValue(cfnFilePath))) {
                    TimeOutBuilder regionTimeOutBuilder = new TimeOutBuilder(
                            REGION_WAIT_TIMEOUT, REGION_WAIT_TIMEOUT_UNIT,
                            REGION_WAIT_POLL_INTERVAL, REGION_WAIT_POLL_UNIT);
                    stackCreationWaiter.waitForAvailableRegion(
                            resourceRequirementsOptional.get(), regionTimeOutBuilder);
                    region = stackCreationWaiter.getAvailableRegion();
                    if (region == null) {
                        throw new TestGridInfrastructureException(
                                "Error occurred while waiting for an available region. Region is null");
                    }
                    infrastructureConfig.getProvisioners().get(0)
                            .getScripts().get(0).getInputParameters().setProperty(AWS_REGION_PARAMETER, region);
                    awsResourceManager.allocateResources(resourceRequirementsOptional.get(), region);

                } else {
                    logger.info("Cloudformation file has been changed. " +
                            "Stack will be created in the default region specified in config.properties");
                    isFirstRunOrHashChanged = true;
                }
            } else {
                isFirstRunOrHashChanged = true;
                logger.info("Running the first test plan for the deployment pattern. " +
                        "Stack will be created in the default region");
            }

            final List<Parameter> populatedExpectedParameters = getParameters(script, expectedParameters,
                    infrastructureConfig, testPlan.getId());
            stackRequest.setParameters(populatedExpectedParameters);
            logger.info(StringUtil.concatStrings("Creation of CloudFormation Stack '", stackName,
                    "' in region '", region, "'. Script : ", script.getFile()));
            final long start = System.currentTimeMillis();
            CreateStackResult stack = cloudFormation.createStack(stackRequest);
            if (logger.isDebugEnabled()) {
                logger.info(StringUtil.concatStrings("Stack configuration created for name ", stackName));
            }

            logger.info(StringUtil.concatStrings("Waiting for stack : ", stackName,
                    ", Infrastructure: ", infrastructureConfig.getParameters()));

            TimeOutBuilder stackTimeOut = new TimeOutBuilder(TIMEOUT, TIMEOUT_UNIT, POLL_INTERVAL, POLL_UNIT);
            stackCreationWaiter.waitForStack(stackName, cloudFormation, stackTimeOut);

            final String duration = StringUtil.getHumanReadableTimeDiff(System.currentTimeMillis() - start);
            logger.info(StringUtil.concatStrings("Stack : ", stackName, ", with ID : ", stack.getStackId(),
                    " creation completed in ", duration, "."));

            // Persist resource usage details if it is the first test plan for the deployment pattern
            // or the hash values are different
            if (isFirstRunOrHashChanged) {
                DescribeStackEventsRequest describeStackEventsRequest = new DescribeStackEventsRequest()
                        .withStackName(stackName);
                DescribeStackEventsResult describeStackEventsResult = cloudFormation.
                        describeStackEvents(describeStackEventsRequest);

                if (!awsResourceManager.persistResourceRequirements(describeStackEventsResult.getStackEvents(),
                        testPlan.getDeploymentPattern(), cfnFilePath)) {
                    throw new TestGridInfrastructureException("Error while persisting resource requirements.");
                }
            }

            DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
            describeStacksRequest.setStackName(stack.getStackId());
            DescribeStacksResult describeStacksResult = cloudFormation
                    .describeStacks(describeStacksRequest);

            //TODO: remove these
            List<Host> hosts = new ArrayList<>();
            Host tomcatHost = new Host();
            tomcatHost.setLabel("tomcatHost");
            tomcatHost.setIp("ec2-35-171-21-194.compute-1.amazonaws.com");
            Host tomcatPort = new Host();
            tomcatPort.setLabel("tomcatPort");
            tomcatPort.setIp("8080");
            hosts.add(tomcatHost);
            hosts.add(tomcatPort);

            Properties outputProps = new Properties();
            for (Stack st : describeStacksResult.getStacks()) {
                for (Output output : st.getOutputs()) {
                    Host host = new Host();
                    host.setIp(output.getOutputValue());
                    host.setLabel(output.getOutputKey());
                    hosts.add(host);
                    outputProps.setProperty(output.getOutputKey(), output.getOutputValue());
                }
            }
            // add cfn input properties into the output. We sometimes use default values of cfn input params
            // which needs to passed down to the next step.
            for (TemplateParameter param : expectedParameters) {
                if (param.getDefaultValue() != null) {
                    outputProps.setProperty(param.getParameterKey(), param.getDefaultValue());
                }
            }
            for (Parameter param : populatedExpectedParameters) {
                outputProps.setProperty(param.getParameterKey(), param.getParameterValue());
            }

            persistOutputs(testPlan, outputProps);
            InfrastructureProvisionResult result = new InfrastructureProvisionResult();
            //added for backward compatibility. todo remove.
            result.setHosts(hosts);
            Properties props = new Properties();
            props.setProperty("HOSTS", hosts.toString());
            result.setProperties(props);

            return result;

        } catch (IOException e) {
            throw new TestGridInfrastructureException("Error occurred while Reading CloudFormation script", e);
        } catch (ConditionTimeoutException e) {
            throw new TestGridInfrastructureException(
                    StringUtil.concatStrings("Error occurred while waiting for stack ", stackName), e);
        } catch (TestGridDAOException e) {
            throw new TestGridInfrastructureException("Error occurred while retrieving resource requirements", e);
        } catch (NoSuchAlgorithmException e) {
            throw new TestGridInfrastructureException("Error while getting MD5 hash value of cfn", e);
        }
    }

    private void persistOutputs(TestPlan testPlan, Properties deploymentInfo)
            throws TestGridInfrastructureException {
        final Path outputLocation = DataBucketsHelper.getOutputLocation(testPlan);
        try (OutputStream outputStream = new FileOutputStream(
                outputLocation.resolve(DataBucketsHelper.INFRA_OUT_FILE).toString(), true)) {
            Files.createDirectories(outputLocation);
            deploymentInfo.store(outputStream, null);
        } catch (IOException e) {
            throw new TestGridInfrastructureException("Error occurred while writing infra outputs.", e);
        }
    }

    /**
     * The stack name must satisfy this regular expression pattern: [a-zA-Z][-a-zA-Z0-9]*
     *
     * @param stackName the aws cfn stack name
     * @return validated stack name
     */
    private String getValidatedStackName(String stackName) {
        if (stackName.matches("^[^a-zA-Z].*")) {
            stackName = 'a' + stackName;
        }
        stackName = stackName.replaceAll("[^-a-zA-Z0-9]", "-");
        return stackName;
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
    private boolean doRelease(
            InfrastructureConfig infrastructureConfig, String stackName, DeploymentPattern deploymentPattern)
            throws TestGridInfrastructureException, InterruptedException, TestGridDAOException {
        Path configFilePath;
        String region = infrastructureConfig.getProvisioners().get(0).getScripts().get(0)
                .getInputParameters().getProperty(AWS_REGION_PARAMETER);
        configFilePath = TestGridUtil.getConfigFilePath();
        AmazonCloudFormation stackdestroy = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                .withRegion(region)
                .build();
        DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
        deleteStackRequest.setStackName(stackName);
        stackdestroy.deleteStack(deleteStackRequest);
        logger.info(StringUtil.concatStrings("Stack : ", stackName, " is handed over for deletion!"));
        AWSResourceManager awsResourceManager = new AWSResourceManager();
        Optional<List<DeploymentPatternResourceUsage>> resourceRequirementsOptional =
                awsResourceManager.getResourceRequirements(deploymentPattern);
        awsResourceManager.releaseResources(resourceRequirementsOptional.get(), region);

        boolean waitForStackDeletion = Boolean.parseBoolean(ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.WAIT_FOR_STACK_DELETION));
        if (waitForStackDeletion) {
            logger.info(StringUtil.concatStrings("Waiting for stack : ", stackName, " to delete.."));
            Waiter<DescribeStacksRequest> describeStacksRequestWaiter = new
                    AmazonCloudFormationWaiters(stackdestroy).stackDeleteComplete();
            try {
                describeStacksRequestWaiter.run(new WaiterParameters<>(new DescribeStacksRequest()
                        .withStackName(stackName)));
            } catch (WaiterUnrecoverableException e) {
                throw new TestGridInfrastructureException("Error occurred while waiting for Stack :"
                                                          + stackName + " deletion !");
            }
        }
        return true;
    }

    /**
     * Reads the parameters for the stack from file.
     *
     * @param script Script object with script details.
     * @param expectedParameters Set of parameters expected by CF-script.
     * @param infrastructureConfig Infrastructure configuration of the test-plan.
     * @return a List of {@link Parameter} objects
     * @throws IOException When there is an error reading the parameters file.
     */
    private List<Parameter> getParameters(Script script, List<TemplateParameter> expectedParameters,
                                          InfrastructureConfig infrastructureConfig, String testPlanId)
            throws IOException, TestGridInfrastructureException {

        List<Parameter> cfCompatibleParameters = new ArrayList<>();

        expectedParameters.forEach(LambdaExceptionUtils.rethrowConsumer(expected -> {
            Optional<Map.Entry<Object, Object>> scriptParameter = script.getInputParameters().entrySet().stream()
                    .filter(input -> input.getKey().equals(expected
                            .getParameterKey())).findAny();
            if (!scriptParameter.isPresent() && expected.getParameterKey().equals("AMI")) {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey())
                            .withParameterValue(getAMIParameterValue(infrastructureConfig));
                cfCompatibleParameters.add(awsParameter);
            }

            scriptParameter.ifPresent(theScriptParameter -> {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue((String) theScriptParameter.getValue());
                cfCompatibleParameters.add(awsParameter);
            });

            //Set Remote Management
            if (CUSTOM_USER_DATA.equals(expected.getParameterKey())) {
                String deploymentTinkererEP = ConfigurationContext.getProperty(ConfigurationContext.
                        ConfigurationProperties.DEPLOYMENT_TINKERER_EP);
                String deploymentTinkererUserName = ConfigurationContext.getProperty(ConfigurationContext.
                        ConfigurationProperties.DEPLOYMENT_TINKERER_USERNAME);
                String deploymentTinkererPassword = ConfigurationContext.getProperty(ConfigurationContext.
                        ConfigurationProperties.DEPLOYMENT_TINKERER_PASSWORD);
                String awsRegion = ConfigurationContext.getProperty(ConfigurationContext.
                        ConfigurationProperties.AWS_REGION_NAME);
                String customScript = StringUtil.concatStrings("/opt/testgrid/agent/init.sh ",
                        deploymentTinkererEP, " ", awsRegion, " ", testPlanId, " aws ", deploymentTinkererUserName, " ",
                        deploymentTinkererPassword, "\n", "/opt/testgrid/agent/telegraf_setup.sh ", testPlanId, " ",
                        ConfigurationContext.getProperty
                                (ConfigurationContext.ConfigurationProperties.PERFORMANCE_DASHBOARD_URL), ":8086 ",
                        ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_USER),
                        " ", ConfigurationContext.getProperty(ConfigurationContext.
                                ConfigurationProperties.INFLUXDB_PASS));
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue(customScript);
                cfCompatibleParameters.add(awsParameter);
            }

            //Set WUM credentials
            if (TestGridConstants.WUM_USERNAME_PROPERTY.equals(expected.getParameterKey())) {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue(ConfigurationContext.getProperty(ConfigurationContext.
                                ConfigurationProperties.WUM_USERNAME));
                cfCompatibleParameters.add(awsParameter);
            }

            if (TestGridConstants.WUM_PASSWORD_PROPERTY.equals(expected.getParameterKey())) {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue(ConfigurationContext.getProperty(ConfigurationContext.
                                ConfigurationProperties.WUM_PASSWORD));
                cfCompatibleParameters.add(awsParameter);
            }
        }));

        return cfCompatibleParameters;
    }

    private String getAMIParameterValue(InfrastructureConfig infrastructureConfig)
            throws TestGridInfrastructureException {
        AMIMapper amiMapper = new AMIMapper(infrastructureConfig.getProvisioners().get(0).getScripts().get(0)
                .getInputParameters().getProperty(AWS_REGION_PARAMETER));
        return amiMapper.getAMIFor(infrastructureConfig.getParameters());
    }
}
