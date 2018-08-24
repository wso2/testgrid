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
import org.wso2.testgrid.common.infrastructure.AWSResourceRequirement;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.AWSResourceLimitsRepository;

import java.util.List;
import java.util.Map;
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
     * Returns all the entries from the AWSResourceLimit table.
     *
     * @return List all the entries from the table matching the given entity type
     * @throws TestGridDAOException thrown when error on searching for entity
     */
    public List<AWSResourceLimit> findAll() throws TestGridDAOException {
        return awsResourceLimitsRepository.findAll();
    }

    /**
     * Returns a list of distinct {@link AWSResourceLimit} instances for the given product id and date.
     *
     * @param params parameter map of search fields
     *
     * @return matching distinct {@link AWSResourceLimit} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public List<AWSResourceLimit> findByFields(Map<String, Object> params)
            throws TestGridDAOException {
        return awsResourceLimitsRepository.findByFields(params);
    }

    /**
     * Return an available region on AWS for the given resource requirements
     *
     * @param resourceRequirements AWS resource requirement list
     * @return available AWS region
     * @throws TestGridDAOException thrown when error on retrieving region
     */
    public String getAvailableRegion(List<AWSResourceRequirement> resourceRequirements) throws TestGridDAOException {
        return awsResourceLimitsRepository.getAvailableRegion(resourceRequirements);
    }

    /**
     * Persist initial resource limits for all AWS resources given in awsLimits.yaml.
     *
     * @param awsResourceLimitsList list of aws resources
     * @return persisted AWS resource limits list
     * @throws TestGridDAOException if persisting resources to database fails
     */
    public List<AWSResourceLimit> persistInitialLimits(
            List<AWSResourceLimit> awsResourceLimitsList) throws TestGridDAOException {
        return awsResourceLimitsRepository.persistInitialLimits(awsResourceLimitsList);
    }

    /**
     * Release acquired resources.
     *
     * @param awsResourceRequirementList aws resource requirement list
     * @param region region to release resources from
     * @throws TestGridDAOException if persisting resourced fails
     */
    public void releaseResources(List<AWSResourceRequirement> awsResourceRequirementList, String region)
            throws TestGridDAOException {
        awsResourceLimitsRepository.releaseResources(awsResourceRequirementList, region);
    }
}
