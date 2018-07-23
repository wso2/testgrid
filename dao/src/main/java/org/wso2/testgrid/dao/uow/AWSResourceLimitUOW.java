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
package org.wso2.testgrid.dao.uow;

import org.wso2.testgrid.common.infrastructure.AWSResourceLimit;
import org.wso2.testgrid.common.infrastructure.DeploymentPatternResourceUsage;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.AWSResourceLimitsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;

/**
 * This class defines the Unit of work related to a {@link AWSResourceLimit}.
 *
 * @since 1.0.0
 */
public class AWSResourceLimitUOW {

    private final AWSResourceLimitsRepository awsResourceLimitsRepository;

    /**
     * Constructs an instance of {@link AWSResourceLimitUOW} to manager use cases related to test cases.
     */
    public AWSResourceLimitUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        awsResourceLimitsRepository = new AWSResourceLimitsRepository(entityManager);
    }

    /**
     * This method persists a {@link AWSResourceLimit} to the database.
     *
     * @param awsResource    an instance of AWSResourceLimit
     * @return persisted {@link AWSResourceLimit} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public AWSResourceLimit persistAWSResource(AWSResourceLimit awsResource) throws TestGridDAOException {

        Optional<AWSResourceLimit> awsResourceLimitOptional = getAWSResource(awsResource);
        if (awsResourceLimitOptional.isPresent()) {
            return awsResourceLimitOptional.get();
        }

        // Persist resource limits if it doesn't exist already.
        return awsResourceLimitsRepository.persist(awsResource);
    }

    /**
     * This method updates an existing {@link AWSResourceLimit} in the database.
     *
     * @param awsResource    an instance of AWSResourceLimit to update
     * @return persisted {@link AWSResourceLimit} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public AWSResourceLimit updateAWSResource(AWSResourceLimit awsResource) throws TestGridDAOException {
        return awsResourceLimitsRepository.persist(awsResource);
    }

    /**
     * Returns a List of {@link AWSResourceLimit}.
     *
     * @return a List of all available {@link AWSResourceLimit}
     */
    public List<AWSResourceLimit> getAllAWSResource() throws TestGridDAOException {
        return awsResourceLimitsRepository.findAll();
    }

    /**
     * Returns a {@link AWSResourceLimit} instance if exists.
     *
     * @param awsResource aws resource object
     *
     * @return matching {@link AWSResourceLimit} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public Optional<AWSResourceLimit> getAWSResource(AWSResourceLimit awsResource)
            throws TestGridDAOException {
        // Search criteria parameters
        Map<String, Object> params = new HashMap<>();
        params.put(AWSResourceLimit.REGION_COLUMN, awsResource.getRegion());
        params.put(AWSResourceLimit.SERVICE_NAME_COLUMN, awsResource.getServiceName());
        params.put(AWSResourceLimit.LIMIT_NAME_COLUMN, awsResource.getLimitName());

        List<AWSResourceLimit> optionalAwsResource = awsResourceLimitsRepository.findByFields(params);
        if (optionalAwsResource.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(optionalAwsResource.get(0));
    }

    /**
     * Returns a list of distinct {@link AWSResourceLimit} instances for the given product id and date.
     *
     * @param params parameter map of search fields
     *
     * @return matching distinct {@link AWSResourceLimit} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public Optional<List<AWSResourceLimit>> getAWSResourceByFields(Map<String, Object> params)
            throws TestGridDAOException {

        List<AWSResourceLimit> optionalAwsResource = awsResourceLimitsRepository.findByFields(params);
        if (optionalAwsResource.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(optionalAwsResource);
    }

    /**
     * Returns a list of distinct AWS regions.
     *
     * @return matching distinct AWS regions
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public Optional<List<String>> getAWSRegions()
            throws TestGridDAOException {
        // Search criteria parameters

        List<String> regions = awsResourceLimitsRepository.findRegions();
        if (regions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(regions);
    }

    public AWSResourceLimit getAvailableResource (DeploymentPatternResourceUsage resourceUsage, String region)
            throws TestGridDAOException {
        return awsResourceLimitsRepository.getAvailableResource(resourceUsage, region);
    }
}
