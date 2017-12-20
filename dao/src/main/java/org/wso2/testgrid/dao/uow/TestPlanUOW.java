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
package org.wso2.testgrid.dao.uow;

import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.TestPlanRepository;

import javax.persistence.EntityManager;

/**
 * This class defines the Unit of work related to a {@link TestPlan}.
 *
 * @since 1.0.0
 */
public class TestPlanUOW {

    private final TestPlanRepository testPlanRepository;

    /**
     * Constructs an instance of {@link TestPlanUOW} to manager use cases related to test plans.
     */
    public TestPlanUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        testPlanRepository = new TestPlanRepository(entityManager);
    }

    /**
     * This method persists a single {@link TestPlan} object to the database.
     *
     * @param testPlan Populated TestPlan object
     * @return The persisted TestPlan object with additional details added
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public TestPlan persistTestPlan(TestPlan testPlan) throws TestGridDAOException {
        TestPlan persisted = testPlanRepository.persist(testPlan);
        return persisted;
    }

    /**
     * Returns the {@link TestPlan} instance for the given id.
     *
     * @param id primary key of the test plan
     * @return matching {@link TestPlan} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public TestPlan getTestPlanById(String id) throws TestGridDAOException {
        return testPlanRepository.findByPrimaryKey(id);
    }
}
