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

import org.wso2.testgrid.common.infrastructure.AWSResourceRequirement;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.AWSResourceRequirementRepository;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * This class defines the Unit of work related to a {@link AWSResourceRequirement}.
 *
 * @since 1.0.0
 */
public class AWSResourceRequirementUOW {

    private final AWSResourceRequirementRepository awsResourceRequirementRepository;

    /**
     * Constructs an instance of {@link AWSResourceRequirementUOW} to manage use cases related to
     * resource usages of deployment patterns.
     */
    public AWSResourceRequirementUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        awsResourceRequirementRepository = new AWSResourceRequirementRepository(entityManager);
    }

    /**
     * This method persists a {@link AWSResourceRequirement} to the database.
     *
     * @param awsResourceRequirement    an instance of AWSResourceLimit
     *
     * @return persisted {@link AWSResourceRequirement} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public AWSResourceRequirement persist(AWSResourceRequirement awsResourceRequirement)
            throws TestGridDAOException {
        return awsResourceRequirementRepository.persist(awsResourceRequirement);
    }

    /**
     * Returns a List of {@link AWSResourceRequirement}.
     *
     * @return a List of all available {@link AWSResourceRequirement}
     */
    public List<AWSResourceRequirement> findAll()
            throws TestGridDAOException {
        return awsResourceRequirementRepository.findAll();
    }

    /**
     * Returns a list of distinct {@link AWSResourceRequirement} instances for a given set of search fields.
     *
     * @param params map of search parameters
     *
     * @return matching distinct {@link AWSResourceRequirement} instances
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public List<AWSResourceRequirement> findByFields(Map<String, Object> params) throws TestGridDAOException {
        return awsResourceRequirementRepository.findByFields(params);
    }

    /**
     * Persists a list of given AWS resource requirements.
     *
     * @param resourceRequirementList list of required resources to persist
     * @throws TestGridDAOException
     */
    public void persistResourceRequirements(
            List<AWSResourceRequirement> resourceRequirementList)
            throws TestGridDAOException {
        awsResourceRequirementRepository.persistResourceRequirements(resourceRequirementList);
    }
}
