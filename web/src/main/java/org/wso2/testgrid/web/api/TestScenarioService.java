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

package org.wso2.testgrid.web.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;
import org.wso2.testgrid.web.bean.ErrorResponse;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST service implementation of ProductTestPlan.
 */

@Path("/test-scenarios")
@Produces(MediaType.APPLICATION_JSON)
public class TestScenarioService {
    private static final Log log = LogFactory.getLog(TestScenarioService.class);

    /**
     * This has the implementation of the REST API for fetching all the TestScenarioss available in a TestPlan.
     *
     * @return A list of available TestScenarios.
     */
    @GET
    public Response getTestScenariosForTestPlan(@QueryParam("test-plan-id") String testPlanId) {
        try {
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            TestPlan testPlan = testPlanUOW.getTestPlanById(testPlanId);
            List<org.wso2.testgrid.common.TestScenario> testScenarios = testPlan.getTestScenarios();
            return Response.status(Response.Status.OK).entity(APIUtil.getTestScenarioBeans(testScenarios)).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestPlans.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching a specific TestScenario.
     *
     * @return the matching TestScenario.
     */
    @GET
    @Path("/{id}")
    public Response getTestScenario(@PathParam("id") String id) {
        try {
            TestScenarioUOW testScenarioUOW = new TestScenarioUOW();
            TestScenario testScenario = testScenarioUOW.getTestScenarioById(id);

            if (testScenario != null) {
                return Response.status(Response.Status.OK).entity(APIUtil.getTestScenarioBean(testScenario)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage("Unable to find the requested TestScenario by id : '" + id + "'").build()).build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestScenario by id : '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}
