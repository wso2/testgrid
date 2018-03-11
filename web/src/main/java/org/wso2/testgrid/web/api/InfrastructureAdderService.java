/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.testgrid.web.api;

import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;
import org.wso2.testgrid.web.bean.ErrorResponse;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * todo
 */
@Path("/infrastructure")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InfrastructureAdderService {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureAdderService.class);

    /**
     * This has the implementation of the REST API for fetching all the infrastructure parameters.
     *
     * @return A list of infrastructure parameters.
     */
    @GET
    public Response getInfrastructureParameters() {
        try {
            InfrastructureParameterUOW infrastructureParameterUOW = new InfrastructureParameterUOW();
            List<InfrastructureParameter> infrastructureParameters = infrastructureParameterUOW
                    .getInfrastructureParameters();

            return Response.status(Response.Status.CREATED).entity(infrastructureParameters).build();
        } catch (TestGridDAOException e) {
            String msg =
                    "Error occurred while loading infrastructure parameters.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().
                            setMessage(msg).
                            setCode(HttpStatus.SC_SERVER_ERROR).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for adding a new infrastructure combination.
     *
     * @param infrastructureParameter The {@link InfrastructureParameter} with name, type, properties, and testgrid
     *                                readiness.
     * @return the persisted InfrastructureParameter that includes additional details such as the id.
     */
    @POST
    public Response addInfrastructureParameter(InfrastructureParameter infrastructureParameter) {
        try {
            InfrastructureParameterUOW infrastructureParameterUOW = new InfrastructureParameterUOW();
            infrastructureParameter = infrastructureParameterUOW
                    .persistInfrastructureParameter(infrastructureParameter);
            logger.info("Added new infrastructure Parameter: " + infrastructureParameter);

            return Response.status(Response.Status.CREATED).entity(infrastructureParameter).build();
        } catch (TestGridDAOException e) {
            String msg =
                    "Error occurred while adding new infrastructure parameter : '" + infrastructureParameter + "'.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().
                            setMessage(msg).
                            setCode(HttpStatus.SC_SERVER_ERROR).build()).build();
        }
    }

}
