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
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestCaseUOW;
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
 * REST service implementation of Test-Cases.
 */

@Path("/test-cases")
@Produces(MediaType.APPLICATION_JSON)
public class TestCaseService {

    private static final Logger logger = LoggerFactory.getLogger(TestCaseService.class);

    /**
     * This has the implementation of the REST API for fetching all the TestCases available in a TestScenario.
     *
     * @return A list of available TestCases in the given TestScenario.
     */
    @GET
    public Response getTestCasesForTestScenario(@QueryParam("test-scenario-id") String testScenarioId) {
        try {
            TestScenarioUOW testScenarioUOW = new TestScenarioUOW();
            TestScenario testScenario = testScenarioUOW.getTestScenarioById(testScenarioId);
            List<org.wso2.testgrid.common.TestCase> testCases = testScenario.getTestCases();
            return Response.status(Response.Status.OK).entity(APIUtil.getTestCaseBeans(testCases)).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestCases.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching a specific TestCase by id.
     *
     * @return the matching TestCase.
     */
    @GET
    @Path("/{id}")
    public Response getTestCase(@PathParam("id") String id) {
        try {
            TestCaseUOW testCaseUOW = new TestCaseUOW();
            org.wso2.testgrid.common.TestCase testCase = testCaseUOW.getTestCaseById(id);

            if (testCase != null) {
                return Response.status(Response.Status.OK).entity(APIUtil.getTestCaseBean(testCase)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage("Unable to find the requested TestCase by id : '" + id + "'").build()).build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestCase by id : '" + id + "'";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}
