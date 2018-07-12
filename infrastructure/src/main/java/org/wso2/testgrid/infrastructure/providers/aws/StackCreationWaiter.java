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

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.cloudformation.model.StackStatus;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TimeOutBuilder;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Validates the Stack creation in AWS.
 */
public class StackCreationWaiter {

    private static final Logger logger = LoggerFactory.getLogger(StackCreationWaiter.class);

    /**
     * Periodically checks for the status of stack creation until a defined timeout.
     *
     * @param stackName       Name of the stack
     * @param cloudFormation  AWS cloud formation
     * @param timeOutBuilder  TimeOut object
     */
    public void waitForStack(String stackName, AmazonCloudFormation cloudFormation,
                             TimeOutBuilder timeOutBuilder)
            throws ConditionTimeoutException, TestGridInfrastructureException {
        logger.info("AWS CloudFormation stack creation events:");
        Awaitility.with().pollInterval(timeOutBuilder.getPollInterval(), timeOutBuilder.getPollUnit()).await().
                atMost(timeOutBuilder.getTimeOut(), timeOutBuilder.getTimeOutUnit())
                .until(new StackCreationVerifier(stackName, cloudFormation));
    }

    /**
     * Inner class for the callable implementation used by awaitility to determine the
     * condition of the stack.
     */
    private static class StackCreationVerifier implements Callable<Boolean> {
        private String stackName;
        private AmazonCloudFormation cloudFormation;
        private List<StackEvent> prevStackEvents = Collections.emptyList();

        /**
         * Constructs the StackCreationVerifier object with stackname and cloudformation.
         *
         * @param stackName Name of the stack to wait
         * @param cloudFormation Amazon cloud formation
         */
        StackCreationVerifier(String stackName, AmazonCloudFormation cloudFormation) {
            this.stackName = stackName;
            this.cloudFormation = cloudFormation;
        }

        @Override
        public Boolean call() throws Exception {
            //Stack details
            DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest().withStackName(stackName);
            DescribeStacksResult describeStacksResult = cloudFormation.describeStacks(describeStacksRequest);
            final List<Stack> stacks = describeStacksResult.getStacks();
            if (stacks.size() > 1 || stacks.isEmpty()) {
                String stackNames = stacks.stream().map(Stack::getStackName).collect(Collectors.joining(", "));
                final String msg = "More than one stack found or stack list is empty for the stack name: " +
                        stackName + ": " + stackNames;
                logger.error(msg);
                throw new IllegalStateException(msg);
            }

            Stack stack = stacks.get(0);
            StackStatus stackStatus = StackStatus.fromValue(stack.getStackStatus());

            // Event details of the stack
            DescribeStackEventsRequest describeStackEventsRequest = new DescribeStackEventsRequest()
                    .withStackName(stackName);
            DescribeStackEventsResult describeStackEventsResult = cloudFormation.
                    describeStackEvents(describeStackEventsRequest);

            //Print a log of the current state of the resources
            StringBuilder stringBuilder = new StringBuilder();
            final List<StackEvent> originalStackEvents = describeStackEventsResult.getStackEvents();
            final List<StackEvent> newStackEvents = new ArrayList<>(originalStackEvents);
            newStackEvents.removeAll(prevStackEvents);
            ListIterator<StackEvent> li = newStackEvents.listIterator(newStackEvents.size());
            while (li.hasPrevious()) {
                StackEvent stackEvent = li.previous();
                stringBuilder.append(StringUtil.concatStrings(
                        "Status: ", stackEvent.getResourceStatus(), ", "));
                stringBuilder.append(StringUtil.concatStrings(
                        "Resource Type: ", stackEvent.getResourceType(), ", "));
                stringBuilder.append(StringUtil.concatStrings(
                        "Logical ID: ", stackEvent.getLogicalResourceId(), ", "));
                stringBuilder.append(StringUtil.concatStrings(
                        "Status Reason: ", Optional.ofNullable(stackEvent.getResourceStatusReason()).orElse("-")));
                stringBuilder.append("\n");
            }
            logger.info(StringUtil.concatStrings("\n", stringBuilder.toString()));
            prevStackEvents = originalStackEvents;

            //Determine the steps to execute based on the status of the stack
            switch (stackStatus) {
            case CREATE_COMPLETE:
                return true;
            case CREATE_IN_PROGRESS:
                break;
            default:
                throw new TestGridInfrastructureException(StringUtil.concatStrings(
                        "Stack creation transitioned to ", stackStatus.toString(), " state."));
            }
            return false;
        }
    }
}
