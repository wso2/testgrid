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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TimeOutBuilder;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.infrastructure.AWSResourceLimit;
import org.wso2.testgrid.common.infrastructure.AWSResourceRequirement;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.AWSResourceLimitUOW;
import org.wso2.testgrid.dao.uow.AWSResourceRequirementUOW;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.amazonaws.services.cloudformation.model.ResourceStatus.CREATE_COMPLETE;

/**
 * Used to manage AWS resources to allocate regions for test plans based on the resource availability.
 * This class is responsible of checking and allocating AWS resources before a test plan is run and
 * releasing resources once the test plan has finished running.
 */
public class AWSResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(AWSResourceManager.class);

    private static final String AWS_REGION_PARAMETER = "region";
    private static final String EC2_RESOURCE_IDENTIFIER = "AWS::EC2::Instance";
    private static final String VPC_RESOURCE_IDENTIFIER = "AWS::EC2::VPC";
    private static final String EC2_SERVICE_NAME = "ec2";
    private static final String NETWORKING_SERVICE_NAME = "networking";
    private static final String VPC_LIMIT_NAME = "vpc";

    private static final int REGION_WAIT_TIMEOUT = 12;
    private static final TimeUnit REGION_WAIT_TIMEOUT_UNIT = TimeUnit.HOURS;
    private static final int REGION_WAIT_POLL_INTERVAL = 1;
    private static final TimeUnit REGION_WAIT_POLL_UNIT = TimeUnit.MINUTES;

    /**
     * Parses the limits.yaml and returns the AWS resources and limits.
     * This is used to populate the initial AWSResourceLimits
     *
     * @param limitsFilePath path to limits.yaml file
     * @throws TestGridDAOException if
     * @throws TestGridInfrastructureException if error occurs while retrieving AWS resource limits
     */
    public List<AWSResourceLimit> populateInitialResourceLimits(Path limitsFilePath)
            throws TestGridInfrastructureException, TestGridDAOException {

        if (!limitsFilePath.toFile().exists()) {
            logger.warn(limitsFilePath.toString() + " file not found.");
            return null;
        }
        Yaml yaml;
        String yamlContent;
        try {
            yaml = new Yaml();
            yamlContent = new String(Files.readAllBytes(limitsFilePath), StandardCharsets.UTF_8);
            Map<String, Object> awsResourceLimits = yaml.load(yamlContent);

            if (awsResourceLimits == null || awsResourceLimits.isEmpty()) {
                logger.warn("Error occurred while parsing" + limitsFilePath.toString());
                return null;
            }
            List<AWSResourceLimit> awsResourceLimitList = parseAWSLimits(awsResourceLimits);
            AWSResourceLimitUOW awsResourceLimitsUOW = new AWSResourceLimitUOW();
            return awsResourceLimitsUOW.persistInitialLimits(awsResourceLimitList);
        } catch (IOException e) {
            throw new TestGridInfrastructureException("Error while trying to get aws resource limits.", e);
        }
    }

    /**
     * Checks the database if resource requirements are already available for the given cloudformation
     * script.
     * If available, acquires and returns an available region. Else, the default region
     * specified in config.properties is returned.
     *
     * @param testPlan test plan to create the stack for
     * @return available AWS region
     * @throws TestGridDAOException if persisting to database fails
     * @throws TestGridInfrastructureException if retrieving the hash of cloudformation fails
     */
    public String requestAvailableRegion(TestPlan testPlan) throws
            TestGridDAOException, TestGridInfrastructureException {

        AWSResourceLimitUOW awsResourceLimitUOW = new AWSResourceLimitUOW();
        if (awsResourceLimitUOW.findAll().isEmpty()) {
            String region = ConfigurationContext.getProperty(
                    ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME);
            logger.info("No AWS limits information found in the database. " +
                    "Test plan will be run in " + region);
            return region;
        }

        Path cfnFilePath = Paths.get(
                testPlan.getInfrastructureRepository(),
                testPlan.getInfrastructureConfig().getProvisioners().get(0).getScripts().get(0).getFile());

        List<AWSResourceRequirement> resourceRequirements;
        try {
            resourceRequirements = getResourceRequirements(TestGridUtil.getHashValue(cfnFilePath));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new TestGridInfrastructureException("Error while retrieving the MD5 checksum of cfn.", e);
        }
        if (resourceRequirements.isEmpty()) {
            String region = ConfigurationContext.getProperty(
                    ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME);
            logger.info("No resource requirement information found in the database. " +
                    "Test plan will be run in " + region);
            return region;
        } else {
            TimeOutBuilder regionTimeOutBuilder = new TimeOutBuilder(REGION_WAIT_TIMEOUT, REGION_WAIT_TIMEOUT_UNIT,
                    REGION_WAIT_POLL_INTERVAL, REGION_WAIT_POLL_UNIT);
            AWSRegionWaiter awsRegionWaiter = new AWSRegionWaiter();
            String region = awsRegionWaiter.waitForAvailableRegion(resourceRequirements, regionTimeOutBuilder);
            logger.info("Acquired resources for test plan: " + testPlan.toString());

            // Update last_accessed_timestamp to current timestamp
            AWSResourceRequirementUOW awsResourceRequirementUOW = new AWSResourceRequirementUOW();
            for (AWSResourceRequirement resourceRequirement : resourceRequirements) {
                resourceRequirement.setLastAccessedTimestamp(new Timestamp(System.currentTimeMillis()));
                awsResourceRequirementUOW.persist(resourceRequirement);
            }
            return region;
        }
    }

    /**
     * Called when a AWS stack creation has been completed. Persists resourse requirements to the database
     * if they are not already available.
     *
     * @param testPlan test plan of which the stack was created
     * @param stackEventList list of stack events to identfy resource requirements
     * @throws TestGridDAOException if persistence fails
     * @throws TestGridInfrastructureException if retrieving md5 hash fails
     */
    public void notifyStackCreation(TestPlan testPlan, List<StackEvent> stackEventList)
            throws TestGridDAOException, TestGridInfrastructureException {
        String region = testPlan.getInfrastructureConfig().getProvisioners().get(0).getScripts().get(0)
                .getInputParameters().getProperty(AWS_REGION_PARAMETER);

        /*
        * Persist AWS resource requirements only if it is the first test plan for
        * deployment pattern or if the cloudformation script has changed
        */
        if (region.equals(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME))) {
            Path cfnFilePath = Paths.get(
                    testPlan.getInfrastructureRepository(),
                    testPlan.getInfrastructureConfig().getProvisioners().get(0).getScripts().get(0).getFile());
            AWSResourceRequirementUOW awsResourceRequirementUOW = new AWSResourceRequirementUOW();
            try {
                List<AWSResourceRequirement> resourceRequirements =
                        createRequiredResourcesList(stackEventList, TestGridUtil.getHashValue(cfnFilePath));
                awsResourceRequirementUOW.persistResourceRequirements(resourceRequirements);
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new TestGridInfrastructureException(
                        "Error while retrieving the MD5 hash of cloudformation script", e);
            }
        }
    }

    /**
     * Called when a created stack has been deleted. Releases the acquired resources when notified.
     *
     * @param testPlan test plan of which the stack has been deleted
     * @param region region the stack was running
     * @throws TestGridDAOException if persistence to database fails
     * @throws TestGridInfrastructureException if retrieving md5 hash fails
     */
    public void notifyStackDeletion(TestPlan testPlan, String region) throws
            TestGridDAOException, TestGridInfrastructureException {
        if (!region.equals(ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME))) {
            logger.info("Releasing resources from test plan " + testPlan.toString());
            AWSResourceRequirementUOW awsResourceRequirementUOW = new AWSResourceRequirementUOW();
            AWSResourceLimitUOW awsResourceLimitUOW = new AWSResourceLimitUOW();
            Path cfnFilePath = Paths.get(
                    testPlan.getInfrastructureRepository(),
                    testPlan.getInfrastructureConfig().getProvisioners().get(0).getScripts().get(0).getFile());
            Map<String, Object> params = new HashMap<>();
            try {
                params.put(AWSResourceRequirement.MD5_HASH_COLUMN, TestGridUtil.getHashValue(cfnFilePath));
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new TestGridInfrastructureException(
                        "Error while retrieving the MD5 hash of cloudformation script", e);
            }
            awsResourceLimitUOW.releaseResources(awsResourceRequirementUOW.findByFields(params), region);
        }
    }

    /**
     * Parses the given list of resources and returns a list of {@link AWSResourceLimit}.
     *
     * @param awsResourceLimitsMap aws resource limits ArrayList
     * @return list of {@link AWSResourceLimit}
     */
    private List<AWSResourceLimit> parseAWSLimits(Map<String, Object> awsResourceLimitsMap) {
        List<AWSResourceLimit> awsResourceLimitsList = new ArrayList<>();
        ArrayList limits = (ArrayList) awsResourceLimitsMap.get("maxLimits");
        limits.forEach((key) -> {
            Map regionItem = (LinkedHashMap) key;
            regionItem.forEach((regionKey, value) -> {
                Map regionInfo = (LinkedHashMap) regionItem.get(regionKey);
                String regionName = regionInfo.get("name").toString();
                ArrayList services = (ArrayList) regionInfo.get("services");
                services.forEach((service) -> {
                    String serviceName = ((LinkedHashMap) service).get("serviceName").toString();
                    ArrayList serviceLimits = (ArrayList) ((LinkedHashMap) service).get("serviceLimits");
                    serviceLimits.forEach((limit) -> {
                        String limitName = ((LinkedHashMap) limit).get("limitName").toString();
                        int maxAllowedLimit = (int) ((LinkedHashMap) limit).get("maxAllowedLimit");

                        //Create an AWSResourceLimit
                        AWSResourceLimit awsResourceLimits = new AWSResourceLimit();
                        awsResourceLimits.setRegion(regionName);
                        awsResourceLimits.setServiceName(serviceName);
                        awsResourceLimits.setLimitName(limitName);
                        awsResourceLimits.setMaxAllowedLimit(maxAllowedLimit);
                        awsResourceLimitsList.add(awsResourceLimits);
                    });
                });
            });
        });
        return awsResourceLimitsList;
    }

    /**
     * Obtains the resource requirements for a particular test plan based on its deployment pattern.
     *
     * @param cfnMD5Hash MD5 hash value of the cloudformation script
     * @return an optional list of {@link AWSResourceRequirement}
     * @throws TestGridDAOException if retrieving information from the database failed
     */
    private List<AWSResourceRequirement> getResourceRequirements(String cfnMD5Hash)
            throws TestGridDAOException {
        AWSResourceRequirementUOW awsResourceRequirementUOW = new AWSResourceRequirementUOW();
        Map<String, Object> params = new HashMap<>();
        params.put(AWSResourceRequirement.MD5_HASH_COLUMN, cfnMD5Hash);
        return awsResourceRequirementUOW.findByFields(params);
    }

    /**
     * Creates a list of required resources using the stack events list.
     *
     * @param stackEventList list of stack events
     * @param cfnMD5hash MD5 of the cloudformation script
     * @return list of required resources
     */
    private List<AWSResourceRequirement> createRequiredResourcesList(
            List<StackEvent> stackEventList, String cfnMD5hash) {
        List<AWSResourceRequirement> resourceRequirementList = new ArrayList<>();
        AWSResourceRequirement resourceRequirement;
        for (StackEvent stackEvent : stackEventList) {
            resourceRequirement = new AWSResourceRequirement();
            resourceRequirement.setCfnMD5Hash(cfnMD5hash);

            if (EC2_RESOURCE_IDENTIFIER.equalsIgnoreCase(stackEvent.getResourceType()) &&
                    String.valueOf(CREATE_COMPLETE).equals(stackEvent.getResourceStatus())) {
                //Parse resource properties to get limit name
                String eventProperties = stackEvent.getResourceProperties();
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(eventProperties);
                String limitName = element.getAsJsonObject().get("InstanceType").getAsString();

                resourceRequirement.setServiceName(EC2_SERVICE_NAME);
                resourceRequirement.setLimitName(limitName);
                resourceRequirementList.add(resourceRequirement);

                //Add resource requirement to the list if does not contain already
                if (resourceRequirementList.isEmpty()
                        || !isListContainResource(resourceRequirementList, resourceRequirement)) {
                    resourceRequirement.setRequiredCount(1);
                    resourceRequirementList.add(resourceRequirement);
                }

            } else if (VPC_RESOURCE_IDENTIFIER.equalsIgnoreCase(stackEvent.getResourceType())) {
                resourceRequirement.setServiceName(NETWORKING_SERVICE_NAME);
                resourceRequirement.setLimitName(VPC_LIMIT_NAME);

                //Add resource requirement to the list if does not contain already
                if (resourceRequirementList.isEmpty()
                        || !isListContainResource(resourceRequirementList, resourceRequirement)) {
                    resourceRequirement.setRequiredCount(1);
                    resourceRequirementList.add(resourceRequirement);
                }
            }
        }
        return resourceRequirementList;
    }

    private boolean isListContainResource(
            List<AWSResourceRequirement> resourceRequirementList, AWSResourceRequirement resourceRequirement) {
        for (AWSResourceRequirement awsResourceRequirement : resourceRequirementList) {
            if (awsResourceRequirement.getServiceName().equals(resourceRequirement.getServiceName())
            && awsResourceRequirement.getLimitName().equals(resourceRequirement.getLimitName())) {
                awsResourceRequirement.setRequiredCount(awsResourceRequirement.getRequiredCount() + 1);
                return true;
            }
        }
        return false;
    }
}
