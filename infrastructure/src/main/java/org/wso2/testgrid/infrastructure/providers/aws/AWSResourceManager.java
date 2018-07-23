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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.TestGridRuntimeException;
import org.wso2.testgrid.common.infrastructure.AWSResourceLimit;
import org.wso2.testgrid.common.infrastructure.DeploymentPatternResourceUsage;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.AWSResourceLimitUOW;
import org.wso2.testgrid.dao.uow.DeploymentPatternResourceUsageUOW;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amazonaws.services.cloudformation.model.ResourceStatus.CREATE_COMPLETE;

/**
 * Used to manage AWS resources to allocate regions for test plans based on the resource availability.
 * This class is responsible of checking and allocating AWS resources before a test plan is run and
 * releasing resources once the test plan has finished running.
 */
public class AWSResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(AWSResourceManager.class);

    private AWSResourceLimitUOW awsResourceUOW;
    private static final String EC2_RESOURCE_IDENTIFIER = "AWS::EC2::Instance";
    private static final String VPC_RESOURCE_IDENTIFIER = "AWS::EC2::VPC";
    private static final String EC2_SERVICE_NAME = "ec2";
    private static final String NETWORKING_SERVICE_NAME = "networking";
    private static final String VPC_LIMIT_NAME = "vpc";

    public AWSResourceManager() {
        awsResourceUOW = new AWSResourceLimitUOW();
    }

    /**
     * Parses the limits.yaml and returns the AWS resources and limits.
     * This is used to populate the initial AWSResourceLimits
     *
     * @param limitFilePath path to limits.yaml file
     * @return AWSResourceLimits instance
     * @throws IOException if reading limits.yaml fails
     * @throws TestGridInfrastructureException if error occurs while retrieving AWS resource limits
     */
    public AWSResourceLimit getInitialResourceLimits(Path limitFilePath) throws TestGridInfrastructureException {
        String yamlContent;
        try {
            yamlContent = new String(Files.readAllBytes(limitFilePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TestGridRuntimeException("Error while reading yaml content.");
        }
        Representer representer = new Representer();

        // Skip missing properties in testgrid.yaml
        representer.getPropertyUtils().setSkipMissingProperties(true);
        AWSResourceLimit awsResourceLimits = new Yaml(new Constructor(AWSResourceLimit.class), representer)
                .loadAs(yamlContent, AWSResourceLimit.class);
        if (awsResourceLimits == null) {
            throw new TestGridInfrastructureException(
                    "Error occurred while parsing" + limitFilePath.toString());
        }
        return awsResourceLimits;
    }

    /**
     * Retrieves and available region to run a test plan.
     *
     * @param resourceRequirements resources requirements of a test plan
     * @return available region
     * @throws TestGridDAOException if retrieving information from the database failed
     */
    public String getAvailableRegion(List<DeploymentPatternResourceUsage> resourceRequirements)
            throws TestGridDAOException {

        if (!awsResourceUOW.getAWSRegions().isPresent()) {
            throw new TestGridDAOException("No AWS region information found in the database");
        }
        List<String> regions = awsResourceUOW.getAWSRegions().get();
        for (String region : regions) {
            boolean isRegionAvailable = true;
            for (DeploymentPatternResourceUsage resourceRequirement : resourceRequirements) {
                AWSResourceLimit awsResourceLimit = awsResourceUOW.getAvailableResource(resourceRequirement, region);
                if (awsResourceLimit == null) {
                    isRegionAvailable = false;
                    break;
                }
            }
            if (isRegionAvailable) {
                return region;
            }
        }
        return null;
    }

    /**
     * Allocates the required resources for a test plan.
     *
     * @param resourceRequirements resourde requirements of a test plan
     * @param region region to allocate resources from
     * @throws TestGridDAOException if retrieving information from the database failed
     */
    public void allocateResources(List<DeploymentPatternResourceUsage> resourceRequirements, String region)
            throws TestGridDAOException {
        logger.info("Allocating resources for test plan...");
        for (DeploymentPatternResourceUsage resourceRequirement : resourceRequirements) {
            Map<String, Object> params = new HashMap<>();
            params.put(AWSResourceLimit.REGION_COLUMN, region);
            Optional<List<AWSResourceLimit>> awsResourceLimits = awsResourceUOW.getAWSResourceByFields(params);
            for (AWSResourceLimit awsResourceLimit : awsResourceLimits.get()) {
                if (resourceRequirement.getServiceName().equals(awsResourceLimit.getServiceName())
                        && resourceRequirement.getLimitName().equals(awsResourceLimit.getLimitName())) {
                    awsResourceLimit.setCurrentUsage(
                            awsResourceLimit.getCurrentUsage() + resourceRequirement.getRequiredCount());
                    if (awsResourceUOW.updateAWSResource(awsResourceLimit) == null) {
                        throw new TestGridDAOException(
                                "Error occurred while allocating AWS resources. Updating the database failed.");
                    }
                }
            }
        }
    }

    /**
     * Releases the allocated resources of a test plan.
     *
     * @param resourceRequirements resource requirements
     * @param region region to release the resources
     * @throws TestGridDAOException if retrieving information from the database failed
     */
    public void releaseResources(List<DeploymentPatternResourceUsage> resourceRequirements, String region)
            throws TestGridDAOException {
        logger.info("Releasing resources from test plan...");
        for (DeploymentPatternResourceUsage resourceRequirement : resourceRequirements) {
            Map<String, Object> params = new HashMap<>();
            params.put(AWSResourceLimit.REGION_COLUMN, region);
            List<AWSResourceLimit> awsResourceLimits = awsResourceUOW.getAWSResourceByFields(params).get();
            for (AWSResourceLimit awsResourceLimit : awsResourceLimits) {
                if (resourceRequirement.getServiceName().equals(awsResourceLimit.getServiceName())
                        && resourceRequirement.getLimitName().equals(awsResourceLimit.getLimitName())) {
                    awsResourceLimit.setCurrentUsage(
                            awsResourceLimit.getCurrentUsage() - resourceRequirement.getRequiredCount());
                    if (awsResourceUOW.updateAWSResource(awsResourceLimit) == null) {
                        throw new TestGridDAOException(
                                "Error occurred while releasing AWS resources. Updating the database failed.");
                    }
                }
            }
        }
    }

    /**
     * Obtains the resource requirements for a particular test plan based on its deployment pattern.
     * @param deploymentPattern deployment pattern of th test plan
     * @return an optional list of {@link DeploymentPatternResourceUsage}
     * @throws TestGridDAOException if retrieving information from the database failed
     */
    public Optional<List<DeploymentPatternResourceUsage>> getResourceRequirements(DeploymentPattern deploymentPattern)
            throws TestGridDAOException {

        DeploymentPatternResourceUsageUOW testPlanResourceUsageUOW = new DeploymentPatternResourceUsageUOW();
        Map<String, Object> params = new HashMap<>();
        params.put(DeploymentPatternResourceUsage.DEPLOYMENT_PATTERN_COLUMN, deploymentPattern);
        return testPlanResourceUsageUOW.getDeploymentPatternResourceUsageByFields(params);

    }

    /**
     * Persist resource requirements of a given deployment pattern. This will be used after the first test plan of
     * the given deployment pattern is run or if the md5 of the CFN of the deployement pattern has changed.
     *
     * @param stackEventsList the list of events in the created stack used identify resource requirements
     * @param deploymentPattern deployment pattern of the test plan ran
     * @param filePath path to CFN script
     * @return if persisting resource requirements succeeded or not
     * @throws TestGridDAOException if persisting to database fails
     * @throws IOException if retrieving CFN hash fails
     * @throws NoSuchAlgorithmException if retrieving CFN hash fails
     */
    public boolean persistResourceRequirements(
            List<StackEvent> stackEventsList, DeploymentPattern deploymentPattern, Path filePath)
            throws TestGridDAOException, IOException, NoSuchAlgorithmException {

        List<DeploymentPatternResourceUsage> resourceUsageList = new ArrayList<>();
        DeploymentPatternResourceUsageUOW deploymentPatternResourceUsageUOW = new DeploymentPatternResourceUsageUOW();
        Optional<List<DeploymentPatternResourceUsage>> existingResourceUsages =
                deploymentPatternResourceUsageUOW.getResourceUsages(deploymentPattern);

        //Get resource list if already exists
        if (existingResourceUsages.isPresent() && !existingResourceUsages.get().isEmpty()) {
            //Set the required resource count to zero
            resetResourceRequirements(existingResourceUsages.get());
            resourceUsageList = existingResourceUsages.get();
        }
        for (StackEvent stackEvent : stackEventsList) {
            DeploymentPatternResourceUsage resourceUsage = new DeploymentPatternResourceUsage();
            resourceUsage.setDeploymentPattern(deploymentPattern);

            if (EC2_RESOURCE_IDENTIFIER.equalsIgnoreCase(stackEvent.getResourceType()) &&
                    String.valueOf(CREATE_COMPLETE).equals(stackEvent.getResourceStatus())) {
                //Parse resource properties to get limit name
                String eventProperties = stackEvent.getResourceProperties();
                final JSONObject jsonObject = new JSONObject(eventProperties);
                String limitName = jsonObject.getString("InstanceType");

                resourceUsage.setServiceName(EC2_SERVICE_NAME);
                resourceUsage.setLimitName(limitName);

                //Add resource usages to a list if does not contain already
                if (resourceUsageList.isEmpty()
                        || !isListContainResource(resourceUsageList, resourceUsage)) {
                    resourceUsage.setRequiredCount(1);
                    resourceUsageList.add(resourceUsage);
                }

            } else if (VPC_RESOURCE_IDENTIFIER.equalsIgnoreCase(stackEvent.getResourceType())) {
                resourceUsage.setServiceName(NETWORKING_SERVICE_NAME);
                resourceUsage.setLimitName(VPC_LIMIT_NAME);

                //Add resource usages to a list if does not contain already
                if (resourceUsageList.isEmpty()
                        || !isListContainResource(resourceUsageList, resourceUsage)) {
                    resourceUsage.setRequiredCount(1);
                    resourceUsageList.add(resourceUsage);
                }
            }
        }

        DeploymentPatternUOW deploymentPatternUOW = new DeploymentPatternUOW();
        JSONObject hashValue = new JSONObject();
        hashValue.put(DeploymentPattern.MD5_HASH_PROPERTY, TestGridUtil.getHashValue(filePath));
        deploymentPattern.setProperties(hashValue.toString());

        if (deploymentPatternUOW.updateDeploymentPattern(deploymentPattern) == null) {
            throw new TestGridDAOException("Error occurred while persisting the MD5 hash of deployment pattern.");
        }
        for (DeploymentPatternResourceUsage resourceUsage : resourceUsageList) {
            if (deploymentPatternResourceUsageUOW.persist(resourceUsage) != null) {
                return true;
            }
        }

        return false;
    }

    private void resetResourceRequirements(List<DeploymentPatternResourceUsage> existingResourceUsages) {
        for (DeploymentPatternResourceUsage resourceUsage : existingResourceUsages) {
            resourceUsage.setRequiredCount(0);
        }
    }

    private boolean isListContainResource(
            List<DeploymentPatternResourceUsage> resourceUsageList, DeploymentPatternResourceUsage resourceUsage) {
        for (DeploymentPatternResourceUsage deploymentPatternResourceUsage : resourceUsageList) {
            if (deploymentPatternResourceUsage.getServiceName().equals(resourceUsage.getServiceName())
            && deploymentPatternResourceUsage.getLimitName().equals(resourceUsage.getLimitName())) {
                deploymentPatternResourceUsage.setRequiredCount(deploymentPatternResourceUsage.getRequiredCount() + 1);
                return true;
            }
        }
        return false;
    }
}
