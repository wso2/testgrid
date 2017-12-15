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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Database;
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.OperatingSystem;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.DatabaseRepository;
import org.wso2.testgrid.dao.repository.InfraCombinationRepository;
import org.wso2.testgrid.dao.repository.OperatingSystemRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * This class defines the Unit of work related to a {@link InfraCombination}.
 *
 * @since 1.0.0
 */
public class InfraCombinationUOW {

    private static final Logger logger = LoggerFactory.getLogger(InfraCombinationUOW.class);

    private final InfraCombinationRepository infraCombinationRepository;
    private final OperatingSystemRepository operatingSystemRepository;
    private final DatabaseRepository databaseRepository;

    /**
     * Constructs an instance of {@link InfraCombinationUOW} to manager use cases related to infra combination.
     */
    public InfraCombinationUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        infraCombinationRepository = new InfraCombinationRepository(entityManager);
        operatingSystemRepository = new OperatingSystemRepository(entityManager);
        databaseRepository = new DatabaseRepository(entityManager);
    }

    /**
     * Returns the {@link InfraCombination} instance from db for the given infra combination.
     *
     * @param infraCombination infra combination
     * @return {@link InfraCombination} instance from db for the given infra combination
     * @throws TestGridDAOException thrown when error on retrieving data
     */
    public InfraCombination getInfraCombination(InfraCombination infraCombination) throws TestGridDAOException {


        // Operating system
        Map<String, Object> operatingSystemParams = new HashMap<>();
        operatingSystemParams.put(OperatingSystem.NAME_COLUMN, infraCombination.getOperatingSystem().getName());
        operatingSystemParams.put(OperatingSystem.VERSION_COLUMN, infraCombination.getOperatingSystem().getVersion());
        List<OperatingSystem> operatingSystems = operatingSystemRepository.findByFields(operatingSystemParams);

        // Database
        Map<String, Object> databaseParams = new HashMap<>();
        databaseParams.put(Database.ENGINE_COLUMN, infraCombination.getDatabase().getEngine());
        databaseParams.put(Database.VERSION_COLUMN, infraCombination.getDatabase().getVersion());
        List<Database> databases = databaseRepository.findByFields(databaseParams);

        if (!operatingSystems.isEmpty()) {
            infraCombination.setOperatingSystem(operatingSystems.get(0)); // Only one OS for given params
        }

        if (!databases.isEmpty()) {
            infraCombination.setDatabase(databases.get(0)); // Only one DB for given params
        }

        // Infra combination do not exist in db
        if (operatingSystems.isEmpty() || databases.isEmpty()) {
            return infraCombination;
        }

        Map<String, Object> infraCombinationParams = new HashMap<>();
        infraCombinationParams.put(InfraCombination.OPERATING_SYSTEM_COLUMN, operatingSystems.get(0));
        infraCombinationParams.put(InfraCombination.DATABASE_COLUMN, databases.get(0));
        infraCombinationParams.put(InfraCombination.JDK_COLUMN, infraCombination.getJdk());
        List<InfraCombination> infraCombinations = infraCombinationRepository.findByFields(infraCombinationParams);
        if (infraCombinations.isEmpty()) {
            logger.info(StringUtil.concatStrings("Infra combinations cannot be located for given params",
                    infraCombinationParams.toString()));
            return infraCombination;
        }
        return infraCombinations.get(0); // Only one DB for given params
    }
}
