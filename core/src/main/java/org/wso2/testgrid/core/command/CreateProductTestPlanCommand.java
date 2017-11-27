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
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.core.TestGridMgtService;
import org.wso2.testgrid.core.TestGridMgtServiceImpl;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

/**
 * This creates a product test plan from the input arguments
 * and persist the information in a database.
 */
public class CreateProductTestPlanCommand extends Command {

    private static final Log log = LogFactory.getLog(CreateProductTestPlanCommand.class);

    @Option(name = "--product",
            usage = "Product Name",
            aliases = {"-p"},
            required = true)
    protected String productName = "";

    @Option(name = "--version",
            usage = "product version",
            aliases = {"-v"},
            required = true)
    protected String productVersion = "";

    @Option(name = "--channel",
            usage = "product channel",
            aliases = {"-c"},
            required = false)
    protected String channel = "public";

    @Option(name = "--infraRepo",
            usage = "Location of Infra plans. "
                    + "Under this location, there should be a Infrastructure/ folder."
                    + "Assume this location is the test-grid-is-resources",
            aliases = {"-ir"},
            required = true)
    protected String infraRepo = "";

    @Option(name = "--infra-configs-location",
            usage = "Location of all the infra plans. "
                    + "Under this location, there should be a Infrastructure/ folder."
                    + "Assume this location is the test-grid-is-resources",
            aliases = {"-ics"},
            required = true)
    protected String infraConfigsLocation = "";
    @Option(name = "--infraPlan",
            usage = "Infrastructure config file",
            aliases = {"-i"},
            required = true)
    protected String infraPlan = "";


    @Override
    public void execute() throws TestGridException {
        log.info("Creating product test plan..");
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

        TestGridMgtService testGridMgtService = new TestGridMgtServiceImpl();
        ProductTestPlan plan = testGridMgtService.createProduct(productName, productVersion, infraRepo);
        //todo add channel as an argument.

        TestPlanUOW testPlanUOW = new TestPlanUOW();
        try {
            testPlanUOW.persistProductTestPlan(plan);
        } catch (TestGridDAOException e) {
            log.error("Error occured while persisting ProductTestPlan", e);
        }
        //todo Persist product and version info in the db.
    }
}
