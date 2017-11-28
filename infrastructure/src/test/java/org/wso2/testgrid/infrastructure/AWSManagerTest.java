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
package org.wso2.testgrid.infrastructure;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.Script;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.infrastructure.aws.AWSManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class will test functionality of {@link AWSManager}
 *
 * @since 1.0.0
 */
@PrepareForTest({AWSManager.class, AmazonCloudFormationClientBuilder.class
        , AmazonCloudFormation.class, AwsClientBuilder.class})
public class AWSManagerTest extends PowerMockTestCase {

    private String key = "AWS_KEY";
    private String keyValue = "aws_key_value";
    private String secret = "AWS_SCERET";
    private String secretValue = "aws_secret_value";
    private String scriptFile = "template.json";
    private String mockStackName = "MockStack";

    @Test(description = "This test case tests creation of AWSManager object when AWS credentials are " +
            "set correctly.")
    public void testManagerCreation() throws Exception {
        //set environment variables for
        Map<String, String> map = new HashMap<>();
        map.put(key, keyValue);
        map.put(secret, secretValue);
        set(map);
        AWSManager awsManager = new AWSManager(key, secret);
        Assert.assertNotNull(awsManager);
    }

    @Test(description = "This test case tests creation of AWS Manager object when AWS credentials are " +
            "not set correctly."
            , expectedExceptions = TestGridInfrastructureException.class)
    public void testManagerCreationNegativeTests() throws TestGridInfrastructureException, IOException,
            InterruptedException, NoSuchFieldException, IllegalAccessException {
        String secret2 = "AWS_SCERET2";
        Map<String, String> map = new HashMap<>();
        map.put(key, keyValue);
        set(map);
        //invoke without no secret key environment variable set.
        new AWSManager(key, secret2);
    }

    @Test(description = "This test case tests infrastructure creation of AWSManager object given the " +
            "AWS CF template path.")
    public void createInfrastructureTest() throws Exception {
        String outputKey = "PublicDNS";
        String outputValue = "TestDNS";
        String patternName = "single-node";

        Map<String, String> map = new HashMap<>();
        map.put(key, keyValue);
        map.put(secret, secretValue);
        set(map);
        //create dummy script object
        Script script = new Script();
        script.setScriptType(Script.ScriptType.CLOUD_FORMATION);
        script.setFilePath(scriptFile);
        script.setName(mockStackName);
        Properties scriptParameters = new Properties();
        scriptParameters.setProperty("CloudFormationParameterFile", "parameters.json");
        script.setScriptParameters(scriptParameters);
        //create dummy infrastructure object
        Infrastructure infrastructure = new Infrastructure();
        infrastructure.setName(patternName);
        infrastructure.setScripts(Collections.singletonList(script));
        File resourcePath = new File("src/test/resources");
        //Stack object with output object
        Stack stack = new Stack();
        stack.setStackName(mockStackName);

        Output output = new Output();
        output.setOutputKey(outputKey);
        output.setOutputValue(outputValue);

        stack.setOutputs(Collections.singletonList(output));
        stack.setStackStatus(StackStatus.CREATE_COMPLETE);

        //Mocking AWS SDK objects.
        DescribeStacksResult describeStacksResultMock = Mockito.mock(DescribeStacksResult.class);
        Mockito.when(describeStacksResultMock.getStacks()).thenReturn(Collections.singletonList(stack));

        AmazonCloudFormationClientBuilder cloudFormationClientBuilderMock = PowerMockito
                .mock(AmazonCloudFormationClientBuilder.class);
        AmazonCloudFormation cloudFormation = Mockito.mock(AmazonCloudFormation.class);
        Mockito.when(cloudFormation.createStack(Mockito.any(CreateStackRequest.class)))
                .thenReturn(Mockito.mock(CreateStackResult.class));
        Mockito.when(cloudFormation.describeStacks(Mockito.any(DescribeStacksRequest.class)))
                .thenReturn(describeStacksResultMock);
        PowerMockito.when(cloudFormationClientBuilderMock.withRegion(Mockito.anyString()))
                .thenReturn(cloudFormationClientBuilderMock);
        PowerMockito.when(cloudFormationClientBuilderMock.withCredentials(Mockito.
                any(EnvironmentVariableCredentialsProvider.class))).thenReturn(cloudFormationClientBuilderMock);
        PowerMockito.when(cloudFormationClientBuilderMock.build()).thenReturn(cloudFormation);
        PowerMockito.mockStatic(AmazonCloudFormationClientBuilder.class);
        PowerMockito.when(AmazonCloudFormationClientBuilder.standard()).thenReturn(cloudFormationClientBuilderMock);

        AWSManager awsManager = new AWSManager(key, secret);
        awsManager.init(infrastructure);
        Deployment dep = awsManager.createInfrastructure(script, resourcePath.getAbsolutePath());

        Assert.assertNotNull(dep);
        Assert.assertEquals(dep.getHosts().size(), 1);
        Assert.assertEquals(dep.getHosts().get(0).getIp(), outputValue);
    }

    @Test(description = "This test case tests destroying infrastructure given a already built stack name.")
    public void destroyInfrastructureTest() throws NoSuchFieldException, IllegalAccessException
            , TestGridInfrastructureException, InterruptedException {
        //set environment variables for
        Map<String, String> map = new HashMap<>();
        map.put(key, keyValue);
        map.put(secret, secretValue);
        set(map);
        //create dummy script object
        Script script = new Script();
        script.setScriptType(Script.ScriptType.CLOUD_FORMATION);
        script.setFilePath(scriptFile);
        script.setName(mockStackName);

        AmazonCloudFormationClientBuilder cloudFormationClientBuilderMock = PowerMockito
                .mock(AmazonCloudFormationClientBuilder.class);
        PowerMockito.when(cloudFormationClientBuilderMock.withRegion(Mockito.anyString()))
                .thenReturn(cloudFormationClientBuilderMock);
        PowerMockito.when(cloudFormationClientBuilderMock
                .withCredentials(Mockito.any(EnvironmentVariableCredentialsProvider.class)))
                .thenReturn(cloudFormationClientBuilderMock);
        AmazonCloudFormation cloudFormation = Mockito.mock(AmazonCloudFormation.class);
        PowerMockito.when(cloudFormationClientBuilderMock.build()).thenReturn(cloudFormation);
        PowerMockito.mockStatic(AmazonCloudFormationClientBuilder.class);
        PowerMockito.when(AmazonCloudFormationClientBuilder.standard())
                .thenReturn(cloudFormationClientBuilderMock);
        Mockito.when(cloudFormation.deleteStack(Mockito.any(DeleteStackRequest.class)))
                .thenReturn(Mockito.mock(DeleteStackResult.class));

        Stack stack = new Stack();
        stack.setStackName(mockStackName);
        stack.setStackStatus(StackStatus.DELETE_COMPLETE);

        DescribeStacksResult describeStacksResultMock = Mockito.mock(DescribeStacksResult.class);
        Mockito.when(describeStacksResultMock.getStacks()).thenReturn(Collections.singletonList(stack));
        Mockito.when(cloudFormation.describeStacks(Mockito.any(DescribeStacksRequest.class)))
                .thenReturn(describeStacksResultMock);

        AWSManager awsManager = new AWSManager(key, secret);
        awsManager.init(new Infrastructure());
        awsManager.destroyInfrastructure(script);
    }

    /**
     * Sets a temporary environment variable for current runtime.
     *
     * @param newenv Map with environment variables to set.
     * @throws NoSuchFieldException   Error occurs while locating the field.
     * @throws IllegalAccessException Error occur while accessing the argument.
     */
    private void set(Map<String, String> newenv) throws NoSuchFieldException, IllegalAccessException {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for (Class cl : classes) {
            if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.putAll(newenv);
            }
        }
    }
}

