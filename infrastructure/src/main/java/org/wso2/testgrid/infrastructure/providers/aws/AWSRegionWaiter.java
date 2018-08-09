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

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TimeOutBuilder;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.infrastructure.AWSResourceRequirement;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.AWSResourceLimitUOW;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Waits to get an available region on AWS.
 */
public class AWSRegionWaiter {

    private static final Logger logger = LoggerFactory.getLogger(AWSRegionWaiter.class);
    private String availableRegion;
    private int itr = 1;
    private int nextLogItr = 1;
    private int count = 1;

    public String waitForAvailableRegion(
            List<AWSResourceRequirement> resourceRequirements, TimeOutBuilder timeOutBuilder)
            throws ConditionTimeoutException, TestGridInfrastructureException, TestGridDAOException {

        AWSResourceLimitUOW awsResourceLimitUOW = new AWSResourceLimitUOW();
        String region = awsResourceLimitUOW.getAvailableRegion(resourceRequirements);
        if (region != null) {
            availableRegion = region;
        } else {
            logger.info("Waiting for an available region on AWS...");
            Awaitility.with().pollInterval(timeOutBuilder.getPollInterval(), timeOutBuilder.getPollUnit()).await().
                    atMost(timeOutBuilder.getTimeOut(), timeOutBuilder.getTimeOutUnit())
                    .until(new RegionAvailabilityWaiter(resourceRequirements));
        }
        return availableRegion;
    }

    /**
     * Inner class for the callable implementation used by awaitility to determine an
     * available region.
     */
    private class RegionAvailabilityWaiter implements Callable<Boolean> {
        private List<AWSResourceRequirement> resourceRequirements;
        private AWSResourceLimitUOW awsResourceLimitUOW;

        /**
         * Constructs the RegionAvailabilityWaiter object with AWS resource requirements
         *
         * @param resourceRequirements required aws resource counts
         */
        RegionAvailabilityWaiter(List<AWSResourceRequirement> resourceRequirements) {
            this.resourceRequirements = resourceRequirements;
            awsResourceLimitUOW = new AWSResourceLimitUOW();
        }

        @Override
        public Boolean call() throws Exception {
            //Exponentially log the waiting for region
            if (itr == nextLogItr) {
                logger.info("Waiting for an available region...");
                count++;
                nextLogItr = TestGridUtil.fibonacci(count);
            }
            itr++;
            availableRegion = awsResourceLimitUOW.getAvailableRegion(resourceRequirements);
            return availableRegion != null;
        }
    }
}
