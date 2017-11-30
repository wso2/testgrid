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

import org.wso2.testgrid.common.Database;
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.InfraResult;
import org.wso2.testgrid.common.OperatingSystem;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.DatabaseRepository;
import org.wso2.testgrid.dao.repository.InfraCombinationRepository;
import org.wso2.testgrid.dao.repository.InfraResultRespository;
import org.wso2.testgrid.dao.repository.OperatingSystemRepository;
import org.wso2.testgrid.dao.repository.ProductTestPlanRepository;
import org.wso2.testgrid.dao.repository.TestPlanRepository;
import org.wso2.testgrid.dao.util.DAOUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * This class defines the Unit of work related to a Test Plan.
 *
 * @since 1.0.0
 */
public class TestPlanUOW {

    private final EntityManagerFactory entityManagerFactory;

    public TestPlanUOW() {
        entityManagerFactory = DAOUtil.getEntityManagerFactory();
    }

    /**
     * Returns an instance of {@link ProductTestPlan} for the given product name and product version.
     *
     * @param productName    product name
     * @param productVersion product version
     * @return an instance of {@link ProductTestPlan} for the given product name and product version
     * @throws TestGridDAOException thrown when error on obtaining the product test plan
     */
    public ProductTestPlan getProductTestPlan(String productName, String productVersion) throws TestGridDAOException {
        ProductTestPlanRepository productTestPlanRepository = new ProductTestPlanRepository(entityManagerFactory);

        // JPQL  query to get the latest modifies product test plan for product name and version
        String sqlQuery = StringUtil.concatStrings("SELECT c FROM ", ProductTestPlan.class.getSimpleName(),
                " c WHERE c.productName=\"", productName, "\" AND ",
                "c.productVersion=\"", productVersion, "\" ORDER BY c.modifiedTimestamp DESC");
        return productTestPlanRepository.executeTypedQuary(sqlQuery, ProductTestPlan.class, 1);
    }

    /**
     * This method persists a {@link ProductTestPlan} to the database.
     *
     * @param productTestPlan Populated ProductTestPlan object.
     * @throws TestGridDAOException When there is an error persisting the object.
     */
    public void persistProductTestPlan(ProductTestPlan productTestPlan) throws TestGridDAOException {
        ProductTestPlanRepository repo = new ProductTestPlanRepository(entityManagerFactory);
        repo.persist(productTestPlan);
    }

    /**
     * This method persists a single {@link TestPlan} object to the database.
     *
     * @param testPlan Populated TestPlan object.
     * @return The persisted TestPlan object with additional details added.
     * @throws TestGridDAOException When there is an error persisting the object.
     */
    public TestPlan persistSingleTestPlan(TestPlan testPlan) throws TestGridDAOException {
        TestPlanRepository testPlanRepository = new TestPlanRepository(entityManagerFactory);
        TestPlan persisted = testPlanRepository.persist(testPlan);
        if (persisted != null) {
            persisted.setDeploymentScript(testPlan.getDeploymentScript());
            persisted.setDeployment(testPlan.getDeployment());
            persisted.setInfraRepoDir(testPlan.getInfraRepoDir());
            persisted.setDeployerType(testPlan.getDeployerType());
            persisted.setHome(testPlan.getHome());
            persisted.setInfrastructureScript(testPlan.getInfrastructureScript());
            persisted.setTestRepoDir(testPlan.getTestRepoDir());
            persisted.setTestScenarios(testPlan.getTestScenarios());
        }
        return persisted;
    }


    /**
     * This method retrieves a {@link Database} object if it exists in the database.
     *
     * @param dbEngine  DBEngine of the database.
     * @param dbVersion Version of the database.
     * @return Database object if it exists.
     * @throws TestGridDAOException When there is an error looking up the object.
     */
    public Database getDatabse(String dbEngine, String dbVersion) throws TestGridDAOException {
        DatabaseRepository repository = new DatabaseRepository(entityManagerFactory);
        Map<String, Object> map = new HashMap<>();
        map.put("engine", dbEngine);
        map.put("version", dbVersion);
        List<Database> byFields = repository.findByFields(map);
        if (byFields.size() == 1) {
            return byFields.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method retrieves a {@link OperatingSystem} object if it exists in the database.
     *
     * @param name    Name of the Operating System.
     * @param version Version of the Operating System.
     * @return OperatingSystem object if it exists.
     * @throws TestGridDAOException when there is an error lokking up the object.
     */
    public OperatingSystem getOperatingSystem(String name, String version) throws TestGridDAOException {
        OperatingSystemRepository repository = new OperatingSystemRepository(entityManagerFactory);
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("version", version);
        List<OperatingSystem> byFields = repository.findByFields(map);
        if (byFields.size() == 1) {
            return byFields.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method retrieves a {@link InfraCombination} object if it exists in the database.
     *
     * @param jdk             The {@link org.wso2.testgrid.common.InfraCombination.JDK} value
     * @param database        Database object of the combination.
     * @param operatingSystem Operating System of the combination.
     * @return InfraCombination if it exists in the databsae.
     * @throws TestGridDAOException When there is an error looking up the InfraCombination.
     */
    public InfraCombination getInfraCombination(String jdk, Database database, OperatingSystem operatingSystem)
            throws TestGridDAOException {
        InfraCombinationRepository repository = new InfraCombinationRepository(entityManagerFactory);
        Map<String, Object> map = new HashMap<>();
        map.put("jdk", jdk);
        map.put("operatingSystem", operatingSystem);
        map.put("database", database);
        List<InfraCombination> byFields = repository.findByFields(map);
        if (byFields.size() == 1) {
            return byFields.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method retrieves {@link InfraResult} object from the database.
     *
     * @param infraResult Populated InfraResult object.
     * @return Persisted InfraResult object if it exists.
     * @throws TestGridDAOException When there is an error looking up the object.
     */
    public InfraResult getInfraResult(InfraResult infraResult) throws TestGridDAOException {
        InfraResultRespository repository = new InfraResultRespository(entityManagerFactory);
        Map<String, Object> map = new HashMap<>();
        map.put("infraCombination", infraResult.getInfraCombination());
        List<InfraResult> byFields = repository.findByFields(map);
        if (byFields.size() == 1) {
            return byFields.get(0);
        } else {
            return null;
        }
    }
}
