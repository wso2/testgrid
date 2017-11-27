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
import org.wso2.testgrid.common.exception.TestGridException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This is the entry point that handles all the sub-commands like create-product-testplan etc.
 *
 * This extends from the help command since help is not a sub-command but an argument.
 * So, help command is special cased.
 *
 */
public class CommandHandler extends HelpCommand {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    @Option(name = "-help",
            usage = "Show help",
            hidden = true,
            aliases = { "--help", "-h" })
    private boolean help = false;

    @Option(name = "--testgrid-release-version",
            usage = "Show version",
            hidden = true,
            aliases = { "-r" })
    private boolean version = false;

    @Argument(index = 0,
              metaVar = "<sub-command>",
              usage = "Consists of several sub-commands",
              required = false,
              handler = SubCommandHandler.class)
    @SubCommands({
                         @SubCommand(name = "generate-infrastructure-plan",
                                     impl = GenerateInfrastructurePlanCommand.class),
                         @SubCommand(name = "gen-infra-plan",
                                     impl = GenerateInfrastructurePlanCommand.class),
                         @SubCommand(name = "create-product-testplan",
                                     impl = CreateProductTestPlanCommand.class),
                         @SubCommand(name = "run-testplan",
                                     impl = RunTestPlanCommand.class),
                         @SubCommand(name = "generate-report",
                                     impl = GenerateReportCommand.class),
                         @SubCommand(name = "help",
                                     impl = HelpCommand.class)
                 })
    public Command cmd = new HelpCommand();

    @Override
    public void execute() throws TestGridException {
        logger.debug("In Command Handler");
        if (help) {
            super.execute();
            return;
        }

        if (version) {
            final String ls = System.lineSeparator();
            StringBuilder versionBuilder = new StringBuilder(256);
            versionBuilder.append("WSO2 Deployment Monitor ").append(getVersion()).append(ls);
            versionBuilder.append("Deployment Monitor Home: ")
                    .append(System.getProperty("deployment.monitor.home", "<unknown>")).append(ls);
            versionBuilder.append("Java version: ").append(System.getProperty("java.version", "<unknown>")).append(ls);

            versionBuilder.append("Java home: ").append(System.getProperty("java.home", "<unknown>")).append(ls);

            versionBuilder.append("OS name: \"").append(SystemUtils.OS_NAME).
                    append("\", version: \"").append(SystemUtils.OS_VERSION).
                    append("\", arch: \"").append(SystemUtils.OS_ARCH).append(ls);

            logger.info(versionBuilder.toString());
            return;
        }

        cmd.execute();
    }

    private static String getVersion() {
        Properties properties = new Properties();

        try (InputStream resourceAsStream = CommandHandler.class.getResourceAsStream(
                "/META-INF/maven/org.wso2.carbon.testgrid/org.wso2.carbon.testgrid.core/pom.properties")) {

            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
                return properties.getProperty("version");
            }
        } catch (IOException e) {
            logger.error("Unable determine version from JAR file: " + e.getMessage());
        }

        return "NaN";
    }

}
