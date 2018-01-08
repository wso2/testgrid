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

/**
 * This generates a cumulative test report that consists of all test plans for a given product, version and channel.
 *
 * @since 1.0.0
 */
public class GenerateReportCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(GenerateReportCommand.class);

    @Option(name = "--product",
            usage = "Product Name",
            aliases = {"-p"},
            required = true)
    private String productName = "";

    @Option(name = "--version",
            usage = "product version",
            aliases = {"-v"},
            required = true)
    private String productVersion = "";

    @Option(name = "--channel",
            usage = "product channel",
            aliases = {"-c"}
    )
    private String channel = "LTS";

    @Option(name = "--showSuccess",
            usage = "Show success tests",
            aliases = {"--showSuccess"})
    private boolean showSuccess = false;

    @Option(name = "--groupBy",
            usage = "Group by the given column",
            aliases = {"--groupBy"})
    private String groupBy = "SCENARIO";


    @Override
    public void execute() throws CommandExecutionException {
        try {
            logger.info("Generating test result report...");
            logger.info(
                    "Input Arguments: \n" +
                    "\tProduct name: " + productName + "\n" +
                    "\tProduct version: " + productVersion + "\n" +
                    "\tChannel" + channel);

            Product product = getProduct(productName, productVersion, channel);
            TestReportEngine testReportEngine = new TestReportEngine();
            testReportEngine.generateReport(product, showSuccess, groupBy);
        } catch (ReportingException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error occurred when generating test report for { product: ", productName,
                            ", version: ", productVersion, ", channel: ", channel, " }"), e);
        }
    }

    /**
     * Returns an instance of {@link Product} for the given product name and product version.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product test plan channel
     * @return an instance of {@link Product} for the given product name and product version
     * @throws CommandExecutionException throw when error on obtaining product for the given product name and product
     *                                   version
     */
    private Product getProduct(String productName, String productVersion, String channel)
            throws CommandExecutionException {
        try {
            ProductUOW productUOW = new ProductUOW();
            Product.Channel productChannel = Product.Channel.valueOf(channel);
            return productUOW.getProduct(productName, productVersion, productChannel)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("No product test plan found for product {product name: ", productName,
                                    ", product version: ", productVersion, ", channel: ", channel,
                                    "}. This exception should not occur in the test execution flow.")));
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Channel ", channel,
                    " not found in channels enum - [ ", Product.Channel.values(), " ]",
                    "This exception should not occur in the test execution flow."));
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in retrieving Product {product name: ", productName,
                            ", product version: ", productVersion, ", channel: ", channel,
                            "} from the Database. This exception should not occur in the test execution flow."), e);
        }
    }
}
