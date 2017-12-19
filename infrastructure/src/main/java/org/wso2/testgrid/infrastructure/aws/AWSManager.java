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
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Database;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.Script;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.EnvironmentUtil;
import org.wso2.testgrid.common.util.LambdaExceptionUtils;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class is responsible for creating the AWS infrastructure.
 *
 * @since 1.0.0
 */
public class AWSManager {

    private static final Logger logger = LoggerFactory.getLogger(AWSManager.class);

    private Infrastructure infra;
    private static final String WUM_USERNAME = "WUMUsername";
    private static final String WUM_PASSWORD = "WUMPassword";
    private static final String DB_ENGINE = "DBEngine";
    private static final String DB_ENGINE_VERSION = "DBEngineVersion";
    private static final String JDK = "JDK";
    private static final String IMAGE = "Image";

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
            throw new TestGridInfrastructureException("AWS Credentials must be set as environment variables");
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
        AmazonCloudFormation stackbuilder = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(this.infra.getRegion())
                .build();

        CreateStackRequest stackRequest = new CreateStackRequest();
        stackRequest.setStackName(cloudFormationName);
        try {
            String file = new String(Files.readAllBytes(Paths.get(infraRepoDir,
                    script.getFilePath())), StandardCharsets.UTF_8);
            stackRequest.setTemplateBody(file);
            stackRequest.setParameters(getParameters(script));
            stackbuilder.createStack(stackRequest);
            if (logger.isDebugEnabled()) {
                logger.info("Stack configuration created for name " + cloudFormationName);
            }
            waitForAWSProcess(stackbuilder, cloudFormationName);
            DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
            describeStacksRequest.setStackName(cloudFormationName);
            DescribeStacksResult describeStacksResult = stackbuilder
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
        } catch (InterruptedException e) {
            throw new TestGridInfrastructureException("Error occured while waiting for " +
                    "CloudFormation Stack creation", e);
        } catch (IOException e) {
            throw new TestGridInfrastructureException("Error occured while Reading CloudFormation script", e);
        }
    }

    /**
     * This method waits for completion of AWS process and generate result depending on
     * the result code.
     *
     * @param stackBuilder AWS CF builder object.
     * @param stackName    The stack name to perform the waiting upon.
     * @return true or false depending on the result.
     * @throws InterruptedException            when an Interrupt occurs while waiting for CF result.
     * @throws TestGridInfrastructureException when an error occurs while reading the cf template file.
     */
    private boolean waitForAWSProcess(AmazonCloudFormation stackBuilder, String stackName) throws InterruptedException,
            TestGridInfrastructureException {
        DescribeStacksRequest wait = new DescribeStacksRequest();
        wait.setStackName(stackName);
        //the status of the operation
        boolean completed = false;
        //result of the operation
        boolean successful = false;
        logger.info("Waiting ..");
        while (!completed) {
            List<Stack> stacks = stackBuilder.describeStacks(wait).getStacks();
            if (stacks.isEmpty()) {
                throw new TestGridInfrastructureException("There is no stack for the name :" + stackName);
            } else {
                for (Stack stack : stacks) {
                    if (StackStatus.CREATE_COMPLETE.toString().equals(stack.getStackStatus()) ||
                        StackStatus.DELETE_COMPLETE.toString().equals(stack.getStackStatus())) {
                        completed = true;
                        successful = true;
                    } else if (StackStatus.CREATE_FAILED.toString().equals(stack.getStackStatus()) ||
                               StackStatus.ROLLBACK_FAILED.toString().equals(stack.getStackStatus()) ||
                               StackStatus.ROLLBACK_COMPLETE.toString().equals(stack.getStackStatus())) {
                        completed = true;
                        successful = false;
                    } else if (StackStatus.CREATE_IN_PROGRESS.toString().equals(stack.getStackStatus()) ||
                               StackStatus.DELETE_IN_PROGRESS.toString().equals(stack.getStackStatus()) ||
                               StackStatus.ROLLBACK_IN_PROGRESS.toString().equals(stack.getStackStatus()) ||
                               StackStatus.REVIEW_IN_PROGRESS.toString().equals(stack.getStackStatus())) {
                        completed = false;
                    }
                }
            }
            //if the operation is not complete then wait 5 seconds and check again.
            if (!completed) {
                Thread.sleep(15000);
            }
        }
        return successful;
    }

    /**
     * Initialize the manager with an infrastructure object.
     *
     * @param infrastructure infrastructure details.
     */
    public void init(Infrastructure infrastructure) {
        this.infra = infrastructure;
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
                .withRegion(infra.getRegion())
                .build();
        DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
        deleteStackRequest.setStackName(cloudFormationName);
        stackdestroy.deleteStack(deleteStackRequest);
        return waitForAWSProcess(stackdestroy, cloudFormationName);
    }

    /**
     * Reads the parameters for the stack from file.
     *
     * @param script Script object with script details.
     * @return a List of {@link Parameter} objects
     * @throws IOException When there is an error reading the parameters file.
     */
    private List<Parameter> getParameters(Script script) throws IOException
            , TestGridInfrastructureException {

        Properties scriptParameters = script.getScriptParameters();
        List<Parameter> cfCompatibleParameters = new ArrayList<>();
        scriptParameters.forEach((key, value) -> {
            Parameter awsParam = new Parameter().withParameterKey((String) key).withParameterValue((String) value);
            cfCompatibleParameters.add(awsParam);
        });

        script.getEnvironmentScriptParameters().forEach(LambdaExceptionUtils.rethrowBiConsumer((key, value) -> {
            String envVariable = EnvironmentUtil.getSystemVariableValue((String) value);
            if (envVariable != null) {
                Parameter awsParam = new Parameter().withParameterKey((String) key).withParameterValue(envVariable);
                cfCompatibleParameters.add(awsParam);
            } else {
                throw new TestGridInfrastructureException("Environment Variable " + value + " not found !!");
            }
        }));

        InfraCombination infraCombination = infra.getInfraCombination();
        for (Parameter parameter : cfCompatibleParameters) {
            if (DB_ENGINE.equals(parameter.getParameterKey())) {
                parameter.setParameterValue(this.getDatabaseEngineName(infraCombination.getDatabase().getEngine()));
            } else if (DB_ENGINE_VERSION.equals(parameter.getParameterKey())) {
                parameter.setParameterValue(infraCombination.getDatabase().getVersion());
            } else if (JDK.equals(parameter.getParameterKey())) {
                parameter.setParameterValue(infraCombination.getJdk().name());
            } else if (IMAGE.equals(parameter.getParameterKey())) {
                parameter.setParameterValue(this.infra.getImageId());
            }
        }
        return cfCompatibleParameters;
    }

    /**
     * Converts the given db engine type to the AWS RDS engine type.
     *
     * @param databaseEngine Required DatabaseEngine type.
     * @return a {@link String} object which indicates the name of AWS RDS
     * @throws TestGridInfrastructureException When the given db engine is not supported by AWS RDS.
     */
    private String getDatabaseEngineName(Database.DatabaseEngine databaseEngine) throws
            TestGridInfrastructureException {
        switch (databaseEngine) {
            case MYSQL:
                return AWSRDSEngine.MYSQL.name;
            case POSTGRESQL:
                return AWSRDSEngine.POSTGRESQL.name;
            case ORACLE:
                return AWSRDSEngine.ORACLE.name;
            case SQL_SERVER:
                return AWSRDSEngine.SQL_SERVER.name;
            case MariaDB:
                return AWSRDSEngine.MariaDB.name;
            default:
                throw new TestGridInfrastructureException("Request DB engine '" + databaseEngine.name()
                                                          + "' is not supported by AWS.");
        }
    }
}
