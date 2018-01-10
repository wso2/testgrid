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
package org.wso2.testgrid.core.command;

import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.dao.DBMigrator;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridMigrationException;

import javax.persistence.EntityManager;

/**
 * This class is responsible for handling tasks related to DB migrations.
 *
 * @since 1.0.0
 */
public class DBMigrationCommand implements Command {

    @Override
    public void execute() throws CommandExecutionException {
        try {
            EntityManager entityManager = EntityManagerHelper.getEntityManager();
            DBMigrator dbMigrator = new DBMigrator();
            dbMigrator.migrateDB(entityManager);
        } catch (TestGridMigrationException e) {
            throw new CommandExecutionException("Error in migrating DB to latest version.", e);
        }
    }
}
