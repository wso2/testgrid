/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.testgrid.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.wso2.testgrid.api.bean.ProductTestPlan;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 */
@Path("/producttestplan")
@Produces("application/json")
public class ProductTestPlanService {
    private EntityManager em;
    private List<Object[]> listEvents;

/* Methods declarations here */

    @GET
    @Path("/all")
    public String getList() throws SQLException, NamingException {
        String records = getAllProductTestPlans();
        return records;
    }

    @GET
    @Path("/echo")
    public String getEcho() throws NamingException {
        return "echo";
    }

    @GET
    @Path("/{productTestPlanid}")
    public String getTestPlans(@PathParam("productTestPlanid") String id) throws NamingException {
        String records = getTestPlanById(id);
        return records;
    }

    @GET
    @Path("/{productTestPlanid}/{testPlanid}")
    public String getScenarios(@PathParam("testPlanid") String id) throws NamingException {
        String records = getScenarioById(id);
        return records;
    }

    private String getAllProductTestPlans() throws NamingException {
        em = getEntityManager();
        em.getTransaction().begin();
        listEvents = em.createNativeQuery("SELECT * FROM product_test_plan;").getResultList();
        em.getTransaction().commit();
        em.close();

        ObjectMapper mapper = new ObjectMapper();
        List<ProductTestPlan> productTestPlanServices = new ArrayList<>();
        ProductTestPlan productTestPlanService;
        for (int i =0; i<5; i++) {
            productTestPlanService = new ProductTestPlan();
            productTestPlanService.setId("1" + i);
            productTestPlanService.setStartTimestamp("1511503091000");
            productTestPlanService.setEndTimestamp("1511503091000");
            productTestPlanService.setStatus("TESTPLAN_PENDING");
            productTestPlanService.setProduct_name("wso2is");
            productTestPlanService.setProduct_version("5.4.0-alpha");
            productTestPlanServices.add(productTestPlanService);
        }
        try {
            return mapper.writeValueAsString(productTestPlanServices);
        } catch (JsonProcessingException e) {

        }
        return null;
    }

    private String getTestPlanById(String id) throws NamingException {
        em = getEntityManager();
        em.getTransaction().begin();
        listEvents = em.createNativeQuery("SELECT * FROM test_plan "
                + "where PRODUCTTESTPLAN_id = " + id + ";")
                .getResultList();
        em.getTransaction().commit();
        em.close();

        return null;
    }

    private String getScenarioById(String id) throws NamingException {
        em = getEntityManager();
        em.getTransaction().begin();
        listEvents = em.createNativeQuery("SELECT name, status FROM test_scenario"
                + " where TESTPLAN_id = " + id + ";")
                .getResultList();
        em.getTransaction().commit();
        em.close();
        return new Gson().toJson(listEvents);
    }

    private EntityManager getEntityManager() throws NamingException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("eclipse_link_jpa");
        return emf.createEntityManager();
    }

}
