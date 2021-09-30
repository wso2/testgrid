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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.command.CommandHandler;

/**
 * This is the Main class of TestGrid which initiates the Test execution process for a particular project.
 *
 * @since 1.0.0
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Parse command line arguments
            CommandHandler commandHandler = new CommandHandler();
            CmdLineParser parser = new CmdLineParser(commandHandler);
            parser.parseArgument(args);
            String testGridHome = TestGridUtil.getTestGridHomePath();
            logger.info("TestGrid Home\t: " + testGridHome);
            commandHandler.execute();
            logger.info("Command execution has completed.");
            System.exit(0);
        } catch (CmdLineException e) {
            logger.error("Error while parsing command line arguments.", e);
        } catch (CommandExecutionException e) {
            logger.error("Error while executing command.", e);
        }
    }
}
