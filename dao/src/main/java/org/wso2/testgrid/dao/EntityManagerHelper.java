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
package org.wso2.testgrid.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.util.ConfigurationContext;
import org.wso2.testgrid.common.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

/**
 * This class is responsible of handling operations related to entity manager.
 *
 * @since 1.0.0
 */
public class EntityManagerHelper {
    private static final Logger logger = LoggerFactory.getLogger(EntityManagerHelper.class);
    private static final Map<String, EntityManager> entityManagerMap = new HashMap<>();
    private static final Map<String, EntityManagerFactory> entityManagerFactoryMap = new HashMap<>();
    private static final String TESTGRID_PU_MYSQL = "testgrid_mysql";

    /**
     * Returns a thread safe {@link EntityManager}.
     *
     * @return thread safe {@link EntityManager}
     */
    public static EntityManager getEntityManager() {
        return getEntityManager(TESTGRID_PU_MYSQL);
    }

    /**
     * Returns a thread safe {@link EntityManager}.
     *
     * @return thread safe {@link EntityManager}
     */
    public static EntityManager getEntityManager(String persistenceUnitName) {
        EntityManager entityManager = entityManagerMap.get(persistenceUnitName);
        if (entityManager == null || !entityManager.isOpen()) {
            EntityManagerFactory entityManagerFactory = getEntityManagerFactory(persistenceUnitName);
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.setFlushMode(FlushModeType.COMMIT); // Flushing will happen on committing the transaction.
            entityManagerMap.put(persistenceUnitName, entityManager);
        }
        return entityManager;
    }

    /**
     * Closes the entity manager.
     * <p>
     * This method will close the associated entity manager factory as well.
     *
     * @param persistenceUnitName persistence unit name of the entity manager
     */
    public static void closeEntityManager(String persistenceUnitName) {
        // Remove entity manager from thread local
        EntityManager entityManager = entityManagerMap.get(persistenceUnitName);
        if (entityManager != null) {
            entityManagerMap.remove(persistenceUnitName);
        }

        // Close and remove entity manager factory. This closes the entity manager automatically
        EntityManagerFactory entityManagerFactory = entityManagerFactoryMap.get(persistenceUnitName);
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactoryMap.remove(persistenceUnitName);
        }
    }

    /**
     * Returns the entity manager factory for the given persistence unit name.
     *
     * @param persistenceUnitName persistence unit name of the entity manager factory
     * @return entity manager factory with the given persistence unit name
     */
    private static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName) {

        Map<String, String> persistenceMap = new HashMap<String, String>();
        EntityManagerFactory entityManagerFactory = entityManagerFactoryMap.get(persistenceUnitName);
        if (entityManagerFactory == null) {

                ConfigurationContext.getInstance();
                //Get database info from testgrid-config.properties
                if (!ConfigurationContext.getInstance().equals(Optional.empty())) {
                    String dbUrl = ConfigurationContext.getProperty(Constants.DB_URL);
                    String dbUser = ConfigurationContext.getProperty(Constants.DB_USER);
                    String dbUserPass = ConfigurationContext.getProperty(Constants.DB_USER_PASS);

                    persistenceMap.put("javax.persistence.jdbc.url", dbUrl);
                    persistenceMap.put("javax.persistence.jdbc.user", dbUser);
                    persistenceMap.put("javax.persistence.jdbc.password", dbUserPass);

                    if (dbUrl != null && dbUser != null && dbUserPass != null) {
                        //Override properties taken from persistence.xml
                        entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, persistenceMap);
                    } else {
                        logger.warn(StringUtil.concatStrings(
                                "One or more database properties:{",
                                Constants.DB_URL, ", ", Constants.DB_USER, ", ", Constants.DB_USER_PASS,
                                "}  in testgrid-config.properties are null. Using default properties in persistence.xml"));
                        entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
                    }
                    entityManagerFactoryMap.put(persistenceUnitName, entityManagerFactory);
                }
        }
        return entityManagerFactory;
    }
}
