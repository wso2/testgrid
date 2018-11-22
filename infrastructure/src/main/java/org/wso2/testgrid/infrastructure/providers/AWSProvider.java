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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TimeOutBuilder;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.ConfigurationContext.ConfigurationProperties;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.infrastructure.AWSResourceLimit;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private AWSResourceManager awsResourceManager;
    private static final int TIMEOUT = 60;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;
    private static final int POLL_INTERVAL = 1;
    private static final TimeUnit POLL_UNIT = TimeUnit.MINUTES;

    @Override
    public String getProviderName() {
        return AWS_PROVIDER;
    }

    @Override
    public boolean canHandle(Script.ScriptType scriptType) {
        //Check if scripts has a cloud formation script.
        boolean isCFN = (scriptType == Script.ScriptType.CLOUDFORMATION);
        return  isCFN;

    }

    @Override
    public void init(TestPlan testPlan) throws TestGridInfrastructureException {
        cfScriptPreprocessor = new CloudFormationScriptPreprocessor();
        awsResourceManager = new AWSResourceManager();
        try {
            Path limitsYamlPath = Paths.get(TestGridUtil.getTestGridHomePath(), TestGridConstants.AWS_LIMITS_YAML);
            List<AWSResourceLimit> awsResourceLimits = awsResourceManager.populateInitialResourceLimits(limitsYamlPath);
            if (awsResourceLimits == null || awsResourceLimits.isEmpty()) {
                logger.warn("Could not populate AWS resource limits. ");
            }
        } catch (TestGridDAOException e) {
            throw new TestGridInfrastructureException("Error while retrieving aws limits.", e);
        }
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
    public InfrastructureProvisionResult provision(TestPlan testPlan, Script script)
            throws TestGridInfrastructureException {
        if (script.getType().equals(Script.ScriptType.CLOUDFORMATION)) {
            Properties infraInputs = getInfraInputs(testPlan, script);
            return doProvision(script, infraInputs, testPlan);
        }
        throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
    }

    @Override
    public boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir,
                           TestPlan testPlan, Script script) throws TestGridInfrastructureException {
        try {
            if (script.getType().equals(Script.ScriptType.CLOUDFORMATION)) {
                Properties infraInputs = getInfraInputs(testPlan, script);
                return doRelease(script, infraInputs, testPlan);
            }
            throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
        } catch (InterruptedException e) {
            throw new TestGridInfrastructureException("Error while waiting for CloudFormation stack to destroy", e);
        } catch (TestGridDAOException e) {
            throw new TestGridInfrastructureException("Error while updating released resources in the database.", e);
        }
    }

    private InfrastructureProvisionResult doProvision(Script script, Properties inputs, TestPlan testPlan) throws
            TestGridInfrastructureException {
        Path configFilePath = TestGridUtil.getConfigFilePath();
        String region = inputs.getProperty(AWS_REGION_PARAMETER);
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

            region = awsResourceManager.requestAvailableRegion(testPlan, script);
            //Set region to test plan
            inputs.setProperty(AWS_REGION_PARAMETER, region);
            script.getInputParameters().setProperty(AWS_REGION_PARAMETER, region);

            cloudFormation = AmazonCloudFormationClientBuilder.standard()
                    .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                    .withRegion(region)
                    .build();

            final List<Parameter> populatedExpectedParameters = getParameters(expectedParameters, inputs,
                    testPlan.getInfrastructureConfig().getParameters(), testPlan);
            stackRequest.setParameters(populatedExpectedParameters);
            logger.info(StringUtil.concatStrings("Creation of CloudFormation Stack '", stackName,
                    "' in region '", region, "'. Script : ", script.getFile()));
            final long start = System.currentTimeMillis();
            CreateStackResult stack = cloudFormation.createStack(stackRequest);
            if (logger.isDebugEnabled()) {
                logger.info(StringUtil.concatStrings("Stack configuration created for name ", stackName));
            }

            logger.info(StringUtil.concatStrings("Waiting for stack : ", stackName,
                    ", Infrastructure: ", testPlan.getInfrastructureConfig().getParameters()));

            TimeOutBuilder stackTimeOut = new TimeOutBuilder(TIMEOUT, TIMEOUT_UNIT, POLL_INTERVAL, POLL_UNIT);
            stackCreationWaiter.waitForStack(stackName, cloudFormation, stackTimeOut);

            final String duration = StringUtil.getHumanReadableTimeDiff(System.currentTimeMillis() - start);
            logger.info(StringUtil.concatStrings("Stack : ", stackName, ", with ID : ", stack.getStackId(),
                    " creation completed in ", duration, "."));

            DescribeStackEventsRequest describeStackEventsRequest = new DescribeStackEventsRequest()
                    .withStackName(stackName);
            DescribeStackEventsResult describeStackEventsResult = cloudFormation.
                    describeStackEvents(describeStackEventsRequest);

            //Notify AWSResourceManager about stack creation completion
            awsResourceManager.notifyStackCreation(testPlan, script, describeStackEventsResult.getStackEvents());

            DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
            describeStacksRequest.setStackName(stack.getStackId());
            DescribeStacksResult describeStacksResult = cloudFormation
                    .describeStacks(describeStacksRequest);

            List<Host> hosts = new ArrayList<>();

            Properties outputProps = new Properties();
            for (Stack st : describeStacksResult.getStacks()) {
                StringBuilder outputsStr = new StringBuilder("Infrastructure/Deployment outputs {\n");
                for (Output output : st.getOutputs()) {
                    Host host = new Host();
                    host.setIp(output.getOutputValue());
                    host.setLabel(output.getOutputKey());
                    hosts.add(host);
                    outputProps.setProperty(output.getOutputKey(), output.getOutputValue());
                    outputsStr.append(output.getOutputKey()).append("=").append(output.getOutputValue()).append("\n");
                }
                //Log cfn outputs
                logger.info(outputsStr.toString() + "\n}");
            }
            //Persist infra outputs to a file to be used for the next step
            persistOutputs(testPlan, outputProps);

            InfrastructureProvisionResult result = new InfrastructureProvisionResult();
            Properties props = new Properties();
            props.setProperty("HOSTS", hosts.toString());
            props.putAll(outputProps);
            result.setProperties(props);
            //added for backward compatibility. todo remove.
            result.setHosts(hosts);

            return result;
        } catch (IOException e) {
            throw new TestGridInfrastructureException("Error occurred while Reading CloudFormation script", e);
        } catch (ConditionTimeoutException e) {
            throw new TestGridInfrastructureException(
                    StringUtil.concatStrings("Error occurred while waiting for stack ", stackName), e);
        } catch (TestGridDAOException e) {
            throw new TestGridInfrastructureException("Error occurred while retrieving resource requirements", e);
        }
    }

    /**
     * Read the {@link InfrastructureConfig#getParameters()},
     * {@link DataBucketsHelper#TESTPLAN_PROPERTIES_FILE},
     * intermediate {@link DataBucketsHelper#INFRA_OUT_FILE}, and
     * {@link Script#getInputParameters()}.
     *
     * NOTE: properties load order is important. Latter properties has higher precedence, and will over-ride others.
     *
     * @param testPlan the test plan
     * @param script script currently being executed.
     * @return list of infrastructure inputs
     */
    private Properties getInfraInputs(TestPlan testPlan, Script script) {
        final Path inputLocation = DataBucketsHelper.getInputLocation(testPlan);
        final Path testplanPropsFile = inputLocation.resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
        final Path infraOutFile = inputLocation.resolve(DataBucketsHelper.INFRA_OUT_FILE);

        Properties props = new Properties();
        props.putAll(testPlan.getInfrastructureConfig().getParameters());
        try (InputStream tpInputStream = Files.newInputStream(testplanPropsFile, StandardOpenOption.READ);
                InputStream infraInputStream = Files.newInputStream(infraOutFile, StandardOpenOption.READ)) {
            props.load(tpInputStream);
            props.load(infraInputStream);
        } catch (IOException e) {
            logger.error(String.format("Error while reading infrastructure inputs from '%s' and/or '%s'. Continuing "
                            + "the flow with parameters already found..", testplanPropsFile, infraOutFile), e);
        }
        props.putAll(script.getInputParameters());

        return props;
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
     * @param script            the script config
     * @param inputs
     * @return true or false to indicate the result of destroy operation.
     * @throws TestGridInfrastructureException when AWS error occurs in deletion process.
     * @throws InterruptedException            when there is an interruption while waiting for the result.
     */
    private boolean doRelease(Script script, Properties inputs, TestPlan testPlan)
            throws TestGridInfrastructureException, InterruptedException, TestGridDAOException {
        Path configFilePath;
        String stackName = script.getName();
        String region = inputs.getProperty(AWS_REGION_PARAMETER);
        configFilePath = TestGridUtil.getConfigFilePath();
        AmazonCloudFormation stackdestroy = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                .withRegion(region)
                .build();
        DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
        deleteStackRequest.setStackName(stackName);
        stackdestroy.deleteStack(deleteStackRequest);
        logger.info(StringUtil.concatStrings("Stack : ", stackName, " is handed over for deletion!"));

        //Notify AWSResourceManager about stack destruction to release acquired resources
        AWSResourceManager awsResourceManager = new AWSResourceManager();
        awsResourceManager.notifyStackDeletion(testPlan, script, region);

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
     * Reads the parameters for the stack from parsed cfn yaml file.
     *
     * @param expectedParameters Set of parameters expected by CF-script.
     * @param infraInputs all the infrastructure inputs
     * @param infraCombinationProperties Infrastructure combination of the test-plan.
     * @param testPlan  The test plan
     * @return a List of {@link Parameter} objects
     * @throws IOException When there is an error reading the parameters file.
     */
    private List<Parameter> getParameters(List<TemplateParameter> expectedParameters,
            Properties infraCombinationProperties, Properties infraInputs, TestPlan testPlan)
            throws IOException, TestGridInfrastructureException {

        String testPlanId = testPlan.getId();
        List<Parameter> cfCompatibleParameters = new ArrayList<>();

        expectedParameters.forEach(LambdaExceptionUtils.rethrowConsumer(expected -> {
            Optional<Map.Entry<Object, Object>> scriptParameter = infraInputs.entrySet().stream()
                    .filter(input -> input.getKey().equals(expected
                            .getParameterKey())).findAny();
            if (!scriptParameter.isPresent() && expected.getParameterKey().equals("AMI")) {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey())
                            .withParameterValue(getAMIParameterValue(infraCombinationProperties, infraInputs));
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
                String windowsScript;
                String customScript;
                String scriptInputs = StringUtil.concatStrings(testPlanId, " ",
                        ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.INFLUXDB_URL),
                        " ", ConfigurationContext.getProperty(ConfigurationContext.
                                ConfigurationProperties.INFLUXDB_USER), " ", ConfigurationContext.getProperty
                                (ConfigurationContext.ConfigurationProperties.INFLUXDB_PASS));

                if (testPlan.getInfraParameters().toLowerCase(Locale.ENGLISH).contains("windows")) {
                    windowsScript = StringUtil.concatStrings("cmd.exe /C \"C:/testgrid/app/agent/init.bat  " +
                            deploymentTinkererEP + " " + awsRegion + " " + testPlanId + " aws " +
                            deploymentTinkererUserName + " " + deploymentTinkererPassword +
                            "\" \n .\\telegraf_setup.sh ", scriptInputs);

                    customScript = "cd C:\\\"Program Files\"\\telegraf\n" +
                            "curl http://169.254.169.254/latest/meta-data/instance-id -o instance_id.txt\n" +
                            windowsScript + "\n" +
                            ".\\telegraf.exe  --service install > service.log\n" +
                            "while(!(netstat -o | findstr 8086 | findstr ESTABLISHED)) " +
                            "{ $val++;Write-Host $val;net stop telegraf;net start telegraf } >> service.log";
                } else {
                    customScript = StringUtil.concatStrings("/opt/testgrid/agent/init.sh ",
                            deploymentTinkererEP, " ", awsRegion, " ", testPlanId, " aws ", deploymentTinkererUserName,
                            " ", deploymentTinkererPassword, "\n", "/opt/testgrid/agent/telegraf_setup.sh ",
                            scriptInputs);
                }
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue(customScript);
                cfCompatibleParameters.add(awsParameter);
            }


            //TODO: Remove these once the UI support is implemented since they would be provided through the UI.
            //Set WUM credentials
            if (TestGridConstants.WUM_USERNAME_PROPERTY.equals(expected.getParameterKey())) {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue(ConfigurationContext.getProperty(
                                ConfigurationProperties.WUM_USERNAME));
                cfCompatibleParameters.add(awsParameter);
            }
            if (TestGridConstants.WUM_PASSWORD_PROPERTY.equals(expected.getParameterKey())) {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue(ConfigurationContext.getProperty(
                                ConfigurationProperties.WUM_PASSWORD));
                cfCompatibleParameters.add(awsParameter);
            }
            //Set AWS credentials for clustering
            if (String.valueOf(ConfigurationProperties.AWS_ACCESS_KEY_ID_CLUSTERING)
                    .equals(expected.getParameterKey())) {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue(ConfigurationContext.getProperty(
                                ConfigurationProperties.AWS_ACCESS_KEY_ID_CLUSTERING));
                cfCompatibleParameters.add(awsParameter);
            }
            if (String.valueOf(ConfigurationProperties.AWS_ACCESS_KEY_SECRET_CLUSTERING)
                    .equals(expected.getParameterKey())) {
                Parameter awsParameter = new Parameter().withParameterKey(expected.getParameterKey()).
                        withParameterValue(ConfigurationContext.getProperty(
                                ConfigurationProperties.AWS_ACCESS_KEY_SECRET_CLUSTERING));
                cfCompatibleParameters.add(awsParameter);
            }
        }));

        return cfCompatibleParameters;
    }

    private String getAMIParameterValue(Properties infraCombination, Properties infraInputs)
            throws TestGridInfrastructureException {
        AMIMapper amiMapper = new AMIMapper(infraInputs.getProperty(AWS_REGION_PARAMETER));
        return amiMapper.getAMIFor(infraCombination);
    }
}
