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

    private static final EntityManagerFactory entityManagerFactory;
    private static final ThreadLocal<EntityManager> threadLocal;
    private static final String TESTGRID_PERSISTENT_UNIT = "eclipse_link_jpa";

    /*
      Initialises the entity manager factory.
     */
    static {
        entityManagerFactory = Persistence.createEntityManagerFactory(TESTGRID_PERSISTENT_UNIT);
        threadLocal = new ThreadLocal<>();
    }

    /**
     * Returns a thread safe {@link EntityManager}.
     *
     * @return thread safe {@link EntityManager}
     */
    public static EntityManager getEntityManager() {
        EntityManager entityManager = threadLocal.get();
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.setFlushMode(FlushModeType.COMMIT); // Flushing will happen on committing the transaction.
            threadLocal.set(entityManager);
        }
        return entityManager;
    }

    /**
     * Closes the entity manager.
     */
    public static void closeEntityManager() {
        EntityManager entityManager = threadLocal.get();
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        threadLocal.set(null);
    }

    /**
     * Closes the entity manager factory.
     */
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
