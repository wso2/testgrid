/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.reporting.EscalationEmailGenerator;
import org.wso2.testgrid.reporting.ReportingException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is responsible for generating the escalation email for all products.
 */
public class GenerateEscalationEmailCommand implements Command {

    @Option(name = "--workspace",
            usage = "Product workspace",
            aliases = {"-w"},
            required = true)
    private String workspace = "";

    @Option(name = "--exclude-products",
            usage = "Products to be excluded from escalation mail",
            handler = StringArrayOptionHandler.class,
            aliases = {"-e"}
            )
    private List<String> excludeList = new ArrayList<>();

    @Option(name = "--product-include-pattern",
            usage = "Product names that matches the regex pattern will be included",
            aliases = {"-i"}
    )
    private String productIncludePattern;

    private static final Logger logger = LoggerFactory.getLogger(GenerateEscalationEmailCommand.class);

    @Override
    public void execute() throws CommandExecutionException {

        try {
            EscalationEmailGenerator generator = new EscalationEmailGenerator();
            final Optional<Path> escalationReportPath = generator.
                    generateEscalationEmail(excludeList, productIncludePattern, workspace);
            escalationReportPath.ifPresent(p -> logger.info("Written the escalation email " +
                                                            "report body contents to: " + p));
        } catch (ReportingException e) {
            throw new CommandExecutionException("Error occurred while executing generate escalation command", e);
        }
    }
}
