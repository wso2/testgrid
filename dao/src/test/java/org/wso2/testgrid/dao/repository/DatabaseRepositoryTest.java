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
package org.wso2.testgrid.dao.repository;

import com.google.common.collect.LinkedListMultimap;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.Database;
import org.wso2.testgrid.dao.SortOrder;
import org.wso2.testgrid.dao.TestGridDAOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to test the functionality of {@link DatabaseRepository}.
 *
 * @since 1.0.0
 */
public class DatabaseRepositoryTest {

    private DatabaseRepository databaseRepository;

    @BeforeTest
    public void setUp() {
        databaseRepository = new DatabaseRepository();
    }

    @Test(description = "Test persist data in the repository.")
    public void persistTest() throws TestGridDAOException {
        Database database = new Database();
        database.setEngine(Database.DatabaseEngine.MYSQL);
        database.setVersion("5.7");

        database = databaseRepository.persist(database);
        Assert.assertNotNull(database);
        Assert.assertNotNull(database.getId());

        // Check if record persisted
        Database foundedDatabase = databaseRepository.findByPrimaryKey(database.getId());

        Assert.assertNotNull(foundedDatabase);
        Assert.assertEquals(foundedDatabase.getId(), database.getId());
        Assert.assertEquals(foundedDatabase.getEngine(), database.getEngine());
        Assert.assertEquals(foundedDatabase.getVersion(), database.getVersion());

        // Persist the same instance and check if duplicates
        database.setVersion("5.5");
        databaseRepository.persist(database);

        Map<String, Object> params = Collections.singletonMap(Database.ID_COLUMN, database.getId());
        List<Database> databases = databaseRepository.findByFields(params);
        Assert.assertEquals(databases.size(), 1);
        Assert.assertEquals(databases.get(0).getVersion(), "5.5"); // Successfully updated
    }

    /**
     * Database table has unique references (engine and version). This test will check if this is properly set.
     */
    @Test(description = "Tests the unique references of the database table.",
          dependsOnMethods = "persistTest",
          expectedExceptions = TestGridDAOException.class,
          expectedExceptionsMessageRegExp = "Error occurred when persisting entity in database.")
    public void uniqueReferenceTest() throws TestGridDAOException {
        Database database = new Database();
        database.setEngine(Database.DatabaseEngine.MYSQL);
        database.setVersion("5.5");
        databaseRepository.persist(database);
    }

    @Test(description = "Tests the unique references of the database table.",
          dependsOnMethods = "persistTest")
    public void updateUniqueReferenceTest() throws TestGridDAOException {
        Database database = new Database();
        database.setEngine(Database.DatabaseEngine.MYSQL);
        database.setVersion("5.7");

        // Persist entry
        database = databaseRepository.persist(database);

        // now try to update the entry
        database.setVersion("5.5");

        // Exception is expected only on this line. Other transactions should not throw an exception
        try {
            databaseRepository.persist(database);
        } catch (TestGridDAOException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred when persisting entity in database.");
        }
    }

    @Test(description = "Test find by field in the repository.")
    public void findByField() throws TestGridDAOException {
        // DB 1
        Database database1 = new Database();
        database1.setEngine(Database.DatabaseEngine.H2);
        database1.setVersion("1.0");

        // DB2
        Database database2 = new Database();
        database2.setEngine(Database.DatabaseEngine.H2);
        database2.setVersion("2.0");

        // DB3
        Database database3 = new Database();
        database3.setEngine(Database.DatabaseEngine.H2);
        database3.setVersion("3.0");

        databaseRepository.persist(database1);
        databaseRepository.persist(database2);
        databaseRepository.persist(database3);

        Map<String, Object> params = Collections.singletonMap(Database.ENGINE_COLUMN, Database.DatabaseEngine.H2);
        List<Database> databases = databaseRepository.findByFields(params);
        Assert.assertEquals(databases.size(), 3);
    }

    @Test(description = "Test delete data in the repository.",
          dependsOnMethods = "findByField")
    public void deleteTest() throws TestGridDAOException {
        Map<String, Object> params = new HashMap<>();
        params.put(Database.ENGINE_COLUMN, Database.DatabaseEngine.H2);
        params.put(Database.VERSION_COLUMN, "3.0");

        List<Database> databases = databaseRepository.findByFields(params);
        Assert.assertEquals(databases.size(), 1);

        Database database = databases.get(0);
        databaseRepository.delete(database);

        // Check if deleted
        databases = databaseRepository.findByFields(params);
        Assert.assertEquals(databases.size(), 0);
    }

    @Test(description = "Test find by primary key in the repository.",
          dependsOnMethods = "persistTest")
    public void findByPrimaryKeyTest() throws TestGridDAOException {
        Map<String, Object> params = new HashMap<>();
        params.put(Database.ENGINE_COLUMN, Database.DatabaseEngine.MYSQL);
        params.put(Database.VERSION_COLUMN, "5.5");

        List<Database> databases = databaseRepository.findByFields(params);
        Assert.assertEquals(databases.size(), 1);

        String id = databases.get(0).getId();
        Database database = databaseRepository.findByPrimaryKey(id);

        // Assert founded database
        Assert.assertNotNull(database);
        Assert.assertEquals(database.getEngine(), Database.DatabaseEngine.MYSQL);
        Assert.assertEquals(database.getVersion(), "5.5");
    }

    @Test(description = "Test find all records from the repository.",
          dependsOnMethods = {"persistTest", "findByField", "deleteTest", "updateUniqueReferenceTest"})
    public void findAllTest() throws TestGridDAOException {
        List<Database> databases = databaseRepository.findAll();
        Assert.assertEquals(databases.size(), 4);
    }

    @Test(description = "Tests ordering ascending by multiple columns.",
          dependsOnMethods = {"persistTest", "findByField", "deleteTest", "updateUniqueReferenceTest"})
    public void orderByAscendingTest() {
        Map<String, Object> params = Collections.emptyMap();
        LinkedListMultimap<SortOrder, String> orderFieldMap = LinkedListMultimap.create();
        orderFieldMap.put(SortOrder.ASCENDING, Database.ENGINE_COLUMN);
        orderFieldMap.put(SortOrder.ASCENDING, Database.VERSION_COLUMN);
        List<Database> databases = databaseRepository.orderByFields(params, orderFieldMap);
        Assert.assertEquals(databases.size(), 4);

        // Assert order
        // 1
        Assert.assertEquals(databases.get(0).getEngine(), Database.DatabaseEngine.H2);
        Assert.assertEquals(databases.get(0).getVersion(), "1.0");

        // 2
        Assert.assertEquals(databases.get(1).getEngine(), Database.DatabaseEngine.H2);
        Assert.assertEquals(databases.get(1).getVersion(), "2.0");

        // 3
        Assert.assertEquals(databases.get(2).getEngine(), Database.DatabaseEngine.MYSQL);
        Assert.assertEquals(databases.get(2).getVersion(), "5.5");

        // 4
        Assert.assertEquals(databases.get(3).getEngine(), Database.DatabaseEngine.MYSQL);
        Assert.assertEquals(databases.get(3).getVersion(), "5.7");
    }

    @Test(description = "Tests ordering descending by multiple columns.",
          dependsOnMethods = {"persistTest", "findByField", "deleteTest", "updateUniqueReferenceTest"})
    public void orderByDescendingTest() {
        Map<String, Object> params = Collections.emptyMap();
        LinkedListMultimap<SortOrder, String> orderFieldMap = LinkedListMultimap.create();
        orderFieldMap.put(SortOrder.DESCENDING, Database.ENGINE_COLUMN);
        orderFieldMap.put(SortOrder.DESCENDING, Database.VERSION_COLUMN);
        List<Database> databases = databaseRepository.orderByFields(params, orderFieldMap);
        Assert.assertEquals(databases.size(), 4);

        // Assert order
        // 4
        Assert.assertEquals(databases.get(0).getEngine(), Database.DatabaseEngine.MYSQL);
        Assert.assertEquals(databases.get(0).getVersion(), "5.7");

        // 3
        Assert.assertEquals(databases.get(1).getEngine(), Database.DatabaseEngine.MYSQL);
        Assert.assertEquals(databases.get(1).getVersion(), "5.5");

        // 2
        Assert.assertEquals(databases.get(2).getEngine(), Database.DatabaseEngine.H2);
        Assert.assertEquals(databases.get(2).getVersion(), "2.0");

        // 1
        Assert.assertEquals(databases.get(3).getEngine(), Database.DatabaseEngine.H2);
        Assert.assertEquals(databases.get(3).getVersion(), "1.0");
    }

    @Test(description = "Tests ordering ascending and descending by multiple columns.",
          dependsOnMethods = {"persistTest", "findByField", "deleteTest", "updateUniqueReferenceTest"})
    public void orderByTest() {
        Map<String, Object> params = Collections.emptyMap();
        LinkedListMultimap<SortOrder, String> orderFieldMap = LinkedListMultimap.create();
        orderFieldMap.put(SortOrder.ASCENDING, Database.ENGINE_COLUMN);
        orderFieldMap.put(SortOrder.DESCENDING, Database.VERSION_COLUMN);

        List<Database> databases = databaseRepository.orderByFields(params, orderFieldMap);
        Assert.assertEquals(databases.size(), 4);

        // Assert order
        // 2
        Assert.assertEquals(databases.get(0).getEngine(), Database.DatabaseEngine.H2);
        Assert.assertEquals(databases.get(0).getVersion(), "2.0");

        // 1
        Assert.assertEquals(databases.get(1).getEngine(), Database.DatabaseEngine.H2);
        Assert.assertEquals(databases.get(1).getVersion(), "1.0");

        // 4
        Assert.assertEquals(databases.get(2).getEngine(), Database.DatabaseEngine.MYSQL);
        Assert.assertEquals(databases.get(2).getVersion(), "5.7");

        // 3
        Assert.assertEquals(databases.get(3).getEngine(), Database.DatabaseEngine.MYSQL);
        Assert.assertEquals(databases.get(3).getVersion(), "5.5");
    }
}
