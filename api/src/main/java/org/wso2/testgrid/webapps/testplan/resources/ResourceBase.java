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
package org.wso2.testgrid.webapps.testplan.resources;

import java.sql.SQLException;
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
public class ResourceBase {
    private EntityManager em;
    private List<String> listEvents;

/* Methods declarations here */

    @GET
    public List<String> getList() throws SQLException, NamingException {
        List records = getAllProductTestPlans();
        return records;
    }

    @GET
    @Path("/{productTestPlanid}")
    public List<String> getTestPlans(@PathParam("productTestPlanid") String id) throws NamingException {
        List records = getTestPlanById(id);
        return records;
    }

    @GET
    @Path("/{productTestPlanid}/{testPlanid}")
    public List<String> getScenarios(@PathParam("testPlanid") String id) throws NamingException {
        List records = getScenarioById(id);
        return records;
    }

    private List<String> getAllProductTestPlans() throws NamingException {
        em = getEntityManager();
        em.getTransaction().begin();
        listEvents = em.createQuery("SELECT * FROM product_test_plan").getResultList();
        em.getTransaction().commit();
        em.close();
        return listEvents;
    }

    private List<String> getTestPlanById(String id) throws NamingException {
        em = getEntityManager();
        em.getTransaction().begin();
        listEvents = em.createQuery("SELECT * FROM test_plan "
                + "where PRODUCTTESTPLAN_id = " + id)
                .getResultList();
        em.getTransaction().commit();
        em.close();
        return listEvents;
    }

    private List<String> getScenarioById(String id) throws NamingException {
        em = getEntityManager();
        em.getTransaction().begin();
        listEvents = em.createQuery("SELECT * FROM test_scenario where TESTPLAN_id = " + id)
                .getResultList();
        em.getTransaction().commit();
        em.close();
        return listEvents;
    }

    private EntityManager getEntityManager() throws NamingException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("eclipse_link_jpa");
        return emf.createEntityManager();
    }


}
