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
package org.wso2.testgrid.core.command;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;

/**
 * This creates a product from the input arguments and persist the information in a database.
 *
 * @since 1.0.0
 */
public class CreateProductCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CreateProductCommand.class);

    @Option(name = "--product",
            usage = "Product Name",
            aliases = {"-p"},
            required = true)
    private String name = "";

    @Option(name = "--version",
            usage = "Product version",
            aliases = {"-v"},
            required = true)
    private String version = "";

    @Option(name = "--channel",
            usage = "Product channel",
            aliases = {"-c"})
    private String channel = "LTS";

    @Override
    public void execute() throws CommandExecutionException {
        try {
            ProductUOW productUOW = new ProductUOW();
            logger.info("Creating product ...");
            logger.info(
                    "Input Arguments: \n" +
                    "\tProduct name: " + name + "\n" +
                    "\tProduct version: " + version + "\n" +
                    "\tChannel" + channel);

            StringUtil.concatStrings("Input Arguments: \n",
                    "\tProduct name: ", name, "\n",
                    "\tProduct version: ", version, "\n",
                    "\tChannel", channel);

            /*
                psuedo code:
                query db: is product for the given product name / product version / channel exist
                if true:
                    logger Product information already stored in the db
                if false:
                    persist product name / product version / channel into the db
             */

            Product.Channel productChannel = Product.Channel.valueOf(channel);
            productUOW.persistProduct(name, version, productChannel);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while persisting ProductTestPlan", e);
        }
    }
}
