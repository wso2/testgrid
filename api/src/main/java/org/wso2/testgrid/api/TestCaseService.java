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

import org.wso2.testgrid.bean.TestCase;

import java.util.ArrayList;
import java.util.Date;
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

    @GET
    public Response getAllTestCases(@QueryParam("test-scenario-id") String testScenarioId) {
        List<TestCase> testCases = new ArrayList<>();
        TestCase testCase;
        for (int i = 0; i < 5; i++) {
            testCase = new TestCase();
            testCase.setId("" + i);
            testCase.setName("Testcase-" + i);
            testCase.setStatus("COMPLETED");
            testCase.setModifiedTimestamp(new Date().getTime());
            testCase.setStartTimestamp(new Date().getTime());
            testCases.add(testCase);
        }
        return Response.status(Response.Status.OK).entity(testCases).build();
    }

    @GET
    @Path("/{id}")
    public Response getTestScenario(@PathParam("id") String id) {
        TestCase testCase = new TestCase();
        testCase.setId("1");
        testCase.setName("Testcase-" + id);
        testCase.setStatus("COMPLETED");
        testCase.setModifiedTimestamp(new Date().getTime());
        testCase.setStartTimestamp(new Date().getTime());
        return Response.status(Response.Status.OK).entity(testCase).build();
    }
}
