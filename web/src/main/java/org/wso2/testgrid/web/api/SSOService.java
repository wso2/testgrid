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
package org.wso2.testgrid.web.api;

import org.wso2.testgrid.web.utils.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Service implementation for SSO related functions.
 */
@Path("/acs")
@Produces(MediaType.APPLICATION_JSON)
public class SSOService {

    @Context
    private HttpServletRequest request;

    /**
     * This has the implementation of the REST API for creating session and send the redirection information .
     * @return Redirection to dashboard home page.
     */
    @POST
    public Response createSession() {
        request.getSession();
        return Response.status(Response.Status.FOUND).header(HttpHeaders.LOCATION, Constants.WEBAPP_CONTEXT).
                type(MediaType.TEXT_PLAIN).build();
    }
}
