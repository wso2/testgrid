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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.infrastructure.providers.aws;

import com.amazonaws.services.cloudformation.model.StackEvent;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.infrastructure.AWSResourceLimit;
import org.wso2.testgrid.common.infrastructure.AWSResourceRequirement;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.AWSResourceLimitUOW;
import org.wso2.testgrid.dao.uow.AWSResourceRequirementUOW;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Test class to test methods in AWSResourceManager.
 */
@PrepareForTest({AWSResourceManager.class, AWSRegionWaiter.class, TestGridUtil.class, ConfigurationContext.class})
@SuppressStaticInitializationFor("org.wso2.testgrid.common.config.ConfigurationContext")
public class AWSResourceManagerTest extends PowerMockTestCase {
    private AWSResourceManager awsResourceManager;
    private AWSResourceLimitUOW awsResourceLimitUOWMock;
    private AWSResourceRequirementUOW awsResourceRequirementUOWMock;
    private TestPlan testPlan;

    @BeforeMethod
    public void setUp() throws Exception {
        awsResourceManager = new AWSResourceManager();
        testPlan = new TestPlan();

        awsResourceLimitUOWMock = Mockito.mock(AWSResourceLimitUOW.class);
        awsResourceRequirementUOWMock = Mockito.mock(AWSResourceRequirementUOW.class);
        PowerMockito.whenNew(AWSResourceLimitUOW.class).withAnyArguments().thenReturn(awsResourceLimitUOWMock);
        PowerMockito.whenNew(AWSResourceRequirementUOW.class).withAnyArguments()
                .thenReturn(awsResourceRequirementUOWMock);
        PowerMockito.mockStatic(ConfigurationContext.class);
        PowerMockito.mockStatic(TestGridUtil.class);
    }

    @Test (description = "Tests persisting of initial limits of AWS resources")
    public void testGenerateInitialResourceLimits() throws Exception {
        Path limitsYamlpath = Paths.get("src/test/resources/awsLimits.yaml");
        assertNotNull(awsResourceManager.populateInitialResourceLimits(limitsYamlpath));
    }

    @Test(description = "Tests the behavior when file to populate aws resource limits does not exist.")
    public void testGetInitialResourceLimitsWhenFileUnavailable() throws Exception {
        //Path to a non-existent file
        Path limitsYamlpath = Paths.get("src/test/resources/limits.yaml");
        AWSResourceManager awsResourceManager = new AWSResourceManager();
        assertNull(awsResourceManager.populateInitialResourceLimits(limitsYamlpath));
    }

    @Test(description = "Test providing a region to run a test plan when resource requirements are available.")
    public void testGetAvailableRegion() throws Exception {
        String cfnhash = "abc123";
        String region = "us-east-1";
        //Create test resource requirement
        AWSResourceRequirement awsResourceRequirement = new AWSResourceRequirement();
        awsResourceRequirement.setServiceName("ec2");
        awsResourceRequirement.setLimitName("m3.xlarge");
        awsResourceRequirement.setRequiredCount(1);

        //Create test AWSResoureLimit with an available region to allocate above resource requirement
        AWSResourceLimit awsResourceLimit = new AWSResourceLimit();
        awsResourceLimit.setServiceName("ec2");
        awsResourceLimit.setLimitName("m3.xlarge");
        awsResourceLimit.setRegion(region);
        awsResourceLimit.setCurrentUsage(20);
        awsResourceLimit.setMaxAllowedLimit(100);

        testPlan.setInfrastructureConfig(getDummyInfrastructureConfig());
        Mockito.when(TestGridUtil.getHashValue(Mockito.any())).thenReturn(cfnhash);
        Map<String, Object> params = new HashMap<>();
        params.put(AWSResourceRequirement.MD5_HASH_COLUMN, cfnhash);
        Mockito.when(awsResourceRequirementUOWMock.findByFields(params))
                .thenReturn(Collections.singletonList(awsResourceRequirement));
        Mockito.when(awsResourceLimitUOWMock.findByFields(params))
                .thenReturn(Collections.singletonList(awsResourceLimit));
        Mockito.when(awsResourceLimitUOWMock.
                getAvailableRegion(Collections.singletonList(awsResourceRequirement))).thenReturn(region);

        Mockito.when(awsResourceLimitUOWMock.findAll()).thenReturn(Collections.singletonList(awsResourceLimit));
        assertEquals(region, awsResourceManager.requestAvailableRegion(testPlan));
    }

    @Test(description = "Test providing a region to run a test plan when resource requirements are unavailable." +
            "Expected to return the default region specified in config.properties")
    public void testGetAvailableRegionDefault() throws Exception {
        String cfnhash = "abc123";
        String defaultRegion = "us-east-1";
        List<AWSResourceRequirement> emptyList = new ArrayList<>();

        testPlan.setInfrastructureConfig(getDummyInfrastructureConfig());
        Mockito.when(TestGridUtil.getHashValue(Mockito.any())).thenReturn(cfnhash);
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME)).thenReturn(defaultRegion);
        Map<String, Object> params = new HashMap<>();
        params.put(AWSResourceRequirement.MD5_HASH_COLUMN, cfnhash);
        Mockito.when(awsResourceRequirementUOWMock.findByFields(params))
                .thenReturn(emptyList);
        assertEquals(defaultRegion, awsResourceManager.requestAvailableRegion(testPlan));
    }

    private InfrastructureConfig getDummyInfrastructureConfig() {
        //create dummy script object
        Script script = new Script();
        script.setType(Script.ScriptType.CLOUDFORMATION);
        script.setFile("template.yaml");
        script.setName("mockStack");
        Properties scriptParameters = new Properties();
        scriptParameters.setProperty("EC2KeyPair", "test-grid.key");
        scriptParameters.setProperty("region", "us-east-1");
        script.setInputParameters(scriptParameters);

        InfrastructureConfig infrastructureConfig = new InfrastructureConfig();
        InfrastructureConfig.Provisioner provisioner = new InfrastructureConfig.Provisioner();
        provisioner.setName("pattern-1");
        provisioner.setScripts(Collections.singletonList(script));
        infrastructureConfig.setProvisioners(Collections.singletonList(provisioner));
        return infrastructureConfig;
    }

    @Test (description = "Test if the requirements are persisted when the stack was run in default region." +
            "Running in default region means the resource requirements are unavailable")
    public void testNotifyStackCreationInDefaultRegion() throws Exception {
        String defaultRegion = "us-east-1";
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME)).thenReturn(defaultRegion);
        StackEvent stackEvent = new StackEvent();
        stackEvent.setResourceType("AWS::EC2::VPC");
        Mockito.doNothing().when(awsResourceRequirementUOWMock)
                .persistResourceRequirements(Mockito.anyListOf(AWSResourceRequirement.class));
        testPlan.setInfrastructureConfig(getDummyInfrastructureConfig());
        awsResourceManager.notifyStackCreation(testPlan, Collections.singletonList(stackEvent));
        Mockito.verify(awsResourceRequirementUOWMock, Mockito.times(1))
                .persistResourceRequirements(Mockito.anyListOf(AWSResourceRequirement.class));
    }

    @Test (description = "Test if the requirements are persisted when they already available")
    public void testNotifyStackCreationNotInDefaultRegion() throws Exception {
        String region = "us-east-2";
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME)).thenReturn(region);
        StackEvent stackEvent = new StackEvent();
        stackEvent.setResourceType("AWS::EC2::VPC");
        Mockito.doNothing().when(awsResourceRequirementUOWMock)
                .persistResourceRequirements(Mockito.anyListOf(AWSResourceRequirement.class));
        testPlan.setInfrastructureConfig(getDummyInfrastructureConfig());
        awsResourceManager.notifyStackCreation(testPlan, Collections.singletonList(stackEvent));
        Mockito.verify(awsResourceRequirementUOWMock, Mockito.times(0))
                .persistResourceRequirements(Mockito.anyListOf(AWSResourceRequirement.class));
    }

    @Test(description = "Test releasing resources after a stack is deleted.")
    public void testNotifyStackDeletion() throws Exception {
        String region = "us-east-1";
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME)).thenReturn("us-east-2");
        testPlan.setInfrastructureConfig(getDummyInfrastructureConfig());

        Mockito.doNothing().when(awsResourceLimitUOWMock)
                .releaseResources(Mockito.anyListOf(AWSResourceRequirement.class), Mockito.anyString());
        awsResourceManager.notifyStackDeletion(testPlan, region);
        Mockito.verify(awsResourceLimitUOWMock, Mockito.times(1))
                .releaseResources(Mockito.anyListOf(AWSResourceRequirement.class), Mockito.anyString());
    }

    @Test(description = "Test releasing resources after a stack in the default region is deleted.")
    public void testNotifyStackDeletionForDefaultRegion() throws Exception {
        String region = "us-east-1";
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME)).thenReturn(region);
        testPlan.setInfrastructureConfig(getDummyInfrastructureConfig());

        Mockito.doNothing().when(awsResourceLimitUOWMock)
                .releaseResources(Mockito.anyListOf(AWSResourceRequirement.class), Mockito.anyString());
        awsResourceManager.notifyStackDeletion(testPlan, region);
        Mockito.verify(awsResourceLimitUOWMock, Mockito.times(0))
                .releaseResources(Mockito.anyListOf(AWSResourceRequirement.class), Mockito.anyString());
    }
}
