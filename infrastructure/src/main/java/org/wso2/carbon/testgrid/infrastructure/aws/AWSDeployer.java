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
package org.wso2.carbon.testgrid.infrastructure.aws;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Host;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.Script;
import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.carbon.testgrid.utils.EnvVariableUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for creating the AWS infrastructure.
 */
public class AWSDeployer {

    private Infrastructure infra;
    private static final Log log = LogFactory.getLog(AWSDeployer.class);

    /**
     * This constructor creates AWS deployer object and validate AWS related environment variables are present.
     *
     * @param awsKeyVariableName    Environment variable name for AWS ACCESS KEY.
     * @param awsSecretVariableName Environment variable name for AWS SECRET KEY.
     * @throws TestGridInfrastructureException Throws exception when environment variables are not set.
     */
    public AWSDeployer(String awsKeyVariableName, String awsSecretVariableName) throws TestGridInfrastructureException {
        String awsIdentity = EnvVariableUtil.readEnvironmentVariable(awsKeyVariableName);
        String awsSecret = EnvVariableUtil.readEnvironmentVariable(awsSecretVariableName);
        if ((awsIdentity == null) || (awsSecret == null)) {
            String errorMessage = "AWS Credentials must be set as environment variables";
            throw new TestGridInfrastructureException(errorMessage);
        }
    }

    /**
     * This method initiates creating infrastructure through CLoudFormation.
     *
     * @param script       Script object containing the CF details.
     * @param infraRepoDir Path of TestGrid home location in file system as a String.
     * @return a Deployment object with created infrastructure details.
     * @throws InterruptedException            when an Interrupt occurs while waiting for CF result.
     * @throws IOException                     when an error occurs while reading the cf template file.
     * @throws TestGridInfrastructureException AWS related error caused by malformed CF template.
     */
    public Deployment createInfrastructure(Script script, String infraRepoDir) throws InterruptedException,
            IOException, TestGridInfrastructureException {
        String cfName = script.getName();
        AmazonCloudFormation stackbuilder = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(infra.getRegion())
                .build();

        CreateStackRequest stackRequest = new CreateStackRequest();
        stackRequest.setStackName(cfName);
        String file = new String(Files.readAllBytes(Paths.get(infraRepoDir, this.infra.getName(),
                "AWS", "Scripts", script.getName())));
        stackRequest.setTemplateBody(file);
        log.info("Created a CloudFormation Stack with the name :" + stackRequest.getStackName());
        stackbuilder.createStack(stackRequest);
        waitForAWSProcess(stackbuilder, cfName);
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
        describeStacksRequest.setStackName(cfName);
        DescribeStacksResult describeStacksResult = stackbuilder.describeStacks(describeStacksRequest);
        //Initially only PublicDNS which is exported from a dummy script is saved in the Deployment object.
        List<Host> hosts = new ArrayList<>();
        for (Stack st : describeStacksResult.getStacks()) {
            for (Output output : st.getOutputs()) {
                if (output.getOutputKey().equals("PublicDNS")) {
                    Host host = new Host();
                    host.setIp(output.getOutputValue());
                    hosts.add(host);
                }
            }
        }
        Deployment deployment = new Deployment();
        deployment.setHosts(hosts);
        return deployment;
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
        boolean completed = false;
        String status = "UNKNOWN";
        String reason = "";

        log.info("Waiting ..");
        while (!completed) {
            List<Stack> stacks = stackBuilder.describeStacks(wait).getStacks();
            if (stacks.isEmpty()) {
                log.info("There is no stack by the name " + stackName);
                return false;
            } else {
                for (Stack stack : stacks) {
                    if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
                            stack.getStackStatus().equals(StackStatus.DELETE_COMPLETE.toString())) {
                        completed = true;
                        return true;
                    } else if (stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
                            stack.getStackStatus().equals(StackStatus.ROLLBACK_COMPLETE.toString())) {
                        String errorMessage = "Error while executing infrastructure command due to :" +
                                stack.getStackStatusReason();
                        throw new TestGridInfrastructureException(errorMessage);
                    }
                }
            }
            if (!completed) {
                Thread.sleep(5000);
            }
        }
        log.info("Finished creating stack");
        log.info(status + "(" + reason + ")");
        return false;
    }

    /**
     * Initialize the deployer with an infrastructure object.
     *
     * @param infrastructure infrastructure details.
     */
    public void init(Infrastructure infrastructure) {
        this.infra = infrastructure;
    }

    /**
     * This method destroys the CF infrastructure given the stack name.
     *
     * @param script Script object with the CloudFormaiton details.
     * @return true if destroy finishes succesfully.
     * @throws TestGridInfrastructureException when AWS error occurs in deletion process.
     * @throws InterruptedException            when there is an interruption while waiting for the result.
     */
    public boolean destroyInfrastructure(Script script) throws TestGridInfrastructureException, InterruptedException {

        String cfName = script.getName();
        AmazonCloudFormation stackdestroy = AmazonCloudFormationClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(infra.getRegion())
                .build();
        DeleteStackRequest deleteStackRequest = new DeleteStackRequest();
        deleteStackRequest.setStackName(cfName);
        stackdestroy.deleteStack(deleteStackRequest);
        return waitForAWSProcess(stackdestroy, cfName);

    }
}
