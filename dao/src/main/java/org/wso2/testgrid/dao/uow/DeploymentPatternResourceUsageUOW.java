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

import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.infrastructure.DeploymentPatternResourceUsage;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.DeploymentPatternResourceUsageRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;

/**
 * This class defines the Unit of work related to a {@link DeploymentPatternResourceUsage}.
 *
 * @since 1.0.0
 */
public class DeploymentPatternResourceUsageUOW {

    private final DeploymentPatternResourceUsageRepository testPlanResourceUsageRepository;

    /**
     * Constructs an instance of {@link DeploymentPatternResourceUsageUOW} to manage use cases related to
     * resource usages of deployment patterns.
     */
    public DeploymentPatternResourceUsageUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        testPlanResourceUsageRepository = new DeploymentPatternResourceUsageRepository(entityManager);
    }

    /**
     * This method persists a {@link DeploymentPatternResourceUsage} to the database.
     *
     * @param deploymentPatternResourceUsage    an instance of AWSResourceLimit
     *
     * @return persisted {@link DeploymentPatternResourceUsage} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public DeploymentPatternResourceUsage persist(DeploymentPatternResourceUsage deploymentPatternResourceUsage)
            throws TestGridDAOException {
        return testPlanResourceUsageRepository.persist(deploymentPatternResourceUsage);
    }

    /**
     * Returns a List of {@link DeploymentPatternResourceUsage}.
     *
     * @return a List of all available {@link DeploymentPatternResourceUsage}
     */
    public List<DeploymentPatternResourceUsage> getAllDeploymentPatternResourceUsages()
            throws TestGridDAOException {
        return testPlanResourceUsageRepository.findAll();
    }

    /**
     * Returns a list of distinct {@link DeploymentPatternResourceUsage} instances for a given deployment pattern.
     *
     * @param deploymentPattern deployment pattern
     *
     * @return matching distinct {@link DeploymentPatternResourceUsage} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public Optional<List<DeploymentPatternResourceUsage>> getResourceUsages(DeploymentPattern deploymentPattern)
            throws TestGridDAOException {
        // Search criteria parameters
        Map<String, Object> params = new HashMap<>();
        params.put(DeploymentPatternResourceUsage.DEPLOYMENT_PATTERN_COLUMN, deploymentPattern);

        return Optional.of(testPlanResourceUsageRepository.findByFields(params));
    }

    /**
     * Returns a list of distinct {@link DeploymentPatternResourceUsage} instances for a given set of search fields.
     *
     * @param params map of search parameters
     *
     * @return matching distinct {@link DeploymentPatternResourceUsage} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public Optional<List<DeploymentPatternResourceUsage>> getDeploymentPatternResourceUsageByFields(
            Map<String, Object> params) throws TestGridDAOException {
        return Optional.of(testPlanResourceUsageRepository.findByFields(params));
    }
}
