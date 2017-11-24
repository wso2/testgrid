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
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.DatabaseRepository;
import org.wso2.testgrid.dao.repository.ProductTestPlanRepository;
import org.wso2.testgrid.dao.repository.TestPlanRepository;
import org.wso2.testgrid.dao.util.DAOUtil;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Native SQL query to get the latest modifies product test plan for product name and version
        String sqlQuery = StringUtil.concatStrings("SELECT c FROM ", ProductTestPlan.class.getSimpleName(),
                " c WHERE c.productName=\"", productName, "\" AND ",
                "c.productVersion=\"", productVersion, "\" ORDER BY c.modifiedTimestamp");

        return productTestPlanRepository.executeTypedQuary(sqlQuery,ProductTestPlan.class,1);
    }


    public void persistProductTestPlan(ProductTestPlan productTestPlan) throws TestGridDAOException {
        ProductTestPlanRepository repo = new ProductTestPlanRepository(entityManagerFactory);
        repo.persist(productTestPlan);
    }

    public void persistSingleTestPlan(TestPlan testPlan) throws TestGridDAOException {
        TestPlanRepository testPlanRepository = new TestPlanRepository(entityManagerFactory);
        testPlanRepository.persist(testPlan);

    }

    public Database getDatabse(String dbEngine,String dbVersion) throws TestGridDAOException{
        DatabaseRepository repository = new DatabaseRepository(entityManagerFactory);
        Map<String,Object> map = new HashMap<>();
        map.put("engine",dbEngine);
        map.put("version",dbVersion);
        List<Database> byFields = repository.findByFields(map);
        if(byFields.size()==1){
            return byFields.get(0);
        }else {
            return null;
        }


    }
}
