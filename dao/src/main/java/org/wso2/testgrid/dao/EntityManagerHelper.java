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

import java.util.HashMap;
import java.util.Map;
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

    private static final ThreadLocal<Map<String, EntityManager>> entityManagerThreadLocal;
    private static final ThreadLocal<Map<String, EntityManagerFactory>> entityManagerFactoryThreadLocal;
    private static final String TESTGRID_PU_MYSQL = "testgrid_mysql";

    static {
        // Entity manager map
        Map<String, EntityManager> entityManagerMap = new HashMap<>();
        entityManagerThreadLocal = new ThreadLocal<>();
        entityManagerThreadLocal.set(entityManagerMap);

        // Entity manager factory
        Map<String, EntityManagerFactory> entityManagerFactoryMap = new HashMap<>();
        entityManagerFactoryThreadLocal = new ThreadLocal<>();
        entityManagerFactoryThreadLocal.set(entityManagerFactoryMap);
    }

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
        EntityManager entityManager = entityManagerThreadLocal.get().get(persistenceUnitName);
        if (entityManager == null || !entityManager.isOpen()) {
            EntityManagerFactory entityManagerFactory = getEntityManagerFactory(persistenceUnitName);
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.setFlushMode(FlushModeType.COMMIT); // Flushing will happen on committing the transaction.
            entityManagerThreadLocal.get().put(persistenceUnitName, entityManager);
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
        EntityManager entityManager = entityManagerThreadLocal.get().get(persistenceUnitName);
        if (entityManager != null) {
            entityManagerThreadLocal.get().remove(persistenceUnitName);
        }

        // Close and remove entity manager factory. This closes the entity manager automatically
        EntityManagerFactory entityManagerFactory = entityManagerFactoryThreadLocal.get().get(persistenceUnitName);
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactoryThreadLocal.get().remove(persistenceUnitName);
        }
    }

    /**
     * Returns the entity manager factory for the given persistence unit name.
     *
     * @param persistenceUnitName persistence unit name of the entity manager factory
     * @return entity manager factory with the given persistence unit name
     */
    private static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName) {
        EntityManagerFactory entityManagerFactory = entityManagerFactoryThreadLocal.get().get(persistenceUnitName);
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
            entityManagerFactoryThreadLocal.get().put(persistenceUnitName, entityManagerFactory);
        }
        return entityManagerFactory;
    }
}
