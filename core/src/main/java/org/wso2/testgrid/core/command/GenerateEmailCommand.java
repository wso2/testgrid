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

package org.wso2.testgrid.core.command;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.TestReportEngine;

import java.nio.file.Path;
import java.util.Optional;

/**
 * This generates an email report that consists of test results for a given product.
 */
public class GenerateEmailCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(GenerateReportCommand.class);

    @Option(name = "--product",
            usage = "Product Name",
            aliases = {"-p"},
            required = true)
    private String productName = "";

    @Option(name = "--workspace",
            usage = "Product workspace",
            aliases = {"-w"},
            required = true)
    private String workspace = "";

    @Override
    public void execute() throws CommandExecutionException {
        Product product = getProduct(productName);
        try {
            TestReportEngine testReportEngine = new TestReportEngine();
            // Generating the summary report
            final Optional<Path> summarizedReportPath = testReportEngine.generateSummarizedEmailReport(product,
                    workspace);

            Optional<Path> infraEmailPath = testReportEngine.generateInfraFailureReport(product, workspace);
            summarizedReportPath.ifPresent(p -> logger.info("Written the summarized email " +
                                                            "report body contents to: " + p));
            final Optional<Path> reportPath = testReportEngine.generateEmailReport(product, workspace);
            reportPath.ifPresent(p -> logger.info("Written the email report body contents to: " + p));
            infraEmailPath.ifPresent(path -> logger.info("Written the infraError email content to" + path));
        } catch (ReportingException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error occurred when generating email report for {" +
                            " product: ", productName, " }"), e);
        }
    }

    /**
     * Returns an instance of {@link Product} for the given product name and product version.
     *
     * @param productName    product name
     * @return an instance of {@link Product} for the given product name and product version
     * @throws CommandExecutionException throw when error on obtaining product for the given product name and product
     *                                   version
     */
    private Product getProduct(String productName)
            throws CommandExecutionException {
        try {
            ProductUOW productUOW = new ProductUOW();
            return productUOW.getProduct(productName)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("No product test plan found for product {product name: ", productName,
                                    "}. This exception should not occur in the test execution flow.")));
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in retrieving Product {product name: ", productName,
                            "} from the Database. This exception should not occur in the test execution flow."), e);
        }
    }
}
