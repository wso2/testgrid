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
 *
 */

package org.wso2.testgrid.deployment.tinkerer.beans;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.testng.PowerMockTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.agentoperation.OperationSegment;

/**
 * Unit test for OperationMessageTest class.
 */
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*" })
public class OperationMessageTest extends PowerMockTestCase {

    private static final Logger logger = LoggerFactory.getLogger(OperationMessageTest.class);


    @BeforeMethod
    public void init() throws Exception { }

    @Test()
    public void testExecute() throws Exception {
        String operationId = "1234-5678-91011";
        String response = "this is test";
        OperationMessage operationMessage = new OperationMessage(operationId,
                OperationSegment.OperationCode.SHELL, "wso2:testgrid:1234-5678:agent:127.0.0.1");
        OperationSegment operationSegment = new OperationSegment();
        operationSegment.setOperationId(operationId);
        operationSegment.setResponse(response);
        operationMessage.addMessage(operationSegment);
        Assert.assertTrue(operationMessage.getContentLength() == response.length());
        operationMessage.persistOperationQueue();
        Assert.assertTrue(operationMessage.getContentLength() == 0,
                "Message queue size should be zero after persist");
        Assert.assertTrue(operationMessage.getMessageQueue().size() == 1,
                "Message queue after read from file");
        Assert.assertTrue(operationMessage.getContentLength() == response.length(),
                "Message size same as response size");
        operationMessage.setMessageQueue(operationMessage.getMessageQueue());
        Assert.assertTrue(operationMessage.getContentLength() == response.length(),
                "Set message queue with previous and test");
    }

    @AfterMethod
    public void tearDown() throws Exception { }

}
