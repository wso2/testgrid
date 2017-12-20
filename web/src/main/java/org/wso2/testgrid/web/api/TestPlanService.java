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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductTestPlanUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
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
 * REST service implementation of TestPlan
 */
@Path("/test-plans")
@Produces(MediaType.APPLICATION_JSON)
public class TestPlanService {

    private static final Logger logger = LoggerFactory.getLogger(TestPlanService.class);

    /**
     * This has the implementation of the REST API for fetching all the TestPlans for a given deployment-pattern
     * and date.
     *
     * @return A list of available TestPlans.
     */
    @GET
    public Response getTestPlansForDeploymentPatternAndDate(@QueryParam("deployment-pattern-id")
                                             String deploymentPatternId, @QueryParam("date") long date,
                                              @QueryParam("requireTestScenarioInfo") boolean requireTestScenarioInfo) {
        try {
            ProductTestPlanUOW productTestPlanUOW = new ProductTestPlanUOW();
            org.wso2.testgrid.common.ProductTestPlan productTestPlan = productTestPlanUOW
                    .getProductTestPlanById(deploymentPatternId);
            List<org.wso2.testgrid.common.TestPlan> testPlans = productTestPlan.getTestPlans();
            return Response.status(Response.Status.OK).entity(APIUtil.getTestPlanBeans(testPlans)).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestPlans.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching a specific TestPlan by id.
     *
     * @return The requested Test-Plan.
     */
    @GET
    @Path("/{id}")
    public Response getTestPlan(@PathParam("id") String id, @QueryParam("requireTestCaseInfo")
            boolean requireTestCaseInfo) {
        try {
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            TestPlan testPlan = testPlanUOW.getTestPlanById(id);
            if (testPlan != null) {
                return Response.status(Response.Status.OK).entity(APIUtil.getTestPlanBean(testPlan)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage("Unable to find the requested TestPlan by id : '" + id + "'").build()).build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestPlan by id : '" + id + "'";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}
