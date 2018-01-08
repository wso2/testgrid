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
package org.wso2.testgrid.dao;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.wso2.testgrid.common.util.StringUtil;

import java.sql.Connection;
import java.util.Date;
import javax.persistence.EntityManager;

/**
 * This class is responsible for handling DB migrations.
 *
 * @since 1.0.0
 */
public class DBMigrator {

    private static final String DB_NAME = "testgriddb";

    /**
     * Migrates the DB of the given entity manager using the master DB change log (all migrations will be applied).
     *
     * @param entityManager entity manager to get the database connection from
     * @throws TestGridMigrationException thrown when error on migrating database
     */
    public void migrateDB(EntityManager entityManager) throws TestGridMigrationException {
        migrateDB(entityManager, "db-changelog-master.xml");
    }

    /**
     * Migrates the DB of the given entity manager using the given change log.
     *
     * @param entityManager entity manager to get the database connection from
     * @param changeLogFile change log file of the DB to refer for the migration
     * @throws TestGridMigrationException thrown when error on migrating database
     */
    public void migrateDB(EntityManager entityManager, String changeLogFile)
            throws TestGridMigrationException {
        try {
            Liquibase liquibase = createLiquibase(entityManager, changeLogFile);
            liquibase.update(DB_NAME);
        } catch (LiquibaseException e) {
            throw new TestGridMigrationException(StringUtil
                    .concatStrings("Error on migrating database using DB change log ", changeLogFile), e);
        }
    }

    /**
     * Rollbacks the DB of the given entity manager using the given change log.
     *
     * @param entityManager entity manager to get the database connection from
     * @param changeLogFile change log file of the DB to refer for the migration
     * @param rollbackDate  date to roll back the DB
     * @throws TestGridMigrationException thrown when error on rolling back
     */
    public void rollbackDB(EntityManager entityManager, String changeLogFile, Date rollbackDate)
            throws TestGridMigrationException {
        try {
            Liquibase liquibase = createLiquibase(entityManager, changeLogFile);
            liquibase.rollback(rollbackDate, DB_NAME);
        } catch (LiquibaseException e) {
            throw new TestGridMigrationException(StringUtil
                    .concatStrings("Error on migrating database using DB change log ", changeLogFile), e);
        }
    }

    /**
     * Creates and returns a Liquibase instance for the given entity manager and DB change log file.
     *
     * @param entityManager entity manager to get the database connection from
     * @param changeLogFile change log file of the DB to refer for the migration
     * @return created Liquibase instance
     * @throws TestGridMigrationException thrown when error on creating a Liquibase instance
     */
    private Liquibase createLiquibase(EntityManager entityManager, String changeLogFile)
            throws TestGridMigrationException {
        try {
            entityManager.getTransaction().begin();
            Connection connection = entityManager.unwrap(Connection.class);
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            entityManager.getTransaction().commit();
            return new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database);
        } catch (LiquibaseException e) {
            throw new TestGridMigrationException("Error on creating an Liquibase instance from DB connection.", e);
        }
    }
}
