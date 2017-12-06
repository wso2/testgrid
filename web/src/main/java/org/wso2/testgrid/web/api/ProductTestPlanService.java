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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductTestPlanUOW;
import org.wso2.testgrid.web.bean.ErrorResponse;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST service implementation of ProductTestPlan.
 */

@Path("/product-test-plans")
@Produces(MediaType.APPLICATION_JSON)
public class ProductTestPlanService {
    private static final Log log = LogFactory.getLog(ProductTestPlanService.class);

    /**
     * This has the implementation of the REST API for fetching all the ProductTestPlans.
     *
     * @return A list of available ProductTestPlans.
     */
    @GET
    public Response getAllProductTestPlans() {
        try (ProductTestPlanUOW productTestPlanUOW = new ProductTestPlanUOW()) {
            List<ProductTestPlan> testPlans = productTestPlanUOW.getAllProductTestPlans();
            return Response.status(Response.Status.OK).entity(APIUtil.getProductTestPlanBeans(testPlans)).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the ProductTestPlans.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching a ProductTestPlan by id.
     *
     * @return the matching ProductTestPlan.
     */
    @GET
    @Path("/{id}")
    public Response getProductTestPlan(@PathParam("id") String id) {
        try (ProductTestPlanUOW productTestPlanUOW = new ProductTestPlanUOW()) {
            ProductTestPlan productTestPlan = productTestPlanUOW.getProductTestPlanById(id);

            if (productTestPlan != null) {
                return Response.status(Response.Status.OK).entity(APIUtil.getProductTestPlanBean(productTestPlan)).
                        build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage("Unable to find the requested ProductTestPlan by id : '" + id + "'").build()).
                        build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the ProductTestPlan by id : '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}
