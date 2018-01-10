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

import org.apache.commons.lang3.SystemUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.CommandExecutionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This is the entry point that handles all the sub-commands like create-product-testplan etc.
 * <p>
 * This extends from the help command since help is not a sub-command but an argument.
 * So, help command is special cased.
 *
 * @since 1.0.0
 */
public class CommandHandler extends HelpCommand {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    @Argument(
            metaVar = "<sub-command>",
            usage = "Consists of several sub-commands",
            handler = SubCommandHandler.class
    )
    @SubCommands({
                         @SubCommand(name = "generate-test-plan",
                                     impl = GenerateTestPlanCommand.class),
                         @SubCommand(name = "run-testplan",
                                     impl = RunTestPlanCommand.class),
                         @SubCommand(name = "generate-report",
                                     impl = GenerateReportCommand.class),
                         @SubCommand(name = "migrate-database",
                                     impl = DBMigrationCommand.class),
                         @SubCommand(name = "help",
                                     impl = HelpCommand.class)
                 })
    private Command cmd = new HelpCommand();
    @Option(name = "-help",
            usage = "Show help",
            hidden = true,
            aliases = {"--help", "-h"})
    private boolean help = false;
    @Option(name = "--testgrid-release-version",
            usage = "Show version",
            hidden = true,
            aliases = {"-r"})
    private boolean version = false;

    /**
     * Returns the version of test grid.
     *
     * @return test grid version
     */
    private static String getVersion() {
        Properties properties = new Properties();

        try (InputStream resourceAsStream = CommandHandler.class
                .getResourceAsStream("/META-INF/maven/org.wso2.testgrid/org.wso2.testgrid.core/pom.properties")) {

            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
                return properties.getProperty("version");
            }
        } catch (IOException e) {
            log.error("Unable determine version from JAR file: " + e.getMessage());
        }
        return "NaN";
    }

    @Override
    public void execute() throws CommandExecutionException {
        log.debug("In Command Handler");
        if (help) {
            super.execute();
            return;
        }

        if (version) {
            final String ls = System.lineSeparator();
            String versionBuilder = "WSO2 Deployment Monitor " + getVersion() + ls +
                                    "Deployment Monitor Home: " +
                                    System.getProperty("deployment.monitor.home", "<unknown>") + ls +
                                    "Java version: " + System.getProperty("java.version", "<unknown>") + ls +
                                    "Java home: " + System.getProperty("java.home", "<unknown>") + ls +
                                    "OS name: \"" + SystemUtils.OS_NAME +
                                    "\", version: \"" + SystemUtils.OS_VERSION +
                                    "\", arch: \"" + SystemUtils.OS_ARCH + ls;

            log.info(versionBuilder);
            return;
        }

        cmd.execute();
    }
}
