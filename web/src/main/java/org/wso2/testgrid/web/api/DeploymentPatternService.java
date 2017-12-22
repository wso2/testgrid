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
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.DeploymentPatternTestFailureStat;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.web.bean.ErrorResponse;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST service implementation of Deployment-Patterns.
 */

@Path("/deployment-patterns")
@Produces(MediaType.APPLICATION_JSON)
public class DeploymentPatternService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentPatternService.class);

    /**
     * This has the implementation of the REST API for fetching all the Deployment-Patterns.
     *
     * @return A list of available Deployment-Patterns.
     */
    @GET
    public Response getAllDeploymentPatterns() {
        try {
            DeploymentPatternUOW deploymentPatternUOW = new DeploymentPatternUOW();
            List<DeploymentPattern> deploymentPatterns = deploymentPatternUOW.getDeploymentPatterns();
            return Response.status(Response.Status.OK).entity(APIUtil.getDeploymentPatternBeans(deploymentPatterns,
                    new ArrayList<>())).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching all Deployment-Patterns.";
            logger.error(msg, e);
            return Response.serverError().entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).
                    build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching a Deployment-Pattern by id.
     *
     * @return the matching Deployment-Pattern.
     */
    @GET
    @Path("/{id}")
    public Response getDeploymentPattern(@PathParam("id") String id) {
        try {
            DeploymentPatternUOW deploymentPatternUOW = new DeploymentPatternUOW();
            Optional<DeploymentPattern> deploymentPattern = deploymentPatternUOW.getDeploymentPatternById(id);

            if (deploymentPattern.isPresent()) {
                return Response.status(Response.Status.OK).entity(APIUtil.
                        getDeploymentPatternBean(deploymentPattern.get(), "")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage("Unable to find the requested Deployment-Pattern by id : '" + id + "'").build()).
                        build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the Deployment-Pattern by id : '" + id + "'";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching Products with build history status.
     *
     * @return Product list with build status.
     */
    @GET
    @Path("/recent-test-info")
    public Response getDeploymentPatternsWithTestInfo(@QueryParam("productId") String productId,
                                                      @QueryParam("date") String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date parsedDate = null;
            try {
                parsedDate = dateFormat.parse(date);
            } catch (ParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date format.").build()).build();
            }
            DeploymentPatternUOW deploymentPatternUOW = new DeploymentPatternUOW();
            List<DeploymentPattern> deploymentPatterns = deploymentPatternUOW.
                    getDeploymentPatternsByProductAndDate(productId, new Timestamp(parsedDate.getTime()));


            List<DeploymentPatternTestFailureStat> stats = deploymentPatternUOW.getFailedTestCounts(productId,
                    new Timestamp(parsedDate.getTime()));
            return Response.status(Response.Status.OK).entity(APIUtil.getDeploymentPatternBeans(deploymentPatterns,
                    stats)).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the Deployment-patterns with test info for the product id : '"
                    + productId + "' , and date : '" + date + "'";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}
