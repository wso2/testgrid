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

import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.exception.TestGridRuntimeException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.reporting.AxisColumn;
import org.wso2.testgrid.web.bean.ErrorResponse;
import org.wso2.testgrid.web.bean.ProductStatus;
import org.wso2.testgrid.web.plugins.AWSArtifactReader;
import org.wso2.testgrid.web.plugins.ArtifactReadable;
import org.wso2.testgrid.web.plugins.ArtifactReaderException;
import org.wso2.testgrid.web.utils.Constants;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
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
                status.setProductId(product.getId());
                status.setProductName(product.getName());
                status.setLastfailed(APIUtil.getTestPlanBean(testPlanUOW.getLastFailure(product), false));
                status.setLastBuild(APIUtil.getTestPlanBean(testPlanUOW.getLastBuild(product), false));
                status.setProductStatus(testPlanUOW.getCurrentStatus(product).toString());
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

    /**
     * This method returns the product if the requested product name exists in the TestGrid.
     *
     * <p> The product is returned as a json response with the last build information and
     * the last failed build information<p/>
     *
     * @return product
     */
    @GET
    @Path("/product-status/{productName}")
    public Response getProductStatus(
            @PathParam("productName") String productName) {
        try {
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            ProductUOW productUOW = new ProductUOW();
            ProductStatus productStatus = new ProductStatus();
            Optional<Product> productInstance = productUOW.getProduct(productName);
            Product product;
            if (productInstance.isPresent()) {
                product = productInstance.get();
                productStatus.setProductId(product.getId());
                productStatus.setProductName(product.getName());
                productStatus.setLastfailed(APIUtil.getTestPlanBean(testPlanUOW.getLastFailure(product), false));
                productStatus.setLastBuild(APIUtil.getTestPlanBean(testPlanUOW.getLastBuild(product), false));
                productStatus.setProductStatus(testPlanUOW.getCurrentStatus(product).toString());
            } else {
                String msg = "Could not found the product:" + productName + " in TestGrid. Please check the "
                        + "infrastructure_parameter table";
                logger.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(productStatus).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the statuses of the product: " + productName + ". Please "
                    + "check the database configurations";
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * This method verifies the existence of the requesting report in the remote location.
     * <p>
     *
     * @return existence of the querying product report
     */
    @HEAD
    @Path("/reports")
    public Response isProductReportExist(
            @QueryParam("product-name") String productName,
            @DefaultValue("false") @QueryParam("show-success") Boolean showSuccess,
            @DefaultValue("SCENARIO") @QueryParam("group-by") String groupBy) {
        try {
            ProductUOW productUOW = new ProductUOW();
            Optional<Product> productInstance = productUOW.getProduct(productName);
            Product product;
            if (productInstance.isPresent()) {
                product = productInstance.get();
                AxisColumn uniqueAxisColumn = AxisColumn.valueOf(groupBy.toUpperCase(Locale.ENGLISH));
                String fileName = StringUtil
                        .concatStrings(product.getName(), "-", uniqueAxisColumn, Constants.HTML_EXTENSION);
                String bucketKey = Paths.get(Constants.AWS_BUCKET_ARTIFACT_DIR, productName, fileName).toString();
                ArtifactReadable artifactReadable = new AWSArtifactReader(ConfigurationContext.
                        getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME),
                        Constants.AWS_BUCKET_NAME);
                if (artifactReadable.isExistArtifact(bucketKey)) {
                    return Response.status(Response.Status.OK).entity("The artifact exists in the remote storage")
                            .build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Couldn't found the Artifact in the remote location").build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Could't found the Product in Test Grid").build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the product for product name : '" + productName + "' ";
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ArtifactReaderException e) {
            String msg = "Error occurred while creating AWS artifact reader.";
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (IOException e) {
            String msg = "Error occurred while accessing configurations.";
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * This method is able to get the latest report of a given product from the remote storage and return.
     * <p>
     * <p> The report is returned as a html file<p/>
     *
     * @return latest report of querying product
     */
    @GET
    @Path("/reports")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getProductReport(
            @QueryParam("product-name") String productName,
            @DefaultValue("false") @QueryParam("show-success") Boolean showSuccess,
            @DefaultValue("SCENARIO") @QueryParam("group-by") String groupBy) {
        try {
            ProductUOW productUOW = new ProductUOW();
            Optional<Product> productInstance = productUOW.getProduct(productName);
            Product product;
            if (productInstance.isPresent()) {
                product = productInstance.get();
                AxisColumn uniqueAxisColumn = AxisColumn.valueOf(groupBy.toUpperCase(Locale.ENGLISH));
                String fileName = StringUtil
                        .concatStrings(product.getName(), "-", uniqueAxisColumn, Constants.HTML_EXTENSION);
                String bucketKey = Paths.get(Constants.AWS_BUCKET_ARTIFACT_DIR, productName, fileName).toString();
                ArtifactReadable artifactReadable = new AWSArtifactReader(ConfigurationContext.
                        getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME),
                        Constants.AWS_BUCKET_NAME);
                Response.ResponseBuilder response = Response
                        .ok(artifactReadable.getArtifactStream(bucketKey), MediaType.APPLICATION_OCTET_STREAM);
                response.status(Response.Status.OK);
                response.type("application/html");
                response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                return response.build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();

        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the product for product name : '" + productName + "' ";
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ArtifactReaderException e) {
            String msg = "Error occurred while creating AWS artifact reader.";
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (IOException e) {
            String msg = "Error occurred while accessing configurations.";
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ResourceNotFoundException e) {
            String msg = "Error occurred while getting the report.";
            logger.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (TestGridRuntimeException e) {
            String msg = "Error occurred while accessing the remote storage";
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
