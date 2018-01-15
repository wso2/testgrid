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
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.web.bean.ErrorResponse;
import org.wso2.testgrid.web.bean.TestPlanRequest;
import org.wso2.testgrid.web.operation.ConfigXmlGenerator;
import org.wso2.testgrid.web.operation.PipelineManager;

import java.net.HttpURLConnection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
     * and created before date.
     *
     * @return A list of available TestPlans.
     */
    @GET
    public Response getTestPlansForDeploymentPatternAndDate(@QueryParam("deployment-pattern-id")
                                           String deploymentPatternId, @QueryParam("date") String date,
                                           @QueryParam("require-test-scenario-info") boolean requireTestScenarioInfo) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date parsedDate = null;
            try {
                parsedDate = dateFormat.parse(date);
            } catch (ParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date format.").build()).build();
            }
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            List<org.wso2.testgrid.common.TestPlan> testPlans = testPlanUOW.
                    getTestPlansByDeploymentIdAndDate(deploymentPatternId, new Timestamp(parsedDate.getTime()));
            return Response.status(Response.Status.OK).entity(APIUtil.getTestPlanBeans(testPlans,
                    requireTestScenarioInfo)).build();
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
    public Response getTestPlan(@PathParam("id") String id, @QueryParam("require-test-scenario-info")
            boolean requireTestScenarioInfo) {
        try {
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            Optional<TestPlan> testPlan = testPlanUOW.getTestPlanById(id);
            if (testPlan.isPresent()) {
                return Response.status(Response.Status.OK).entity(APIUtil.getTestPlanBean(testPlan.get(),
                        requireTestScenarioInfo)).build();
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

    /**
     * This has the implementation of the REST API for creating a new Test plan.
     * @param testPlanRequest  {@link TestPlanRequest} that includes the repository and other necessary detials
     *                                                to create new test plan.
     * @return A list of available TestPlans.
     */
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTestPlan(TestPlanRequest testPlanRequest) {
        ConfigXmlGenerator configXmlGenerator = new ConfigXmlGenerator();
        PipelineManager pipelineManager = new PipelineManager();
        try {
            String configXml = configXmlGenerator.getConfigXml(testPlanRequest);
            return Response.status(Response.Status.CREATED).
                    entity(pipelineManager.createNewPipelineJob(configXml, testPlanRequest.getProductName())).build();
        } catch (Exception e) {
            String msg = "Error occurred while creating new test plan named : '" +
                    testPlanRequest.getProductName() + "'";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().
                            setMessage(msg + " " + e).
                            setDescription(e.toString()).
                            setCode(HttpURLConnection.HTTP_INTERNAL_ERROR).build()).build();
        }
    }
}
