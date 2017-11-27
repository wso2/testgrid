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

import org.wso2.testgrid.bean.TestPlan;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * REST service implementation of TestPlan
 */
@Path("/test-plans")
@Produces(MediaType.APPLICATION_JSON)
public class TestPlanService {

    @GET
    public Response getAllTestScenarios(@QueryParam("product-test-plan-id") String testPlanId) {
        List<TestPlan> testPlans = new ArrayList<>();
        TestPlan testPlan;
        for (int i = 0; i < 5; i++) {
            testPlan = new TestPlan();
            testPlan.setId("1" + i);
            testPlan.setName("Test-plan-01");
            testPlan.setStartTimestamp("1511503091000");
            testPlan.setModifiedTimestamp("1511503091000");
            testPlan.setStatus("TESTPLAN_PENDING");
            testPlan.setProductTestPlanId("10");
            testPlan.setInfraResultId("1");
            testPlans.add(testPlan);
        }
        return Response.status(Response.Status.OK).entity(testPlans).build();
    }

    @GET
    @Path("/{id}")
    public Response getTestScenario(@PathParam("id") String id) {
        TestPlan testPlan = new TestPlan();
        testPlan.setId("2");
        testPlan.setName("Test-plan-02");
        testPlan.setStartTimestamp("1511503091000");
        testPlan.setModifiedTimestamp("1511503091000");
        testPlan.setStatus("TESTPLAN_PENDING");
        testPlan.setProductTestPlanId("10");
        testPlan.setInfraResultId("1");
        return Response.status(Response.Status.OK).entity(testPlan).build();
    }
}
