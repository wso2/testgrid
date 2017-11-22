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

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.Script;
import org.wso2.testgrid.infrastructure.aws.AWSManager;
import org.wso2.testgrid.infrastructure.providers.AWSProvider;

import java.util.Collections;

/**
 * This class tests the functionality of {@link AWSProvider}
 *
 * @since 1.0.0
 */
@PrepareForTest({AWSProvider.class, AWSManager.class})
public class AWSProviderTests extends PowerMockTestCase {

    @Test(description = "This test case tests the provider name of AWSProvider.")
    public void testGetProviderName() {
        AWSProvider provider = new AWSProvider();
        String providerName = provider.getProviderName();
        Assert.assertNotNull(providerName);
        Assert.assertEquals(providerName, "AWS Provider");
    }

    @Test(description = "This test case tests if this provider can handle specific " +
            "infrastructure script.")
    public void testCanHandle() {
        Infrastructure infrastructure = new Infrastructure();
        infrastructure.setProviderType(Infrastructure.ProviderType.AWS);
        Script script = new Script();
        script.setScriptType(Script.ScriptType.CLOUD_FORMATION);
        infrastructure.setScripts(Collections.singletonList(script));
        AWSProvider provider = new AWSProvider();
        boolean b = provider.canHandle(infrastructure);
        Assert.assertNotNull(b);
        Assert.assertEquals(b, true);
    }

    @Test(description = "This test case tests the initiation of infrastructure creation request.")
    public void testCreateInfrastructure() throws Exception {
        Infrastructure infrastructure = new Infrastructure();
        infrastructure.setProviderType(Infrastructure.ProviderType.AWS);
        Script script = new Script();
        script.setScriptType(Script.ScriptType.CLOUD_FORMATION);
        infrastructure.setScripts(Collections.singletonList(script));
        AWSProvider provider = new AWSProvider();
        AWSManager manager = Mockito.mock(AWSManager.class);
        Deployment deployment = new Deployment();
        deployment.setName("TestDeployment");
        Mockito.when(manager.createInfrastructure(Mockito.any(Script.class), Mockito.anyString()))
                .thenReturn(deployment);
        PowerMockito.whenNew(AWSManager.class).withAnyArguments().thenReturn(manager);
        Deployment response = provider.createInfrastructure(infrastructure, "dummyRepoDir");
        Assert.assertNotNull(response);
        Assert.assertEquals("TestDeployment", response.getName());
    }

    @Test(description = "This test case tests the functionality of removing existing CloudFormation " +
            "stack given the script with stack name.")
    public void testRemoveInfrastructure() throws Exception {
        Infrastructure infrastructure = new Infrastructure();
        infrastructure.setProviderType(Infrastructure.ProviderType.AWS);
        Script script = new Script();
        script.setScriptType(Script.ScriptType.CLOUD_FORMATION);
        infrastructure.setScripts(Collections.singletonList(script));
        AWSProvider provider = new AWSProvider();
        AWSManager manager = Mockito.mock(AWSManager.class);
        Deployment deployment = new Deployment();
        deployment.setName("TestDeployment");
        Mockito.when(manager.destroyInfrastructure(Mockito.any(Script.class))).thenReturn(true);
        PowerMockito.whenNew(AWSManager.class).withAnyArguments().thenReturn(manager);
        boolean removeInfrastructure = provider.removeInfrastructure(infrastructure, "dummyRepoDir");
        Assert.assertNotNull(removeInfrastructure);
        Assert.assertEquals(removeInfrastructure, true);
    }
}
