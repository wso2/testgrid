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
 */

package org.wso2.testgrid.remote.session.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.remote.session.beans.Agent;
import org.wso2.testgrid.remote.session.beans.ErrorResponse;
import org.wso2.testgrid.remote.session.beans.OperationRequest;
import org.wso2.testgrid.remote.session.utils.Constants;
import org.wso2.testgrid.remote.session.utils.SessionManager;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.websocket.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class represents REST service implementation of Agent Management.
 *
 * @since 1.0.0
 */

@Path("/management")
@Produces(MediaType.APPLICATION_JSON)
public class AgentManagement {

    private static final Logger logger = LoggerFactory.getLogger(AgentManagement.class);

    /**
     * Get list of registered agents.
     *
     * @return A list of registered agents.
     */
    @GET
    @Path("/agents")
    public Response listAllRegisteredAgents() {
        SessionManager sessionManager = SessionManager.getInstance();
        return Response.status(Response.Status.OK).entity(sessionManager.getAgents()).build();
    }

    /**
     * Get list of registered agents under a test plan.
     *
     * @return A list of registered agents under a test plan.
     */
    @GET
    @Path("/test-plan/{testPlanId}/agents")
    public Response listAllRegisteredTestPlanAgents(@PathParam("testPlanId") String testPlanId) {
        SessionManager sessionManager = SessionManager.getInstance();
        List<Agent> agents = sessionManager.getAgents().stream()
                .filter(agent -> testPlanId.equals(agent.getTestPlanId())).collect(Collectors.toList());
        return Response.status(Response.Status.OK).entity(agents).build();
    }

    /**
     * Send operation to agent and get response.
     *
     * @param testPlanId       - Test plan id of the target agent.
     * @param instanceName     - Instance Name of the target agent.
     * @param operationRequest - Operation request.
     * @return The operation response.
     */
    @POST
    @Path("/test-plan/{testPlanId}/agent/{instanceName}/operation")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendOperation(@PathParam("testPlanId") String testPlanId,
                                  @PathParam("instanceName") String instanceName, OperationRequest operationRequest) {
        SessionManager sessionManager = SessionManager.getInstance();
        Agent agent = sessionManager.getAgent(testPlanId, instanceName);
        if (agent != null && sessionManager.hasAgentSession(agent.getAgentId())) {
            Session wsSession = sessionManager.getAgentSession(agent.getAgentId());
            try {
                operationRequest.setOperationId(UUID.randomUUID().toString());
                wsSession.getBasicRemote().sendText(operationRequest.toJSON());
                long initTime = Calendar.getInstance().getTimeInMillis();
                while (!sessionManager.hasOperationResponse(operationRequest.getOperationId())) {
                    long currentTime = Calendar.getInstance().getTimeInMillis();
                    if (initTime + Constants.OPERATION_TIMEOUT < currentTime) {
                        String message = "Operation timed out for agent: " + agent.getAgentId();
                        logger.error(message);
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.setCode(Response.Status.REQUEST_TIMEOUT.getStatusCode());
                        errorResponse.setMessage(message);
                        return Response.status(Response.Status.REQUEST_TIMEOUT).entity(errorResponse).build();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }
            } catch (IOException e) {
                String message = "Error occurred while sending operation to agent: " + agent.getAgentId();
                logger.error(message, e);
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                errorResponse.setMessage(message);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
            }
            return Response.status(Response.Status.OK)
                    .entity(sessionManager.retrieveOperationResponse(operationRequest.getOperationId())).build();
        } else {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(Response.Status.NOT_FOUND.getStatusCode());
            errorResponse.setMessage("Agent not found with given ID");
            return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
        }
    }

}
