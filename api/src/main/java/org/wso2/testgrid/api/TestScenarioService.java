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

package org.wso2.testgrid.api;

import org.wso2.testgrid.bean.TestScenario;

import java.util.ArrayList;
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

    @GET
    public Response getAllTestScenarios(@QueryParam("test-plan-id") String testPlanId) {
        List<TestScenario> testScenarios = new ArrayList<>();
        TestScenario testScenario;
        for (int i =0; i<5; i++) {
            testScenario = new TestScenario();
            testScenario.setName("TestScenario-" + i);
            testScenario.setStatus("COMPLETED");
            testScenario.setId("" + i);
            testScenarios.add(testScenario);
        }
        return Response.status(Response.Status.OK).entity(testScenarios).build();
    }

    @GET
    @Path("/{id}")
    public Response getTestScenario(@PathParam("id") String id) {
        TestScenario testScenario = new TestScenario();
        testScenario.setName("TestScenario-1");
        testScenario.setStatus("COMPLETED");
        testScenario.setId("1");
        return Response.status(Response.Status.OK).entity(testScenario).build();
    }
}
