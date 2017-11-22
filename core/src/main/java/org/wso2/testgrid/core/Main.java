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

package org.wso2.testgrid.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.exception.TestGridConfigurationException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.core.command.CommandHandler;

/**
 * This is the Main class of TestGrid which initiates the Test execution process for a particular project.
 */
public class Main {

    private static final Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) {
        try {

            CommandHandler commandHandler = new CommandHandler();
            CmdLineParser parser = new CmdLineParser(commandHandler);
            parser.parseArgument(args);

            String repo = "https://github.com/sameerawickramasekara/test-grid-is-resources.git";
            String product = "WSO2_Identity_Server";
            String productVersion = "5.3.0";
            if (args.length == 3) {
                repo = args[0];
                product = args[1];
                productVersion = args[2];
            }

            commandHandler.execute();
            TestGridMgtService testGridMgtService = new TestGridMgtServiceImpl();
            if (testGridMgtService.isEnvironmentConfigured()) {
                log.info("Initializing TestGrid for product : '"
                        + product + ", version  '" + productVersion + "'");
                ProductTestPlan plan = testGridMgtService.addProductTestPlan(product, productVersion, repo);
                testGridMgtService.executeProductTestPlan(plan);
            }
        } catch (TestGridException | TestGridConfigurationException | CmdLineException e) {
            log.error(e.getMessage(), e);
        }
    }
}
