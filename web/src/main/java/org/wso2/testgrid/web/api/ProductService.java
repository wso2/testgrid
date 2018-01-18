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
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.web.bean.ErrorResponse;
import org.wso2.testgrid.web.bean.ProductStatus;

import java.util.ArrayList;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST service implementation of Products.
 */

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String DELIMITER = "_";

    /**
     * This has the implementation of the REST API for fetching all the Products.
     *
     * @return A list of available Products.
     */
    @GET
    public Response getAllProducts() {
        try {
            ProductUOW productUOW = new ProductUOW();
            return Response.status(Response.Status.OK).entity(APIUtil.getProductBeans(productUOW.getProducts())).
                    build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the Products.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching a Product by name, version and channel.
     *
     * @return the matching Product for the given name, version and channel.
     */
    @GET
    @Path("/search")
    public Response getProduct(@QueryParam("name") String name, @QueryParam("version") String version,
                               @QueryParam("channel") String channel) {
        try {
            ProductUOW productUOW = new ProductUOW();
            Optional<Product> product = productUOW.getProduct(name, version, Product.Channel.valueOf(channel));
            if (product.isPresent()) {
                return Response.status(Response.Status.OK).entity(APIUtil.getProductBean(product.get())).
                        build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage("Unable to find the requested Product by name: '" + name + "', " +
                                "version: '" + version + "', channel: '" + channel + "'").build()).build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the Product by name: '" + name + "', " + "version: '" +
                    version + "', channel: '" + channel + "'";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching a Product by id.
     *
     * @return the matching Product.
     */
    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") String id) {
        try {
            ProductUOW productUOW = new ProductUOW();
            Optional<Product> product = productUOW.getProduct(id);
            if (product.isPresent()) {
                return Response.status(Response.Status.OK).entity(APIUtil.getProductBean(product.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage("Unable to find the requested Product by id : '" + id + "'").build()).
                        build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the Product by id : '" + id + "'";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This method returns the list of products that are currently in TestGrid.
     * <p>
     * <p> The products are returned as a json response with the last build information and
     * the last failed build information<p/>
     *
     * @return list of products
     */
    @GET
    @Path("/product-status")
    public Response getAllProductStatuses() {
        TestPlanUOW testPlanUOW = new TestPlanUOW();
        ProductUOW productUOW = new ProductUOW();
        ArrayList<ProductStatus> list = new ArrayList<>();
        try {
            for (Product product : productUOW.getProducts()) {
                ProductStatus status = new ProductStatus();
                status.setId(product.getId());
                status.setName(StringUtil.concatStrings(product.getName(), product.getVersion(), product.getChannel()));
                status.setLastfailed(APIUtil.getTestPlanBean(testPlanUOW.getLastFailure(product), false));
                status.setLastBuild(APIUtil.getTestPlanBean(testPlanUOW.getLastBuild(product), false));
                status.setStatus(testPlanUOW.getCurrentStatus(product).toString());
                list.add(status);
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the Product statuses ";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(list).build();
    }
}
