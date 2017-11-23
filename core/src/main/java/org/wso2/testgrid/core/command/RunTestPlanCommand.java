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
import org.wso2.testgrid.common.exception.TestGridException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class RunTestPlanCommand extends Command {

    private static final Log log = LogFactory.getLog(CreateProductTestPlanCommand.class);

    @Option(name = "--testplan",
            usage = "Path to Testplan",
            aliases = { "-t" },
            required = true)
    protected String testPlan = "";

    @Option(name = "--product",
            usage = "Product Name",
            aliases = { "-p" },
            required = true)
    protected String productName = "";

    @Option(name = "--version",
            usage = "product version",
            aliases = { "-v" },
            required = true)
    protected String productVersion = "";

    @Option(name = "--channel",
            usage = "product channel",
            aliases = { "-c" },
            required = false)
    protected String channel = "public";

    @Override
    public void execute() throws TestGridException {
        try {
            log.info("Running the test plan: " + testPlan);
            Path path = Paths.get(testPlan);
            if (!Files.exists(path)) {
                String msg = "The test plan path does not exist: " + path;
                log.info(msg);
                throw new IllegalArgumentException(msg);
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Input Arguments: \n" +
                                "\tProduct name: " + productName + "\n" +
                                "\tProduct version: " + productVersion + "\n" +
                                "\tChannel" + channel);
                log.debug("TestPlan contents : \n" + new String(Files.readAllBytes(path), Charset.forName("UTF-8")));
            }

        } catch (IOException e) {
            throw new TestGridException("Error while executing test plan " + testPlan, e);
        }

    }
}
