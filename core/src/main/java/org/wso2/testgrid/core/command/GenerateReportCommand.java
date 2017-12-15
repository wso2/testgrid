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
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
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

            TestReportEngine testReportEngine = new TestReportEngine();
            testReportEngine.generateReport(productName, productVersion, channel, showSuccess, groupBy);
        } catch (ReportingException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error occurred when generating test report for { product: ", productName,
                            ", version: ", productVersion, ", channel: ", channel, " }"), e);
        }
    }
}
