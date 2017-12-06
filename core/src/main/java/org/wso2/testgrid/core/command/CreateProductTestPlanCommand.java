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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.Option;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

/**
 * This creates a product test plan from the input arguments and persist the information in a database.
 *
 * @since 1.0.0
 */
public class CreateProductTestPlanCommand implements Command {

    private static final Log log = LogFactory.getLog(CreateProductTestPlanCommand.class);

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
            aliases = {"-c"})
    private String channel = "LTS";

    @Override
    public void execute() throws CommandExecutionException {
        log.info("Creating product test plan...");
        log.info(
                "Input Arguments: \n" +
                "\tProduct name: " + productName + "\n" +
                "\tProduct version: " + productVersion + "\n" +
                "\tChannel" + channel);
        /*
            psuedo code:
            query db: is product test plan for the given product/version/channel exist
            if true:
                log Product information already stored in the db
            if false:
                persist p/v/c into the db
         */

        ProductTestPlan productTestPlan = createProductTestPlan(productName, productVersion, channel);
        TestPlanUOW testPlanUOW = new TestPlanUOW();
        try {
            testPlanUOW.persistProductTestPlan(productTestPlan);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while persisting ProductTestPlan", e);
        }
    }

    /**
     * Creates an instance of {@link ProductTestPlan} for the given parameters.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product test plan channel
     * @return {@link ProductTestPlan} instance for the given parameters
     * @throws CommandExecutionException thrown when error on creating an instance of {@link ProductTestPlan}
     */
    private ProductTestPlan createProductTestPlan(String productName, String productVersion, String channel)
            throws CommandExecutionException {
        try {
            ProductTestPlan.Channel productTestPlanChannel = ProductTestPlan.Channel.valueOf(channel);
            ProductTestPlan productTestPlan = new ProductTestPlan();

            productTestPlan.setProductName(productName);
            productTestPlan.setProductVersion(productVersion);
            productTestPlan.setStatus(ProductTestPlan.Status.PRODUCT_TEST_PLAN_PENDING);
            productTestPlan.setChannel(productTestPlanChannel);
            return productTestPlan;
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Channel ",
                    channel, " is not defined in the available channels enum"), e);
        }
    }
}
