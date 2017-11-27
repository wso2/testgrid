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
package org.wso2.testgrid.webapps.testplan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to display the status of the TestPlan.
 */
public class TestPlanServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(TestPlanServlet.class);
    private static final long serialVersionUID = 1L;
 
    @Override
    protected void doGet(
        HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("eclipse_link_jpa");
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            // Display the Testplan status:
            List<String> testPlanStatus = em.createNativeQuery("SELECT status FROM test_plan "
                    + "where modified_timestamp = (select MAX(modified_timestamp)from test_plan)")
                    .getResultList();
            request.setAttribute("testPlans", testPlanStatus);
            request.getRequestDispatcher("/TestPlan.jsp").forward(request, response);

        } finally {

            // Close the PersistenceManager:
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    @Override
    protected void doPost(
        HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
