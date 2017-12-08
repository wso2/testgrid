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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.dao.uow;

import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.TestScenarioRepository;
import org.wso2.testgrid.dao.util.DAOUtil;

import java.io.Closeable;
import javax.persistence.EntityManagerFactory;

/**
 * Unit of work class to handle to data base transactions related to {@link TestScenario}.
 *
 * @since 1.0.0
 */
public class TestScenarioUOW implements Closeable {

    private final TestScenarioRepository testScenarioRepository;

    /**
     * Constructs an instance of {@link TestScenarioUOW} to manager use cases related to product test plan.
     */
    public TestScenarioUOW() {
        EntityManagerFactory entityManagerFactory = DAOUtil.getEntityManagerFactory();
        testScenarioRepository = new TestScenarioRepository(entityManagerFactory);
    }

    /**
     * Persists (adds if not exists, updates otherwise) an {@link TestScenario} instance.
     *
     * @param testScenario {@link TestScenario} instance tp be persisted
     * @param status       status of the test scenario
     * @return the persisted {@link TestScenario} instance
     * @throws TestGridDAOException thrown when error on persisting the {@link TestScenario} instance
     */
    public TestScenario persistTestScenario(TestScenario testScenario, TestScenario.Status status)
            throws TestGridDAOException {
        testScenario.setStatus(status);
        return testScenarioRepository.persist(testScenario);
    }

    /**
     * Returns the {@link TestScenario} instance for the given id.
     *
     * @param id primary key of the test scenario
     * @return matching {@link TestScenario} instance
     * @throws TestGridDAOException thrown when error on retrieving results
     */
    public TestScenario getTestScenarioById(String id) throws TestGridDAOException {
        return testScenarioRepository.findByPrimaryKey(id);
    }

    @Override
    public void close() {
        testScenarioRepository.close();
    }
}
