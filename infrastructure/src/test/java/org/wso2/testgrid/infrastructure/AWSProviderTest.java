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
import com.amazonaws.services.cloudformation.model.TemplateParameter;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateResult;
import com.amazonaws.services.cloudformation.waiters.AmazonCloudFormationWaiters;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.infrastructure.providers.AWSProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class will test functionality of {@link AWSProvider}
 *
 * @since 1.0.0
 */
@PrepareForTest({
                        AWSProvider.class, AmazonCloudFormationClientBuilder.class
                        , AmazonCloudFormation.class, AwsClientBuilder.class
                })
public class AWSProviderTest extends PowerMockTestCase {

    private static final String AWS_ACCESS_KEY_ID_VALUE = "aws_key_value";
    private static final String AWS_SECRET_ACCESS_KEY_VALUE = "aws_secret_value";

    private String scriptFile = "template.json";
    private String mockStackName = "MockStack";

    @Test(description = "This test case tests creation of AWSProvider object when AWS credentials are " +
            "set correctly.")
    public void testManagerCreation() throws Exception {
        //set environment variables for
        Map<String, String> map = new HashMap<>();
        map.put(AWSProvider.AWS_ACCESS_KEY_ID, AWS_ACCESS_KEY_ID_VALUE);
        map.put(AWSProvider.AWS_SECRET_ACCESS_KEY, AWS_SECRET_ACCESS_KEY_VALUE);
        set(map);
        AWSProvider awsProvider = new AWSProvider();
        awsProvider.init();
        Assert.assertNotNull(awsProvider);
        unset(map.keySet());
    }

    @Test(description = "This test case tests creation of AWS Manager object when AWS credentials are " +
            "not set correctly."
            ,
          expectedExceptions = TestGridInfrastructureException.class)
    public void testManagerCreationNegativeTests() throws TestGridInfrastructureException, IOException,
            InterruptedException, NoSuchFieldException, IllegalAccessException {
        String secret2 = "AWS_SCERET2";
        Map<String, String> map = new HashMap<>();
        map.put(AWSProvider.AWS_ACCESS_KEY_ID, AWS_ACCESS_KEY_ID_VALUE);
        set(map);
        //invoke without no secret key environment variable set.
        AWSProvider awsProvider = new AWSProvider();
        awsProvider.init();
        unset(map.keySet());
    }

    @Test(description = "This test case tests infrastructure creation of AWSProvider object given the " +
            "AWS CF template path.")
    public void createInfrastructureTest() throws Exception {
        String outputKey = "PublicDNS";
        String outputValue = "TestDNS";
        String patternName = "single-node";

        Map<String, String> map = new HashMap<>();
        map.put(AWSProvider.AWS_ACCESS_KEY_ID, AWS_ACCESS_KEY_ID_VALUE);
        map.put(AWSProvider.AWS_SECRET_ACCESS_KEY, AWS_SECRET_ACCESS_KEY_VALUE);
        set(map);
        InfrastructureConfig infrastructureConfig = getDummyInfrastructureConfig(patternName);

        File resourcePath = new File("src/test/resources");
        //Stack object with output object
        Stack stack = new Stack();
        stack.setStackName(mockStackName);

        TemplateParameter parameter = new TemplateParameter().withParameterKey("AMI").withParameterKey("abc-1234");

        Output output = new Output();
        output.setOutputKey(outputKey);
        output.setOutputValue(outputValue);

        stack.setOutputs(Collections.singletonList(output));
        stack.setStackStatus(StackStatus.CREATE_COMPLETE);

        //Mocking AWS SDK objects.
        DescribeStacksResult describeStacksResultMock = Mockito.mock(DescribeStacksResult.class);
        Mockito.when(describeStacksResultMock.getStacks()).thenReturn(Collections.singletonList(stack));
        ValidateTemplateResult validateTemplateResult = Mockito.mock(ValidateTemplateResult.class);
        Mockito.when(validateTemplateResult.getParameters()).thenReturn(Collections.singletonList(parameter));

        AmazonCloudFormationClientBuilder cloudFormationClientBuilderMock = PowerMockito
                .mock(AmazonCloudFormationClientBuilder.class);
        AmazonCloudFormation cloudFormation = Mockito.mock(AmazonCloudFormation.class);
        Mockito.when(cloudFormation.validateTemplate(Mockito.any(ValidateTemplateRequest.class)))
                .thenReturn(Mockito.mock(ValidateTemplateResult.class));
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

        AmazonCloudFormationWaiters waiterMock = Mockito.mock(AmazonCloudFormationWaiters.class);
        Waiter mock = Mockito.mock(Waiter.class);
        Mockito.doNothing().when(mock).run(Matchers.any(WaiterParameters.class));
        Mockito.when(waiterMock.stackCreateComplete()).thenReturn(mock);
        PowerMockito.whenNew(AmazonCloudFormationWaiters.class).withAnyArguments().thenReturn(waiterMock);

        AWSProvider awsProvider = new AWSProvider();
        awsProvider.init();
        TestPlan testPlan = new TestPlan();
        testPlan.setInfrastructureConfig(infrastructureConfig);
        testPlan.setInfraRepoDir(resourcePath.getAbsolutePath());
        InfrastructureProvisionResult provisionResult = awsProvider
                .provision(testPlan);

        Assert.assertNotNull(provisionResult);
        Assert.assertEquals(provisionResult.getHosts().size(), 3);
        Assert.assertEquals(provisionResult.getHosts().get(2).getIp(), outputValue);

        unset(map.keySet());
    }

    private InfrastructureConfig getDummyInfrastructureConfig(String patternName) {
        //create dummy script object
        Script script = new Script();
        script.setType(Script.ScriptType.CLOUDFORMATION);
        script.setFile(scriptFile);
        script.setName(mockStackName);
        Properties scriptParameters = new Properties();
        scriptParameters.setProperty("EC2KeyPair", "test-grid.key");
        script.setInputParameters(scriptParameters);

        InfrastructureConfig infrastructureConfig = new InfrastructureConfig();
        InfrastructureConfig.Provisioner provisioner = new InfrastructureConfig.Provisioner();
        provisioner.setName(patternName);
        provisioner.setScripts(Collections.singletonList(script));
        infrastructureConfig.setProvisioners(Collections.singletonList(provisioner));
        return infrastructureConfig;
    }

    @Test(description = "This test case tests destroying infrastructure given a already built stack name.")
    public void destroyInfrastructureTest() throws Exception {
        String patternName = "single-node";
        //set environment variables for
        Map<String, String> map = new HashMap<>();
        map.put(AWSProvider.AWS_ACCESS_KEY_ID, AWS_ACCESS_KEY_ID_VALUE);
        map.put(AWSProvider.AWS_SECRET_ACCESS_KEY, AWS_SECRET_ACCESS_KEY_VALUE);
        set(map);
        InfrastructureConfig dummyInfrastructureConfig = getDummyInfrastructureConfig(patternName);
        File resourcePath = new File("src/test/resources");

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

        AmazonCloudFormationWaiters waiterMock = Mockito.mock(AmazonCloudFormationWaiters.class);
        Waiter mock = Mockito.mock(Waiter.class);
        Mockito.doNothing().when(mock).run(Matchers.any(WaiterParameters.class));
        Mockito.when(waiterMock.stackDeleteComplete()).thenReturn(mock);
        PowerMockito.whenNew(AmazonCloudFormationWaiters.class).withAnyArguments().thenReturn(waiterMock);

        AWSProvider awsProvider = new AWSProvider();
        awsProvider.init();
        boolean released = awsProvider.release(dummyInfrastructureConfig, resourcePath
                .getAbsolutePath());

        Assert.assertTrue(released);
        unset(map.keySet());
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

    /**
     * Unset the temporary env variables set via {@link #set(Map)} method.
     *
     * @param envToUnset
     */
    private void unset(Set<String> envToUnset) throws NoSuchFieldException, IllegalAccessException {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for (Class cl : classes) {
            if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                for (String envKey : envToUnset) {
                    map.remove(envKey);
                }
            }
        }
    }

}
