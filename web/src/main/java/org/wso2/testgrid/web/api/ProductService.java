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
import org.wso2.testgrid.common.ProductTestStatus;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.web.bean.ErrorResponse;
import org.wso2.testgrid.web.bean.TestStatus;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
     * This has the implementation of the REST API for fetching Product info with the test history.
     *
     * @param date number of dates the history should be given
     * @return Product list with recent test status.
     */
    @GET
    @Path("/test-status")
    public Response getProductsWithRecentTestStatus(@QueryParam("date") String date) {
        try {
            ProductUOW productUOW = new ProductUOW();
            //Get the history of all products
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date parsedDate = null;
            try {
                parsedDate = dateFormat.parse(date);
            } catch (ParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date format.").build()).build();
            }
            List<ProductTestStatus> productTestStatuses = productUOW.
                    getProductTestHistory(new Timestamp(parsedDate.getTime()));

            ProductTestStatus status;
            Map<String, String> productStatuses = new HashMap<>();
            //Iterate through available product information to populate statuses
            Iterator<ProductTestStatus> itr = productTestStatuses.iterator();
            while (itr.hasNext()) {
                status = itr.next();
                productStatuses.put(this.generateKey(status), status.getStatus());
            }

            org.wso2.testgrid.web.bean.ProductTestStatus testStatusBean;
            TestStatus testStatus;
            Map<String, org.wso2.testgrid.web.bean.ProductTestStatus> products = new HashMap<>();
            String key, productId, strDate;
            for (Map.Entry<String, String> entry : productStatuses.entrySet()) {
                key = entry.getKey();

                testStatusBean = new org.wso2.testgrid.web.bean.ProductTestStatus(key);
                //Extract the product-id
                productId = key.substring(0, key.indexOf(DELIMITER));
                //Extract the test-date
                strDate = key.substring(key.lastIndexOf(DELIMITER) + 1);

                if (!products.containsKey(productId)) {
                    products.put(productId, testStatusBean);
                }

                testStatus = new TestStatus();
                testStatus.setDate(strDate);
                testStatus.setStatus(entry.getValue());

                org.wso2.testgrid.web.bean.ProductTestStatus productTestStatus = products.get(productId);
                productTestStatus.getTestStatuses().add(testStatus);
                products.put(productId, productTestStatus);
            }

            return Response.status(Response.Status.OK).entity(products.values()).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the Products with test info.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    private String generateKey (ProductTestStatus status) {
        Date date = new Date(status.getTestExecutionTime().getTime());
        return new StringBuilder().append(status.getId() + DELIMITER).append(status.getName() + DELIMITER).
                append(status.getVersion() + DELIMITER).append(status.getChannel() + DELIMITER).
                append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date)).toString();
    }
}
