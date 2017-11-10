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
package org.wso2.carbon.testgrid.infrastructure;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.Script;
import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.carbon.testgrid.infrastructure.aws.AWSDeployer;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class will test AWS deployer related tasks.
 */
@PrepareForTest({AWSDeployer.class,AmazonCloudFormationClientBuilder.class,AmazonCloudFormation.class, AwsClientBuilder.class})
public class AWSDeployerTest extends PowerMockTestCase{

    @Test
    public void testDeployerCreation() throws Exception {
        //test dummy varibles
        String key = "AWS_KEY";
        String keyValue = "aws_key_value";
        String secret = "AWS_SCERET";
        String secret_value = "aws_secret_value";
        //set environment variables for
        Map<String,String> map = new HashMap<>();
        map.put(key,keyValue);
        map.put(secret,secret_value);
        set(map);
        AWSDeployer awsDeployer = new AWSDeployer(key,secret);
        Assert.assertNotNull(awsDeployer);
    }

    @Test
    @ExpectedExceptions({TestGridInfrastructureException.class})
    public void testDeployerCreationNegativeTests() throws TestGridInfrastructureException, IOException,
            InterruptedException, NoSuchFieldException, IllegalAccessException {
        //test dummy varibles
        String key = "AWS_KEY";
        String keyValue = "aws_key_value";
        String secret = "AWS_SCERET2";
        //set environment variables for
        Map<String,String> map = new HashMap<>();
        map.put(key,keyValue);
        set(map);
        //invoke without no secret key environment varible set.
        new AWSDeployer(key,secret);
    }

    @Test
    public void createInfrastructureTest() throws Exception {
        String key = "AWS_KEY";
        String keyValue = "aws_key_value";
        String secret = "AWS_SCERET";
        String secret_value = "aws_secret_value";
        //set environment variables for
        Map<String,String> map = new HashMap<>();
        map.put(key,keyValue);
        map.put(secret,secret_value);
        set(map);
        Script script = new Script();
        script.setScriptType(Script.ScriptType.CLOUD_FORMATION);
        Infrastructure infrastructure = new Infrastructure();
        infrastructure.setName("single-node");
        infrastructure.setScripts(Arrays.asList(script));
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("DeploymentPatterns").getFile());
        //TODO mock aws service and test creation process.
    }

    /**
     * Sets a temperary environmental variable for current runtime.
     *
     * @param newenv Map with environment variables to set.
     * @throws NoSuchFieldException Error occurs while locating the field.
     * @throws IllegalAccessException Error occur while accessing the argument.
     */
    private void set(Map<String, String> newenv) throws NoSuchFieldException, IllegalAccessException {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for(Class cl : classes) {
            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.putAll(newenv);
            }
        }
    }
}
